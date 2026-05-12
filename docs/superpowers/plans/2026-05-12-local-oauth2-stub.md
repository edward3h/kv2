# Local OAuth2 Stub Server Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Embed a stub OAuth2 server in kv2 (dev/test profile only) so the full Discord OAuth2 flow can be exercised without real credentials, and make Discord URLs configurable so profile config can redirect them to the stub.

**Architecture:** `StubOAuthController` implements `Routing.HttpService` directly (avoids annotation processor issues with `@me` in paths) and serves five stub endpoints under `/stub/...`. `OAuthController` and `DiscordApiClient` have their hardcoded Discord URLs replaced with Avaje Config fields — defaults point at real Discord; dev/test config overrides point at the stub. `AuthFilter` is extended to treat `/stub/.*` as always public (same treatment as `/dev/.*`).

**Tech Stack:** Java 21, Avaje Jex 3.5, Avaje Inject, Avaje Config 5.1, Avaje Jsonb 3.13, JUnit 5, Testcontainers MySQL

---

## File Structure

| Action | Path | Responsibility |
|--------|------|----------------|
| Create | `src/main/java/org/ethelred/kv2/dev/StubOAuthController.java` | Stub OAuth2 + Discord API endpoints; only active when `kv2.dev.enabled=true` |
| Modify | `src/main/java/org/ethelred/kv2/security/AuthFilter.java` | Add `/stub/` to always-public paths |
| Modify | `src/main/java/org/ethelred/kv2/security/OAuthController.java` | Replace 2 hardcoded Discord URL constants with Avaje Config fields |
| Modify | `src/main/java/org/ethelred/kv2/providers/discord/DiscordApiClient.java` | Replace hardcoded `BASE_URL` constant with Avaje Config field |
| Modify | `src/main/resources/application-dev.yml` | Add URL overrides pointing to stub |
| Create | `src/test/java/org/ethelred/kv2/controllers/OAuthControllerTest.java` | Integration tests: stub endpoints + full OAuth flow |

---

## Chunk 1: StubOAuthController

### Task 1: Write failing tests for stub endpoints

**Files:**
- Create: `src/test/java/org/ethelred/kv2/controllers/OAuthControllerTest.java`

- [ ] **Step 1: Create `OAuthControllerTest.java` with stub endpoint tests**

```java
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
import org.ethelred.kv2.controllers.MyExceptionHandlers;
import org.ethelred.kv2.security.AuthFilter;
import org.ethelred.kv2.security.JwtService;
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
    private final HttpClient http = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    @BeforeAll
    public void startServer() throws Exception {
        // Allocate a free port so we can configure stub URLs before BeanScope creation
        try (var ss = new ServerSocket(0)) {
            port = ss.getLocalPort();
        }

        // Set stub URL config before BeanScope constructs OAuthController + DiscordApiClient
        System.setProperty("kv2.oauth.discord.authorize-url",
                "http://localhost:" + port + "/stub/oauth/authorize");
        System.setProperty("kv2.oauth.discord.token-url",
                "http://localhost:" + port + "/stub/oauth/token");
        System.setProperty("kv2.oauth.discord.api-base-url",
                "http://localhost:" + port + "/stub/api/v9");
        System.setProperty("kv2.oauth.discord.redirect-uri",
                "http://localhost:" + port + "/oauth/callback/discord");
        System.setProperty("kv2.dev.enabled", "true");

        scope = BeanScope.builder().profiles("test").build();
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
        System.clearProperty("kv2.oauth.discord.authorize-url");
        System.clearProperty("kv2.oauth.discord.token-url");
        System.clearProperty("kv2.oauth.discord.api-base-url");
        System.clearProperty("kv2.oauth.discord.redirect-uri"); // must match BeforeAll set
        System.clearProperty("kv2.dev.enabled");
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }

    private HttpResponse<String> get(String path) throws Exception {
        return http.send(HttpRequest.newBuilder(uri(path)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, String body) throws Exception {
        return http.send(HttpRequest.newBuilder(uri(path))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build(),
                HttpResponse.BodyHandlers.ofString());
    }

    // --- Stub endpoint tests ---

    @Test
    public void stubAuthorizeShowsUsers() throws Exception {
        var redirectUri = URLEncoder.encode("http://localhost:" + port + "/oauth/callback/discord",
                StandardCharsets.UTF_8);
        var response = get("/stub/oauth/authorize?client_id=test&redirect_uri=" + redirectUri
                + "&response_type=code&scope=identify&state=teststate");
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("devuser"));
        assertTrue(response.body().contains("testuser"));
    }

    @Test
    public void stubSelectRedirectsWithCode() throws Exception {
        var redirectUri = URLEncoder.encode("http://localhost:" + port + "/oauth/callback/discord",
                StandardCharsets.UTF_8);
        var response = get("/stub/oauth/authorize/select?user_id=111111111111111111"
                + "&redirect_uri=" + redirectUri + "&state=teststate");
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
        var response = http.send(HttpRequest.newBuilder(uri("/stub/api/v9/users/@me"))
                .header("Authorization", "Bearer 111111111111111111")
                .GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("devuser"));
    }

    @Test
    public void stubGetGuildsReturnsFakeGuilds() throws Exception {
        var response = http.send(HttpRequest.newBuilder(uri("/stub/api/v9/users/@me/guilds"))
                .header("Authorization", "Bearer 111111111111111111")
                .GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Dev Guild"));
    }
}
```

