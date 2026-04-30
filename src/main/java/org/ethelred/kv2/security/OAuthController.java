/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.security;

import io.avaje.config.Config;
import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.jex.http.Context;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import org.ethelred.kv2.providers.discord.DiscordApiClient;
import org.ethelred.kv2.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller("/oauth")
@Singleton
public class OAuthController {
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthController.class);
    private static final String DISCORD_AUTHORIZE_URL = "https://discord.com/api/oauth2/authorize";
    private static final String DISCORD_TOKEN_URL = "https://discord.com/api/oauth2/token";
    private static final String STATE_COOKIE = "oauth_state";

    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final UserService userService;
    private final JwtService jwtService;
    private final DiscordApiClient discordApiClient;
    private final JsonType<Map<String, Object>> mapType;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public OAuthController(
            UserService userService, JwtService jwtService, DiscordApiClient discordApiClient, Jsonb jsonb) {
        this.clientId = Config.get("kv2.oauth.discord.client-id", "test_discord_id");
        this.clientSecret = Config.get("kv2.oauth.discord.client-secret", "test_discord_secret");
        this.redirectUri = Config.get("kv2.oauth.discord.redirect-uri", "http://localhost:8080/oauth/discord/callback");
        this.userService = userService;
        this.jwtService = jwtService;
        this.discordApiClient = discordApiClient;
        this.mapType = jsonb.type(Types.mapOf(Object.class));
    }

    @Get("/discord")
    public void discordLogin(Context ctx) {
        var state = UUID.randomUUID().toString();
        ctx.cookie(STATE_COOKIE, state, 600); // 10 min TTL
        var url = DISCORD_AUTHORIZE_URL
                + "?client_id=" + encode(clientId)
                + "&redirect_uri=" + encode(redirectUri)
                + "&response_type=code"
                + "&scope=identify+guilds+email"
                + "&state=" + encode(state);
        ctx.redirect(url);
    }

    @Get("/discord/callback")
    public void discordCallback(Context ctx) {
        var code = ctx.queryParam("code");
        var state = ctx.queryParam("state");
        var savedState = ctx.cookie(STATE_COOKIE);

        if (code == null || state == null || !state.equals(savedState)) {
            ctx.status(400).text("Invalid OAuth state");
            return;
        }

        try {
            var accessToken = exchangeCode(code);
            var auth = DiscordApiClient.authorization(accessToken);
            var discordUser = discordApiClient.getUser(auth);
            var guilds = discordApiClient.getUserGuilds(auth);

            var attributes = Map.<String, Object>of(
                    "provider", "discord",
                    "name", discordUser.username(),
                    "email", discordUser.email() != null ? discordUser.email() : "",
                    "picture", avatarUrl(discordUser),
                    "guilds", guilds.stream().map(g -> g.name()).toList());

            var user = userService.findOrCreateUser("discord", discordUser.id(), attributes);
            var jwt = jwtService.generate(user);

            ctx.cookie("JWT_TOKEN", jwt, (int) (7 * 24 * 60 * 60));
            ctx.removeCookie(STATE_COOKIE);
            ctx.redirect("/");
        } catch (Exception e) {
            LOGGER.error("OAuth callback failed", e);
            ctx.status(500).text("Authentication failed");
        }
    }

    @Get("/logout")
    public void logout(Context ctx) {
        ctx.removeCookie("JWT_TOKEN");
        ctx.redirect("/");
    }

    private String exchangeCode(String code) throws IOException, InterruptedException {
        var body = "grant_type=authorization_code"
                + "&code=" + encode(code)
                + "&redirect_uri=" + encode(redirectUri)
                + "&client_id=" + encode(clientId)
                + "&client_secret=" + encode(clientSecret);

        var request = HttpRequest.newBuilder()
                .uri(URI.create(DISCORD_TOKEN_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> json = mapType.fromJson(response.body());
        var token = json.get("access_token");
        if (token == null) {
            throw new IOException("No access_token in response: " + response.body());
        }
        return token.toString();
    }

    private String avatarUrl(org.ethelred.kv2.providers.discord.DiscordUser user) {
        if (user.avatar() == null) {
            int discriminator;
            try {
                discriminator = Integer.parseInt(user.discriminator());
            } catch (NumberFormatException e) {
                discriminator = 0;
            }
            return "https://cdn.discordapp.com/embed/avatars/%d.png".formatted(discriminator % 5);
        }
        return "https://cdn.discordapp.com/avatars/%s/%s.png?size=128".formatted(user.id(), user.avatar());
    }

    private static String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
