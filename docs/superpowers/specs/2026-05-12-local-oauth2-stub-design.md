# Local OAuth2 Stub Server

## Problem

kv2 uses Discord as its sole OAuth2 provider. The `OAuthController` hardcodes Discord's
authorization URL and token URL; `DiscordApiClient` hardcodes the API base URL. Running the app
or testing the OAuth flow requires real Discord credentials and internet access.

A `DevLoginController` exists that bypasses OAuth entirely, but it does not exercise the
redirect → callback → token exchange path.

## Goal

Support both goals without real Discord credentials:
- **Dev:** A realistic OAuth2 login experience (browser redirect flow) using a local stub
- **Tests:** Integration tests that exercise `OAuthController` end-to-end

## Design

### Strategy

Embed a stub OAuth2 server inside kv2 (enabled only when `kv2.dev.enabled = true`). Make the
Discord URLs configurable so profile-specific config can redirect them to the stub.

---

### Part 1 — Configurable Discord URLs

**`OAuthController`** (`src/main/java/org/ethelred/kv2/security/OAuthController.java`)

Replace **both** hardcoded constants with instance fields loaded from Avaje Config (in constructor):

```java
// Remove:
private static final String DISCORD_AUTHORIZE_URL = "https://discord.com/api/oauth2/authorize";
private static final String DISCORD_TOKEN_URL     = "https://discord.com/api/oauth2/token";

// Add (in constructor, alongside existing clientId/clientSecret):
this.authorizeUrl = Config.get("kv2.oauth.discord.authorize-url",
                               "https://discord.com/api/oauth2/authorize");
this.tokenUrl     = Config.get("kv2.oauth.discord.token-url",
                               "https://discord.com/api/oauth2/token");
```

Both fields are used in two places: `discordLogin()` (authorize URL) and `exchangeCode()` (token
URL). Ensure both usages are updated.

**`DiscordApiClient`** (`src/main/java/org/ethelred/kv2/providers/discord/DiscordApiClient.java`)

Replace `private static final String BASE_URL` with a constructor-injected config field:

```java
this.apiBaseUrl = Config.get("kv2.oauth.discord.api-base-url", "https://discord.com/api/v9");
```

Use `apiBaseUrl` in the `request(String path, ...)` method instead of `BASE_URL`.

---

### Part 2 — Stub Controller

**New file:** `src/main/java/org/ethelred/kv2/dev/StubOAuthController.java`

Package: `org.ethelred.kv2.dev`. Annotated `@Controller("/stub")` and `@Singleton`. All routes
check `kv2.dev.enabled` at runtime and return 404 if false (same pattern as `DevLoginController`).

#### Stub users — hardcoded defaults, no config binding needed

Avaje Config 5.x does not support deserialising YAML sequences of maps into `List<T>`. To avoid
inventing an unsupported pattern, the stub controller holds a hardcoded static list of test users.
These are sufficient for dev and test use; there is no requirement to configure arbitrary users.

```java
private static final List<DiscordUser> STUB_USERS = List.of(
    new DiscordUser("111111111111111111", "devuser", "0", null, "devuser@example.com"),
    new DiscordUser("999999999999999999", "testuser", "0", null, "test@example.com")
);

private static final Map<String, List<DiscordGuild>> STUB_GUILDS = Map.of(
    "111111111111111111", List.of(new DiscordGuild("888888888888888888", "Dev Guild", null)),
    "999999999999999999", List.of(new DiscordGuild("888888888888888888", "Test Guild", null))
);
```

Reuse the existing `DiscordUser` and `DiscordGuild` records (already in
`org.ethelred.kv2.providers.discord`) as the in-memory model. The stub serialises them to JSON
using Avaje Jsonb — the same adapters that `DiscordApiClient` uses to deserialise responses —
guaranteeing the field shapes match exactly (including `discriminator` and `icon`).

No `StubUser` or `StubGuild` records are needed.

#### Routes

| Method | Path | Behaviour |
|--------|------|-----------|
| `GET` | `/stub/oauth/authorize` | HTML page listing stub users; each is a link to `/select` |
| `GET` | `/stub/oauth/authorize/select?user_id={id}&redirect_uri={uri}&state={state}` | Redirects to `{redirect_uri}?code={user_id}&state={state}` |
| `POST` | `/stub/oauth/token` | Returns `{"access_token": "{code}", "token_type": "Bearer", ...}` |
| `GET` | `/stub/api/v9/users/@me` | Returns `DiscordUser` JSON for the user whose ID is the Bearer token |
| `GET` | `/stub/api/v9/users/@me/guilds` | Returns guilds JSON for that user |