- [ ] **Step 2: Run tests — expect compile or class-not-found errors**

```bash
./gradlew test --tests "org.ethelred.kv2.controllers.OAuthControllerTest" --info 2>&1 | tail -30
```

Expected: compilation failure or test failure — `StubOAuthController` not yet created

---

### Task 2: Add `/stub/` to AuthFilter public paths

**Files:**
- Modify: `src/main/java/org/ethelred/kv2/security/AuthFilter.java:53`

- [ ] **Step 3: Update `requiresAuth` to treat `/stub/` paths as always public**

In `AuthFilter.java`, find the existing check at line ~53:
```java
if (path.startsWith("/oauth/") || path.startsWith("/dev/")) {
    return false;
}
```

Change to:
```java
if (path.startsWith("/oauth/") || path.startsWith("/dev/") || path.startsWith("/stub/")) {
    return false;
}
```

---

### Task 3: Implement StubOAuthController

**Files:**
- Create: `src/main/java/org/ethelred/kv2/dev/StubOAuthController.java`

- [ ] **Step 4: Create `StubOAuthController.java`**

```java
/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.dev;

import io.avaje.config.Config;
import io.avaje.jex.Routing;
import io.avaje.jex.http.Context;
import io.avaje.jex.http.HttpResponseException;
import jakarta.inject.Singleton;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.ethelred.kv2.providers.discord.DiscordGuild;
import org.ethelred.kv2.providers.discord.DiscordUser;

@Singleton
public class StubOAuthController implements Routing.HttpService {

    private static final List<DiscordUser> STUB_USERS = List.of(
            new DiscordUser("111111111111111111", "devuser", "0", null, "devuser@example.com"),
            new DiscordUser("999999999999999999", "testuser", "0", null, "test@example.com"));

    private static final Map<String, List<DiscordGuild>> STUB_GUILDS = Map.of(
            "111111111111111111",
            List.of(new DiscordGuild("888888888888888888", "Dev Guild", null)),
            "999999999999999999",
            List.of(new DiscordGuild("888888888888888888", "Test Guild", null)));

    @Override
    public void add(Routing routing) {
        routing.get("/stub/oauth/authorize", this::authorize);
        routing.get("/stub/oauth/authorize/select", this::select);
        routing.post("/stub/oauth/token", this::token);
        // Use wildcard to avoid potential @me routing issues; dispatch in handler
        routing.get("/stub/api/v9/users/*", this::usersDispatch);
    }

    private boolean isDevEnabled() {
        return Config.getBool("kv2.dev.enabled", false);
    }

    private void authorize(Context ctx) {
        if (!isDevEnabled()) throw new HttpResponseException(404, "Not found");
        var redirectUri = ctx.queryParam("redirect_uri");
        var state = ctx.queryParam("state");
        var html = new StringBuilder("<html><body><h1>Stub OAuth2 Login</h1><ul>");
        for (var user : STUB_USERS) {
            var selectUrl = "/stub/oauth/authorize/select?user_id=" + encode(user.id())
                    + "&redirect_uri=" + encode(redirectUri)
                    + "&state=" + encode(state);
            html.append("<li><a href='").append(selectUrl)
                    .append("'>Login as ").append(user.username()).append("</a></li>");
        }
        html.append("</ul></body></html>");
        ctx.contentType("text/html");
        ctx.result(html.toString());
    }

    private void select(Context ctx) {
        if (!isDevEnabled()) throw new HttpResponseException(404, "Not found");
        var userId = ctx.queryParam("user_id");
        var redirectUri = ctx.queryParam("redirect_uri");
        var state = ctx.queryParam("state");
        ctx.redirect(redirectUri + "?code=" + encode(userId) + "&state=" + encode(state));
    }

    private void token(Context ctx) {
        if (!isDevEnabled()) throw new HttpResponseException(404, "Not found");
        var code = ctx.formParam("code");
        ctx.contentType("application/json");
        ctx.result("{\"access_token\":\"" + code
                + "\",\"token_type\":\"Bearer\",\"scope\":\"identify guilds email\"}");
    }

    private void usersDispatch(Context ctx) {
        if (!isDevEnabled()) throw new HttpResponseException(404, "Not found");
        var path = ctx.path();
        if (path.endsWith("/@me/guilds")) {
            getGuilds(ctx);
        } else if (path.endsWith("/@me")) {
            getUser(ctx);
        } else {
            throw new HttpResponseException(404, "Not found");
        }
    }

    private void getUser(Context ctx) {
        var auth = ctx.header("Authorization");
        var userId = auth != null ? auth.replaceFirst("^Bearer ", "") : "";
        var user = STUB_USERS.stream()
                .filter(u -> u.id().equals(userId))
                .findFirst()
                .orElseThrow(() -> new HttpResponseException(404, "User not found"));
        ctx.contentType("application/json");
        ctx.result(userToJson(user));
    }

    private void getGuilds(Context ctx) {
        var auth = ctx.header("Authorization");
        var userId = auth != null ? auth.replaceFirst("^Bearer ", "") : "";
        var guilds = STUB_GUILDS.getOrDefault(userId, List.of());
        ctx.contentType("application/json");
        ctx.result(guildsToJson(guilds));
    }

    private static String userToJson(DiscordUser u) {
        return "{\"id\":\"%s\",\"username\":\"%s\",\"discriminator\":\"%s\",\"avatar\":%s,\"email\":%s}"
                .formatted(
                        u.id(),
                        u.username(),
                        u.discriminator(),
                        u.avatar() != null ? "\"" + u.avatar() + "\"" : "null",
                        u.email() != null ? "\"" + u.email() + "\"" : "null");
    }

    private static String guildsToJson(List<DiscordGuild> guilds) {
        var sb = new StringBuilder("[");
        for (int i = 0; i < guilds.size(); i++) {
            if (i > 0) sb.append(",");
            var g = guilds.get(i);
            sb.append("{\"id\":\"").append(g.id())
                    .append("\",\"name\":\"").append(g.name())
                    .append("\",\"icon\":null}");
        }
        return sb.append("]").toString();
    }

    private static String encode(String s) {
        return URLEncoder.encode(s != null ? s : "", StandardCharsets.UTF_8);
    }
}
```

