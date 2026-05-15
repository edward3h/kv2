/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.avaje.inject.BeanScope;
import io.avaje.jex.Jex;
import io.avaje.jex.Routing;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.ethelred.kv2.MySQLContainerExtension;
import org.ethelred.kv2.dev.DevConfig;
import org.ethelred.kv2.providers.discord.DiscordApiConfig;
import org.ethelred.kv2.security.AuthFilter;
import org.ethelred.kv2.security.JwtService;
import org.ethelred.kv2.security.OAuthDiscordConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MySQLContainerExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SuppressWarnings("initialization")
public class OAuthControllerTest {

    private BeanScope scope;
    private Jex.Server server;
    private int port;
    private JwtService jwtService;
    private final HttpClient http =
            HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();

    @BeforeAll
    public void startServer() throws Exception {
        try (var ss = new ServerSocket(0)) {
            port = ss.getLocalPort();
        }

        // JwtConfig uses the SecurityConfigFactory default — both generate/parse paths use the same key
        scope = BeanScope.builder()
                .profiles("test")
                .bean(OAuthDiscordConfig.class, new OAuthDiscordConfig() {
                    public String clientId() {
                        return "test_discord_id";
                    }

                    public String clientSecret() {
                        return "test_discord_secret";
                    }

                    public String redirectUri() {
                        return "http://localhost:" + port + "/oauth/callback/discord";
                    }

                    public String authorizeUrl() {
                        return "http://localhost:" + port + "/stub/oauth/authorize";
                    }

                    public String tokenUrl() {
                        return "http://localhost:" + port + "/stub/oauth/token";
                    }
                })
                .bean(DiscordApiConfig.class, new DiscordApiConfig() {
                    public String apiBaseUrl() {
                        return "http://localhost:" + port + "/stub/api/v9";
                    }
                })
                .bean(DevConfig.class, new DevConfig() {
                    public boolean enabled() {
                        return true;
                    }
                })
                .build();
        jwtService = scope.get(JwtService.class);

        var authFilter = scope.get(AuthFilter.class);
        var exHandlers = scope.get(MyExceptionHandlers.class);

        var app = Jex.create()
                .routing(scope.list(Routing.HttpService.class))
                .before(authFilter::before)
                .port(port);
        exHandlers.configure(app);
        server = app.start();
    }

    @AfterAll
    public void stopServer() {
        server.shutdown();
        scope.close();
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }

    private HttpResponse<String> get(String path) throws Exception {
        return http.send(HttpRequest.newBuilder(uri(path)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, String body) throws Exception {
        return http.send(
                HttpRequest.newBuilder(uri(path))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
    }

    @Test
    public void stubAuthorizeShowsUsers() throws Exception {
        var redirectUri =
                URLEncoder.encode("http://localhost:" + port + "/oauth/callback/discord", StandardCharsets.UTF_8);
        var response = get("/stub/oauth/authorize?client_id=test&redirect_uri=" + redirectUri
                + "&response_type=code&scope=identify&state=teststate");
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("devuser"));
        assertTrue(response.body().contains("testuser"));
    }

    @Test
    public void stubSelectRedirectsWithCode() throws Exception {
        var redirectUri =
                URLEncoder.encode("http://localhost:" + port + "/oauth/callback/discord", StandardCharsets.UTF_8);
        var response = get("/stub/oauth/authorize/select?user_id=111111111111111111" + "&redirect_uri=" + redirectUri
                + "&state=teststate");
        assertEquals(302, response.statusCode());
        var location = response.headers().firstValue("Location").orElseThrow();
        assertTrue(location.contains("code=111111111111111111"));
        assertTrue(location.contains("state=teststate"));
    }

    @Test
    public void stubTokenReturnsAccessToken() throws Exception {
        var body = "grant_type=authorization_code&code=111111111111111111"
                + "&redirect_uri=http%3A%2F%2Flocalhost%3A" + port + "%2Foauth%2Fcallback%2Fdiscord"
                + "&client_id=test&client_secret=test";
        var response = post("/stub/oauth/token", body);
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"access_token\":\"111111111111111111\""));
    }

    @Test
    public void stubGetUserReturnsFakeUser() throws Exception {
        var response = http.send(
                HttpRequest.newBuilder(uri("/stub/api/v9/users/@me"))
                        .header("Authorization", "Bearer 111111111111111111")
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("devuser"));
    }

    @Test
    public void stubGetGuildsReturnsFakeGuilds() throws Exception {
        var response = http.send(
                HttpRequest.newBuilder(uri("/stub/api/v9/users/@me/guilds"))
                        .header("Authorization", "Bearer 111111111111111111")
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Dev Guild"));
    }

    @Test
    public void fullOAuthFlow() throws Exception {
        // Step 1: initiate login — OAuthController redirects to stub authorize URL
        var loginResponse = get("/oauth/discord");
        assertEquals(302, loginResponse.statusCode());
        var authorizeLocation = loginResponse.headers().firstValue("Location").orElseThrow();
        assertTrue(authorizeLocation.contains("/stub/oauth/authorize"));

        // Extract oauth_state cookie value
        var stateCookieHeader = loginResponse.headers().allValues("Set-Cookie").stream()
                .filter(c -> c.startsWith("oauth_state="))
                .findFirst()
                .orElseThrow();
        var stateValue = stateCookieHeader.split("=")[1].split(";")[0];

        // Step 2: call select directly (bypassing the HTML picker UI)
        var redirectUri =
                URLEncoder.encode("http://localhost:" + port + "/oauth/callback/discord", StandardCharsets.UTF_8);
        var selectResponse = get("/stub/oauth/authorize/select?user_id=999999999999999999" + "&redirect_uri="
                + redirectUri + "&state=" + stateValue);
        assertEquals(302, selectResponse.statusCode());
        var callbackLocation = selectResponse.headers().firstValue("Location").orElseThrow();
        assertTrue(callbackLocation.contains("/oauth/callback/discord"));
        assertTrue(callbackLocation.contains("code=999999999999999999"));

        // Step 3: complete the callback with the oauth_state cookie
        // callbackLocation is absolute (http://localhost:PORT/oauth/callback/discord?code=...&state=...)
        var callbackResponse = http.send(
                HttpRequest.newBuilder(URI.create(callbackLocation))
                        .header("Cookie", "oauth_state=" + stateValue)
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(302, callbackResponse.statusCode());
        var jwtCookieHeader = callbackResponse.headers().allValues("Set-Cookie").stream()
                .filter(c -> c.startsWith("JWT_TOKEN="))
                .findFirst()
                .orElseThrow();

        // Step 4: validate the JWT
        var jwtValue = jwtCookieHeader.split("=")[1].split(";")[0];
        var principal = jwtService.parse(jwtValue);
        if (principal == null) throw new AssertionError("JWT parse returned null");
        assertTrue(principal.roles().contains("ROLE_USER"));
    }
}