The "code" in the OAuth exchange is simply the user ID — no random token generation needed.

---

### Part 3 — Configuration

#### `src/main/resources/application.yml`

No changes. All code defaults to real Discord URLs in production.

#### `src/main/resources/application-dev.yml` (additions)

The dev server runs on port 8080 (fixed), so URLs can be hardcoded:

```yaml
kv2:
  oauth:
    discord:
      authorize-url: http://localhost:8080/stub/oauth/authorize
      token-url: http://localhost:8080/stub/oauth/token
      api-base-url: http://localhost:8080/stub/api/v9
```

#### Test configuration

The test server starts on port 0 (dynamic). Avaje Config loads YAML before the port is known, so
`${server.port}` substitution in YAML is not reliable. Instead, `OAuthControllerTest` sets the
three config properties programmatically via `System.setProperty(...)` **before** creating the
`BeanScope`:

```java
int port = findFreePort(); // bind a ServerSocket to 0, get port, close it
System.setProperty("kv2.oauth.discord.authorize-url",
                   "http://localhost:" + port + "/stub/oauth/authorize");
System.setProperty("kv2.oauth.discord.token-url",
                   "http://localhost:" + port + "/stub/oauth/token");
System.setProperty("kv2.oauth.discord.api-base-url",
                   "http://localhost:" + port + "/stub/api/v9");
System.setProperty("kv2.dev.enabled", "true");
// then start server on 'port' (not 0, so we know it upfront)
```

`application-test.yml` requires no URL additions; the test sets them in code.

---

## OAuth Flow (end-to-end)

```
Browser           kv2                      Stub (embedded)
  |                |                            |
  |-- GET /oauth/discord -->                    |
  |                |--(set oauth_state cookie)  |
  |<- 302 /stub/oauth/authorize?...state=X --   |
  |                                             |
  |-------------- GET /stub/oauth/authorize --> |
  |<----------- HTML user picker --------------|
  |                                             |
  |- GET /stub/oauth/authorize/select?user_id=1&state=X -->
  |<-- 302 /oauth/callback/discord?code=1&state=X --------|
  |                                             |
  |-- GET /oauth/callback/discord?code=1&state=X -->
  |                |                            |
  |                |----- POST /stub/oauth/token (code=1) -->
  |                |<---- {"access_token":"1"} ------------|
  |                |                            |
  |                |----- GET /stub/api/v9/users/@me (Bearer 1) -->
  |                |<---- {id:"1",username:"devuser",...} --|
  |                |                            |
  |                |----- GET /stub/api/v9/users/@me/guilds -->
  |                |<---- [{id:"888...",name:"Dev Guild"}] --|
  |                |                            |
  |                |-(findOrCreateUser, generate JWT)       |
  |<- 302 / (JWT_TOKEN cookie set) --           |
```

---

## Testing

### Existing tests (unchanged)

`ApiRosterControllerTest` and similar generate JWTs directly via `JwtService`. These are unchanged
and remain preferred for tests that don't need to exercise the OAuth flow itself.

### New: `OAuthControllerTest`

**File:** `src/test/java/org/ethelred/kv2/controllers/OAuthControllerTest.java`

Setup: bind a free port, set system properties for stub URLs and `kv2.dev.enabled`, start Jex on
that port (not 0), create BeanScope with test profile.

Flow (HTTP client configured **not** to follow redirects):

1. `GET /oauth/discord` → assert 302; capture `oauth_state` cookie and `Location` header
2. Extract `state` from the Location query string
3. `GET /stub/oauth/authorize/select?user_id=999...&redirect_uri={redirect_uri}&state={state}` → assert 302; capture `Location` (the callback URL)
   *(Bypass the HTML picker — going directly to `/select` is intentional for test efficiency)*
4. `GET /oauth/callback/discord?code=999...&state={state}` with `Cookie: oauth_state={state}` → assert 302 to `/`; assert `JWT_TOKEN` cookie is set
5. Decode the JWT using `JwtService.parse(token)`, assert `displayName = "testuser"`

---

## Files

| Action | Path |
|--------|------|
| Modify | `src/main/java/org/ethelred/kv2/security/OAuthController.java` |
| Modify | `src/main/java/org/ethelred/kv2/providers/discord/DiscordApiClient.java` |
| Create | `src/main/java/org/ethelred/kv2/dev/StubOAuthController.java` |
| Modify | `src/main/resources/application-dev.yml` |
| Create | `src/test/java/org/ethelred/kv2/controllers/OAuthControllerTest.java` |