- [ ] **Step 5: Run stub endpoint tests — expect PASS**

```bash
./gradlew test --tests "org.ethelred.kv2.controllers.OAuthControllerTest.stubAuthorize*" \
    --tests "org.ethelred.kv2.controllers.OAuthControllerTest.stubSelect*" \
    --tests "org.ethelred.kv2.controllers.OAuthControllerTest.stubToken*" \
    --tests "org.ethelred.kv2.controllers.OAuthControllerTest.stubGetUser*" \
    --tests "org.ethelred.kv2.controllers.OAuthControllerTest.stubGetGuilds*" \
    --info 2>&1 | tail -30
```

Expected: all 5 stub tests PASS

- [ ] **Step 6: Run full check to confirm no regressions**

```bash
./gradlew check 2>&1 | tail -20
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git checkout -b feat/local-oauth2-stub
git add src/main/java/org/ethelred/kv2/dev/StubOAuthController.java \
    src/main/java/org/ethelred/kv2/security/AuthFilter.java \
    src/test/java/org/ethelred/kv2/controllers/OAuthControllerTest.java
git commit -m "feat: add stub OAuth2 server for dev/test"
```

---

## Chunk 2: Configurable URLs + Full Integration Test

### Task 4: Make OAuthController Discord URLs configurable

**Files:**
- Modify: `src/main/java/org/ethelred/kv2/security/OAuthController.java`

`OAuthController` currently has two `private static final String` constants (lines 30–31) that hardcode Discord's URLs. Both are used in instance methods: `DISCORD_AUTHORIZE_URL` in `discordLogin()` and `DISCORD_TOKEN_URL` in `exchangeCode()`.

- [ ] **Step 8: Replace the two static final constants with Config-backed instance fields**

Remove:
```java
private static final String DISCORD_AUTHORIZE_URL = "https://discord.com/api/oauth2/authorize";
private static final String DISCORD_TOKEN_URL = "https://discord.com/api/oauth2/token";
```

Add instance fields (after `private final String redirectUri;`):
```java
private final String authorizeUrl;
private final String tokenUrl;
```

Add to the constructor body (alongside the existing `clientId`, `clientSecret`, `redirectUri` assignments):
```java
this.authorizeUrl = Config.get("kv2.oauth.discord.authorize-url",
        "https://discord.com/api/oauth2/authorize");
this.tokenUrl = Config.get("kv2.oauth.discord.token-url",
        "https://discord.com/api/oauth2/token");
```

In `discordLogin()`, replace `DISCORD_AUTHORIZE_URL` with `authorizeUrl`:
```java
var url = authorizeUrl
        + "?client_id=" + encode(clientId)
        + "&redirect_uri=" + encode(redirectUri)
        + "&response_type=code"
        + "&scope=identify+guilds+email"
        + "&state=" + encode(state);
```

In `exchangeCode()`, replace `DISCORD_TOKEN_URL` with `tokenUrl`:
```java
var request = HttpRequest.newBuilder()
        .uri(URI.create(tokenUrl))
        .header("Content-Type", "application/x-www-form-urlencoded")
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .build();
```

- [ ] **Step 9: Run full check — no behaviour change expected**

```bash
./gradlew check 2>&1 | tail -20
```

Expected: BUILD SUCCESSFUL (all existing tests still pass)

- [ ] **Step 10: Commit**

```bash
git add src/main/java/org/ethelred/kv2/security/OAuthController.java
git commit -m "refactor: make Discord OAuth URLs configurable via Avaje Config"
```

---

### Task 5: Make DiscordApiClient base URL configurable

**Files:**
- Modify: `src/main/java/org/ethelred/kv2/providers/discord/DiscordApiClient.java`

`DiscordApiClient` has `private static final String BASE_URL = "https://discord.com/api/v9"` (line 17). It is used only in the private `request()` method.

- [ ] **Step 11: Replace the static final constant with a Config-backed instance field**

Add import at top: `import io.avaje.config.Config;`

Remove:
```java
private static final String BASE_URL = "https://discord.com/api/v9";
```

Add instance field (after `USER_AGENT`):
```java
private final String apiBaseUrl;
```

Add to the constructor body (first line):
```java
this.apiBaseUrl = Config.get("kv2.oauth.discord.api-base-url", "https://discord.com/api/v9");
```

In `request()`, replace `BASE_URL` with `apiBaseUrl`:
```java
return HttpRequest.newBuilder()
        .uri(URI.create(apiBaseUrl + path))
        .header("Authorization", authorization)
        .header("User-Agent", USER_AGENT)
        .GET()
        .build();
```

- [ ] **Step 12: Run full check**

```bash
./gradlew check 2>&1 | tail -20
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 13: Commit**

```bash
git add src/main/java/org/ethelred/kv2/providers/discord/DiscordApiClient.java
git commit -m "refactor: make Discord API base URL configurable via Avaje Config"
```

---

### Task 6: Full OAuth flow integration test

**Files:**
- Modify: `src/test/java/org/ethelred/kv2/controllers/OAuthControllerTest.java`

Now that both `OAuthController` and `DiscordApiClient` read URLs from Config, add the full end-to-end test to `OAuthControllerTest`. The server setup in `@BeforeAll` already sets the stub URLs via `System.setProperty`, so the wiring is correct.

- [ ] **Step 14: Add `fullOAuthFlow` test method to `OAuthControllerTest.java`**

Add this test inside `OAuthControllerTest`:

```java
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
            .findFirst().orElseThrow();
    var stateValue = stateCookieHeader.split("=")[1].split(";")[0];

    // Step 2: call select directly (bypassing the HTML picker UI)
    var redirectUri = URLEncoder.encode(
            "http://localhost:" + port + "/oauth/callback/discord", StandardCharsets.UTF_8);
    var selectResponse = get("/stub/oauth/authorize/select?user_id=999999999999999999"
            + "&redirect_uri=" + redirectUri + "&state=" + stateValue);
    assertEquals(302, selectResponse.statusCode());
    var callbackLocation = selectResponse.headers().firstValue("Location").orElseThrow();
    assertTrue(callbackLocation.contains("/oauth/callback/discord"));
    assertTrue(callbackLocation.contains("code=999999999999999999"));

    // Step 3: complete the callback with the oauth_state cookie
    // callbackLocation is absolute (http://localhost:PORT/oauth/callback/discord?code=...&state=...)
    var callbackResponse = http.send(
            HttpRequest.newBuilder(URI.create(callbackLocation))
                    .header("Cookie", "oauth_state=" + stateValue)
                    .GET().build(),
            HttpResponse.BodyHandlers.ofString());
    assertEquals(302, callbackResponse.statusCode());
    var jwtCookieHeader = callbackResponse.headers().allValues("Set-Cookie").stream()
            .filter(c -> c.startsWith("JWT_TOKEN="))
            .findFirst().orElseThrow();

    // Step 4: validate the JWT
    var jwtValue = jwtCookieHeader.split("=")[1].split(";")[0];
    var principal = jwtService.parse(jwtValue);
    assertTrue(principal != null);
    assertTrue(principal.roles().contains("ROLE_USER"));
}
```

- [ ] **Step 15: Run the full integration test**

```bash
./gradlew test --tests "org.ethelred.kv2.controllers.OAuthControllerTest.fullOAuthFlow" \
    --info 2>&1 | tail -40
```

Expected: PASS — full OAuth flow exercised end-to-end against the embedded stub

- [ ] **Step 16: Run full check to confirm all tests pass**

```bash
./gradlew check 2>&1 | tail -20
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 17: Commit**

```bash
git add src/test/java/org/ethelred/kv2/controllers/OAuthControllerTest.java
git commit -m "test: add full OAuth flow integration test using embedded stub"
```

---

### Task 7: Update dev config to use stub URLs

**Files:**
- Modify: `src/main/resources/application-dev.yml`

The dev server always runs on port 8080. Add the stub URL overrides so that running `./gradlew run` (dev profile) uses the stub without real Discord credentials.

- [ ] **Step 18: Add stub URL overrides to `application-dev.yml`**

Current content of `application-dev.yml`:
```yaml
kv2:
  dev:
    enabled: true
  jte:
    dynamic: true
    dynamic-source-path: views/src/main/jte
```

Append the OAuth URL overrides:
```yaml
kv2:
  dev:
    enabled: true
  jte:
    dynamic: true
    dynamic-source-path: views/src/main/jte
  oauth:
    discord:
      authorize-url: http://localhost:8080/stub/oauth/authorize
      token-url: http://localhost:8080/stub/oauth/token
      api-base-url: http://localhost:8080/stub/api/v9
      redirect-uri: http://localhost:8080/oauth/callback/discord
```

- [ ] **Step 19: Verify the application starts with dev profile**

```bash
./gradlew run &
sleep 5
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/stub/oauth/authorize?client_id=test&redirect_uri=http://localhost:8080/oauth/callback/discord&state=test&response_type=code
```

Expected: `200` — stub authorize page serves HTML

Kill the background process: `kill %1`

- [ ] **Step 20: Final commit**

```bash
git add src/main/resources/application-dev.yml
git commit -m "config: redirect dev OAuth to embedded stub server"
```

---

## Verification Checklist

- [ ] `./gradlew check` passes with all tests green
- [ ] Stub endpoint tests in `OAuthControllerTest` pass (5 tests)
- [ ] `fullOAuthFlow` integration test passes (1 test)
- [ ] Dev server starts and `/stub/oauth/authorize` returns 200 HTML with user list
- [ ] Visiting `http://localhost:8080/oauth/discord` in a browser shows the stub login page
- [ ] `OAuthController` defaults to real Discord URLs (no `.env` required for production)
