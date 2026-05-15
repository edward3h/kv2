# Config DI Refactor Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace all static `Config.get/getBool/getOptional` calls in non-factory beans with constructor-injected typed interfaces, following the existing `AssetsConfiguration` pattern.

**Architecture:** One public interface per configuration domain lives in the consumer's package. A private record created inside a `@Bean` factory method implements the interface. Consumers declare the interface as a constructor parameter and avaje-inject wires it automatically.

**Tech Stack:** Java 21, avaje-inject 12.5, avaje-config 5.1, JUnit 5, Spotless (Palantir format + licence header)

**Spec:** `docs/superpowers/specs/2026-05-15-config-di-refactor-design.md`

---

## File Map

### New files
| File | Purpose |
|---|---|
| `src/main/java/org/ethelred/kv2/data/DataSourceConfig.java` | Interface: datasource connection properties |
| `src/main/java/org/ethelred/kv2/services/TemplatesConfig.java` | Interface: JTE template engine settings |
| `src/main/java/org/ethelred/kv2/dev/DevConfig.java` | Interface: dev-mode feature flag |
| `src/main/java/org/ethelred/kv2/dev/DevConfigFactory.java` | `@Factory` producing `DevConfig` |
| `src/main/java/org/ethelred/kv2/providers/discord/DiscordApiConfig.java` | Interface: Discord API base URL |
| `src/main/java/org/ethelred/kv2/providers/discord/DiscordConfigFactory.java` | `@Factory` producing `DiscordApiConfig` |
| `src/main/java/org/ethelred/kv2/security/JwtConfig.java` | Interface: JWT signing secret |
| `src/main/java/org/ethelred/kv2/security/OAuthDiscordConfig.java` | Interface: Discord OAuth endpoints + credentials |
| `src/main/java/org/ethelred/kv2/security/SecurityConfigFactory.java` | `@Factory` producing `JwtConfig` + `OAuthDiscordConfig` |

### Modified files
| File | Change |
|---|---|
| `src/main/java/org/ethelred/kv2/data/DataSourceFactory.java` | Extract `dataSourceConfig()` bean; pass `DataSourceConfig` to `dataSource()` |
| `src/main/java/org/ethelred/kv2/services/TemplatesFactory.java` | Extract `templatesConfig()` bean; pass `TemplatesConfig` to `templates()` |
| `src/main/java/org/ethelred/kv2/dev/DevLoginController.java` | Inject `DevConfig`; remove static `Config.getBool` |
| `src/main/java/org/ethelred/kv2/dev/StubOAuthController.java` | Inject `DevConfig`; remove static `Config.getBool` |
| `src/main/java/org/ethelred/kv2/providers/discord/DiscordApiClient.java` | Inject `DiscordApiConfig`; remove static `Config.get` |
| `src/main/java/org/ethelred/kv2/security/JwtService.java` | Inject `JwtConfig`; remove static `Config.get` |
| `src/main/java/org/ethelred/kv2/security/OAuthController.java` | Inject `OAuthDiscordConfig`; remove five static `Config.get` calls |
| `src/test/java/org/ethelred/kv2/controllers/OAuthControllerTest.java` | Replace `Config.setProperty/clearProperty` with `BeanScope.builder().withBean(...)` |
| `src/uiTest/java/org/ethelred/kv2/ui/LoginUITest.java` | Replace `Config.setProperty/clearProperty` with `BeanScope.builder().withBean(...)` |

---

## Chunk 1: DataSourceConfig and TemplatesConfig

### Task 1: DataSourceConfig interface and DataSourceFactory restructure

**Files:**
- Create: `src/main/java/org/ethelred/kv2/data/DataSourceConfig.java`
- Modify: `src/main/java/org/ethelred/kv2/data/DataSourceFactory.java`

- [ ] **Step 1: Create the DataSourceConfig interface**

```java
/* (C) Edward Harman and contributors 2026 */
package org.ethelred.kv2.data;

import org.jspecify.annotations.Nullable;

public interface DataSourceConfig {
    String url();

    String driverClassName();

    @Nullable String username();

    String password();
}
```

- [ ] **Step 2: Restructure DataSourceFactory**

Replace the single `dataSource()` method with two `@Bean` methods. The `dataSourceConfig()` method does all static `Config` reading. The `dataSource(DataSourceConfig)` method receives config via injection.

Full replacement for `DataSourceFactory.java`:

```java
/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.avaje.config.Config;
import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import jakarta.inject.Named;
import javax.sql.DataSource;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Factory
public class DataSourceFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceFactory.class);

    @Bean
    public DataSourceConfig dataSourceConfig() {
        record Impl(String url, String driverClassName, String username, String password)
                implements DataSourceConfig {}
        return new Impl(
                Config.get("datasource.url"),
                Config.get("datasource.driverClassName", "com.mysql.cj.jdbc.Driver"),
                Config.getOptional("datasource.username").orElse(null),
                Config.get("datasource.password", ""));
    }

    @Bean
    @Named("default")
    public DataSource dataSource(DataSourceConfig config) {
        var hikari = new HikariConfig();
        hikari.setJdbcUrl(config.url());
        hikari.setDriverClassName(config.driverClassName());
        var username = config.username();
        if (username != null) {
            hikari.setUsername(username);
            hikari.setPassword(config.password());
        }
        var ds = new HikariDataSource(hikari);
        runLiquibase(ds);
        return ds;
    }

    private void runLiquibase(DataSource ds) {
        try (var conn = ds.getConnection()) {
            var database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(conn));
            var liquibase = new Liquibase("db/liquibase-changelog.xml", new ClassLoaderResourceAccessor(), database);
            liquibase.update("");
            LOGGER.info("Liquibase migrations applied");
        } catch (Exception e) {
            throw new RuntimeException("Liquibase migration failed", e);
        }
    }
}
```

Note: the `Impl` record has `String username` (not `@Nullable String`) as a record component — `@Nullable` is on the interface method. Spotless will auto-format on build; do not add the `@Nullable` annotation to the record component to avoid checker framework issues with records.

- [ ] **Step 3: Run the build**

```bash
./gradlew check
```

Expected: `BUILD SUCCESSFUL`. If Spotless reports formatting issues, run `./gradlew spotlessApply` then re-run `check`.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/org/ethelred/kv2/data/DataSourceConfig.java \
        src/main/java/org/ethelred/kv2/data/DataSourceFactory.java
git commit -m "refactor: inject DataSourceConfig into DataSourceFactory"
```

---

### Task 2: TemplatesConfig interface and TemplatesFactory restructure

**Files:**
- Create: `src/main/java/org/ethelred/kv2/services/TemplatesConfig.java`
- Modify: `src/main/java/org/ethelred/kv2/services/TemplatesFactory.java`

- [ ] **Step 1: Create the TemplatesConfig interface**

```java
/* (C) Edward Harman and contributors 2026 */
package org.ethelred.kv2.services;

public interface TemplatesConfig {
    boolean dynamic();

    String dynamicSourcePath();
}
```

- [ ] **Step 2: Restructure TemplatesFactory**

```java
/* (C) Edward Harman and contributors 2023-2026 */
package org.ethelred.kv2.services;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import io.avaje.config.Config;
import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import java.nio.file.Paths;
import org.ethelred.kv2.template.DynamicTemplates;
import org.ethelred.kv2.template.StaticTemplates;
import org.ethelred.kv2.template.Templates;

@Factory
public class TemplatesFactory {

    @Bean
    public TemplatesConfig templatesConfig() {
        record Impl(boolean dynamic, String dynamicSourcePath) implements TemplatesConfig {}
        return new Impl(
                Config.getBool("kv2.jte.dynamic", false),
                Config.get("kv2.jte.dynamic-source-path", "views/src/main/jte"));
    }

    @Bean
    public Templates templates(TemplatesConfig config) {
        if (config.dynamic()) {
            var engine = TemplateEngine.create(
                    new DirectoryCodeResolver(Paths.get(config.dynamicSourcePath())), ContentType.Html);
            return new DynamicTemplates(engine);
        }
        return new StaticTemplates();
    }
}
```

- [ ] **Step 3: Run the build**

```bash
./gradlew check
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/org/ethelred/kv2/services/TemplatesConfig.java \
        src/main/java/org/ethelred/kv2/services/TemplatesFactory.java
git commit -m "refactor: inject TemplatesConfig into TemplatesFactory"
```

---

## Chunk 2: DevConfig and DiscordApiConfig

### Task 3: DevConfig interface, factory, and shared consumer updates

Both `DevLoginController` and `StubOAuthController` currently call `Config.getBool("kv2.dev.enabled", false)` in instance methods. Both will receive `DevConfig` via constructor injection.

**Files:**
- Create: `src/main/java/org/ethelred/kv2/dev/DevConfig.java`
- Create: `src/main/java/org/ethelred/kv2/dev/DevConfigFactory.java`
- Modify: `src/main/java/org/ethelred/kv2/dev/DevLoginController.java`
- Modify: `src/main/java/org/ethelred/kv2/dev/StubOAuthController.java`

- [ ] **Step 1: Create the DevConfig interface**

```java
/* (C) Edward Harman and contributors 2026 */
package org.ethelred.kv2.dev;

public interface DevConfig {
    boolean enabled();
}
```

- [ ] **Step 2: Create DevConfigFactory**

```java
/* (C) Edward Harman and contributors 2026 */
package org.ethelred.kv2.dev;

import io.avaje.config.Config;
import io.avaje.inject.Bean;
import io.avaje.inject.Factory;

@Factory
public class DevConfigFactory {

    @Bean
    public DevConfig devConfig() {
        record Impl(boolean enabled) implements DevConfig {}
        return new Impl(Config.getBool("kv2.dev.enabled", false));
    }
}
```

- [ ] **Step 3: Update DevLoginController**

Replace the `Config` import and `isDevEnabled()` method. Add `DevConfig` field and constructor parameter:

```java
/* (C) Edward Harman and contributors 2023-2026 */
package org.ethelred.kv2.dev;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Post;
import io.avaje.http.api.Produces;
import io.avaje.jex.http.Context;
import jakarta.inject.Singleton;
import java.util.Map;
import org.ethelred.kv2.security.JwtService;
import org.ethelred.kv2.services.UserService;

@Controller("/dev")
@Singleton
public class DevLoginController {
    private final UserService userService;
    private final JwtService jwtService;
    private final DevConfig devConfig;

    public DevLoginController(UserService userService, JwtService jwtService, DevConfig devConfig) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.devConfig = devConfig;
    }

    private boolean isDevEnabled() {
        return devConfig.enabled();
    }

    @Get("/login")
    @Produces("text/html")
    public String loginForm(Context ctx) {
        if (!isDevEnabled()) {
            throw new io.avaje.jex.http.HttpResponseException(404, "Not found");
        }
        // language=HTML
        return """
                <html>
                <body>
                <form action='/dev/login' method='post'>
                <label for='username'>Login as</label>
                <input type='text' id='username' name='username'>
                <input type='submit' value='Login'>
                </form>
                </body>
                </html>
                """;
    }

    @Post("/login")
    public void doLogin(Context ctx) {
        if (!isDevEnabled()) {
            throw new io.avaje.jex.http.HttpResponseException(404, "Not found");
        }
        var username = ctx.formParam("username");
        if (username == null || username.isBlank()) {
            ctx.redirect("/dev/login");
            return;
        }
        var user = userService.findOrCreateUser("dev", username, Map.of("name", username));
        var jwt = jwtService.generate(user);
        ctx.cookie(Context.Cookie.of("JWT_TOKEN", jwt)
                .maxAge(java.time.Duration.ofSeconds(7 * 24 * 60 * 60))
                .path("/"));
        ctx.redirect("/");
    }
}
```

- [ ] **Step 4: Update StubOAuthController**

Remove the `Config` import and add `DevConfig` constructor injection. Replace the `isDevEnabled()` method body:

```java
/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.dev;

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

    private final DevConfig devConfig;

    public StubOAuthController(DevConfig devConfig) {
        this.devConfig = devConfig;
    }

    @Override
    public void add(Routing routing) {
        routing.get("/stub/oauth/authorize", this::authorize);
        routing.get("/stub/oauth/authorize/select", this::select);
        routing.post("/stub/oauth/token", this::token);
        // Use wildcard to avoid potential @me routing issues; dispatch in handler
        routing.get("/stub/api/v9/users/*", this::usersDispatch);
    }

    private boolean isDevEnabled() {
        return devConfig.enabled();
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
            html.append("<li><a href='")
                    .append(selectUrl)
                    .append("'>Login as ")
                    .append(user.username())
                    .append("</a></li>");
        }
        html.append("</ul></body></html>");
        ctx.html(html.toString());
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
        ctx.write("{\"access_token\":\"" + code + "\",\"token_type\":\"Bearer\",\"scope\":\"identify guilds email\"}");
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
        ctx.write(userToJson(user));
    }

    private void getGuilds(Context ctx) {
        var auth = ctx.header("Authorization");
        var userId = auth != null ? auth.replaceFirst("^Bearer ", "") : "";
        var guilds = STUB_GUILDS.getOrDefault(userId, List.of());
        ctx.contentType("application/json");
        ctx.write(guildsToJson(guilds));
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
            sb.append("{\"id\":\"")
                    .append(g.id())
                    .append("\",\"name\":\"")
                    .append(g.name())
                    .append("\",\"icon\":null}");
        }
        return sb.append("]").toString();
    }

    private static String encode(String s) {
        return URLEncoder.encode(s != null ? s : "", StandardCharsets.UTF_8);
    }
}
```

- [ ] **Step 5: Run the build**

```bash
./gradlew check
```

Expected: `BUILD SUCCESSFUL`. The tests use `Config.setProperty("kv2.dev.enabled", "true")` which still reaches `DevConfigFactory` at BeanScope construction time, so tests continue to pass.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/org/ethelred/kv2/dev/DevConfig.java \
        src/main/java/org/ethelred/kv2/dev/DevConfigFactory.java \
        src/main/java/org/ethelred/kv2/dev/DevLoginController.java \
        src/main/java/org/ethelred/kv2/dev/StubOAuthController.java
git commit -m "refactor: inject DevConfig into dev controllers"
```

---

### Task 4: DiscordApiConfig interface, factory, and DiscordApiClient update

**Files:**
- Create: `src/main/java/org/ethelred/kv2/providers/discord/DiscordApiConfig.java`
- Create: `src/main/java/org/ethelred/kv2/providers/discord/DiscordConfigFactory.java`
- Modify: `src/main/java/org/ethelred/kv2/providers/discord/DiscordApiClient.java`

- [ ] **Step 1: Create the DiscordApiConfig interface**

```java
/* (C) Edward Harman and contributors 2026 */
package org.ethelred.kv2.providers.discord;

public interface DiscordApiConfig {
    String apiBaseUrl();
}
```

- [ ] **Step 2: Create DiscordConfigFactory**

```java
/* (C) Edward Harman and contributors 2026 */
package org.ethelred.kv2.providers.discord;

import io.avaje.config.Config;
import io.avaje.inject.Bean;
import io.avaje.inject.Factory;

@Factory
public class DiscordConfigFactory {

    @Bean
    public DiscordApiConfig discordApiConfig() {
        record Impl(String apiBaseUrl) implements DiscordApiConfig {}
        return new Impl(Config.get("kv2.oauth.discord.api-base-url", "https://discord.com/api/v9"));
    }
}
```

- [ ] **Step 3: Update DiscordApiClient**

Remove the `Config` import. Add `DiscordApiConfig` constructor parameter:

```java
/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.providers.discord;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Singleton
public class DiscordApiClient {
    private static final String USER_AGENT = "Ordo Acerbus Login (https://github.com/edward3h/kv2 0.1)";

    private final String apiBaseUrl;
    private final HttpClient http = HttpClient.newHttpClient();
    private final JsonType<DiscordUser> userType;
    private final JsonType<List<DiscordGuild>> guildsType;

    DiscordApiClient(DiscordApiConfig config, Jsonb jsonb) {
        this.apiBaseUrl = config.apiBaseUrl();
        this.userType = jsonb.type(DiscordUser.class);
        this.guildsType = jsonb.type(Types.listOf(DiscordGuild.class));
    }

    public DiscordUser getUser(String authorization) {
        return get("/users/@me", authorization, userType);
    }

    public List<DiscordGuild> getUserGuilds(String authorization) {
        return get("/users/@me/guilds", authorization, guildsType);
    }

    public static String authorization(String token) {
        return "Bearer %s".formatted(token);
    }

    private <T> T get(String path, String authorization, JsonType<T> type) {
        try {
            var response = http.send(request(path, authorization), HttpResponse.BodyHandlers.ofString());
            return type.fromJson(response.body());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpRequest request(String path, String authorization) {
        return HttpRequest.newBuilder()
                .uri(URI.create(apiBaseUrl + path))
                .header("Authorization", authorization)
                .header("User-Agent", USER_AGENT)
                .GET()
                .build();
    }
}
```

- [ ] **Step 4: Run the build**

```bash
./gradlew check
```

Expected: `BUILD SUCCESSFUL`. Test still works because `Config.setProperty("kv2.oauth.discord.api-base-url", ...)` is read by `DiscordConfigFactory` at BeanScope construction time.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/ethelred/kv2/providers/discord/DiscordApiConfig.java \
        src/main/java/org/ethelred/kv2/providers/discord/DiscordConfigFactory.java \
        src/main/java/org/ethelred/kv2/providers/discord/DiscordApiClient.java
git commit -m "refactor: inject DiscordApiConfig into DiscordApiClient"
```

---

## Chunk 3: Security configs and test updates

### Task 5: JwtConfig, OAuthDiscordConfig, SecurityConfigFactory, and JwtService

**Files:**
- Create: `src/main/java/org/ethelred/kv2/security/JwtConfig.java`
- Create: `src/main/java/org/ethelred/kv2/security/OAuthDiscordConfig.java`
- Create: `src/main/java/org/ethelred/kv2/security/SecurityConfigFactory.java`
- Modify: `src/main/java/org/ethelred/kv2/security/JwtService.java`

- [ ] **Step 1: Create JwtConfig interface**

```java
/* (C) Edward Harman and contributors 2026 */
package org.ethelred.kv2.security;

public interface JwtConfig {
    String jwtSecret();
}
```

- [ ] **Step 2: Create OAuthDiscordConfig interface**

```java
/* (C) Edward Harman and contributors 2026 */
package org.ethelred.kv2.security;

public interface OAuthDiscordConfig {
    String clientId();

    String clientSecret();

    String redirectUri();

    String authorizeUrl();

    String tokenUrl();
}
```

- [ ] **Step 3: Create SecurityConfigFactory**

```java
/* (C) Edward Harman and contributors 2026 */
package org.ethelred.kv2.security;

import io.avaje.config.Config;
import io.avaje.inject.Bean;
import io.avaje.inject.Factory;

@Factory
public class SecurityConfigFactory {

    @Bean
    public JwtConfig jwtConfig() {
        record Impl(String jwtSecret) implements JwtConfig {}
        return new Impl(Config.get(
                "kv2.security.jwt-secret", "MTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTE="));
    }

    @Bean
    public OAuthDiscordConfig oauthDiscordConfig() {
        record Impl(
                String clientId,
                String clientSecret,
                String redirectUri,
                String authorizeUrl,
                String tokenUrl)
                implements OAuthDiscordConfig {}
        return new Impl(
                Config.get("kv2.oauth.discord.client-id", "test_discord_id"),
                Config.get("kv2.oauth.discord.client-secret", "test_discord_secret"),
                Config.get(
                        "kv2.oauth.discord.redirect-uri", "http://localhost:8080/oauth/discord/callback"),
                Config.get(
                        "kv2.oauth.discord.authorize-url", "https://discord.com/api/oauth2/authorize"),
                Config.get("kv2.oauth.discord.token-url", "https://discord.com/api/oauth2/token"));
    }
}
```

- [ ] **Step 4: Update JwtService**

Remove `Config` import. Add `JwtConfig` constructor parameter and replace `Config.get(...)` call:

```java
/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.inject.Singleton;
import java.text.ParseException;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.ethelred.kv2.models.User;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class JwtService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtService.class);
    private static final long EXPIRY_SECONDS = 7 * 24 * 60 * 60; // 7 days

    private final JWSSigner signer;
    private final JWSVerifier verifier;

    public JwtService(JwtConfig config) {
        try {
            var keyBytes = Base64.getDecoder().decode(config.jwtSecret());
            this.signer = new MACSigner(keyBytes);
            this.verifier = new MACVerifier(keyBytes);
        } catch (JOSEException e) {
            throw new IllegalStateException("Failed to initialise JwtService", e);
        }
    }

    public String generate(User user) {
        try {
            var now = Instant.now();
            var claims = new JWTClaimsSet.Builder()
                    .subject(user.id())
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plusSeconds(EXPIRY_SECONDS)))
                    .claim("roles", user.roles())
                    .claim("displayName", orEmpty(user.displayName()))
                    .claim("picture", orEmpty(user.pictureUrl()))
                    .build();
            var jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            jwt.sign(signer);
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException("Failed to generate JWT", e);
        }
    }

    public @Nullable Principal parse(String token) {
        try {
            var jwt = SignedJWT.parse(token);
            if (!jwt.verify(verifier)) {
                return null;
            }
            var claims = jwt.getJWTClaimsSet();
            if (claims.getExpirationTime().before(new Date())) {
                return null;
            }
            @SuppressWarnings("unchecked")
            var roles = (List<String>) claims.getClaim("roles");
            var attributes = Map.<String, Object>of(
                    "displayName", orEmpty(claims.getStringClaim("displayName")),
                    "picture", orEmpty(claims.getStringClaim("picture")));
            return new Principal(claims.getSubject(), roles, attributes);
        } catch (ParseException | JOSEException e) {
            LOGGER.debug("Invalid JWT: {}", e.toString());
            return null;
        }
    }

    private String orEmpty(@Nullable String s) {
        return s != null ? s : "";
    }
}
```

- [ ] **Step 5: Run the build (compile check only — tests will fail until OAuthController is updated)**

```bash
./gradlew compileJava compileTestJava compileUiTestJava
```

Expected: `BUILD SUCCESSFUL` (compile-only; tests not run yet since OAuthController still uses static Config).

---

### Task 6: OAuthController update and full test suite

**Files:**
- Modify: `src/main/java/org/ethelred/kv2/security/OAuthController.java`
- Modify: `src/test/java/org/ethelred/kv2/controllers/OAuthControllerTest.java`
- Modify: `src/uiTest/java/org/ethelred/kv2/ui/LoginUITest.java`

- [ ] **Step 1: Update OAuthController**

Remove the five `Config.get` calls and individual string fields. Inject `OAuthDiscordConfig` instead:

```java
/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.security;

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
    private static final String STATE_COOKIE = "oauth_state";

    private final OAuthDiscordConfig config;
    private final UserService userService;
    private final JwtService jwtService;
    private final DiscordApiClient discordApiClient;
    private final JsonType<Map<String, Object>> mapType;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public OAuthController(
            OAuthDiscordConfig config,
            UserService userService,
            JwtService jwtService,
            DiscordApiClient discordApiClient,
            Jsonb jsonb) {
        this.config = config;
        this.userService = userService;
        this.jwtService = jwtService;
        this.discordApiClient = discordApiClient;
        this.mapType = jsonb.type(Types.mapOf(Object.class));
    }

    @Get("/discord")
    public void discordLogin(Context ctx) {
        var state = UUID.randomUUID().toString();
        ctx.cookie(STATE_COOKIE, state, 600); // 10 min TTL
        var url = config.authorizeUrl()
                + "?client_id=" + encode(config.clientId())
                + "&redirect_uri=" + encode(config.redirectUri())
                + "&response_type=code"
                + "&scope=identify+guilds+email"
                + "&state=" + encode(state);
        ctx.redirect(url);
    }

    @Get("/callback/discord")
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

            ctx.cookie(Context.Cookie.of("JWT_TOKEN", jwt)
                    .maxAge(java.time.Duration.ofSeconds(7 * 24 * 60 * 60))
                    .path("/"));
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
                + "&redirect_uri=" + encode(config.redirectUri())
                + "&client_id=" + encode(config.clientId())
                + "&client_secret=" + encode(config.clientSecret());

        var request = HttpRequest.newBuilder()
                .uri(URI.create(config.tokenUrl()))
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
```

- [ ] **Step 2: Update OAuthControllerTest**

Replace `Config.setProperty/clearProperty` calls with `withBean(...)` on the `BeanScope.builder()`. The `startServer` and `stopServer` methods become:

```java
@BeforeAll
public void startServer() throws Exception {
    try (var ss = new ServerSocket(0)) {
        port = ss.getLocalPort();
    }

    scope = BeanScope.builder()
            .profiles("test")
            .bean(OAuthDiscordConfig.class, new OAuthDiscordConfig() {
                public String clientId() { return "test_discord_id"; }
                public String clientSecret() { return "test_discord_secret"; }
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
                public boolean enabled() { return true; }
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
```

Also update imports — remove `io.avaje.config.Config`, add:
```java
import org.ethelred.kv2.dev.DevConfig;
import org.ethelred.kv2.providers.discord.DiscordApiConfig;
import org.ethelred.kv2.security.OAuthDiscordConfig;
```

- [ ] **Step 3: Update LoginUITest**

Apply the same pattern to `setUp` and `tearDown`:

```java
@BeforeAll
public void setUp() throws Exception {
    try (var ss = new ServerSocket(0)) {
        port = ss.getLocalPort();
    }

    scope = BeanScope.builder()
            .profiles("test")
            .bean(OAuthDiscordConfig.class, new OAuthDiscordConfig() {
                public String clientId() { return "test_discord_id"; }
                public String clientSecret() { return "test_discord_secret"; }
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
                public boolean enabled() { return true; }
            })
            .build();
    var authFilter = scope.get(AuthFilter.class);
    var exHandlers = scope.get(MyExceptionHandlers.class);

    var app = Jex.create()
            .routing(scope.list(Routing.HttpService.class))
            .before(authFilter::before)
            .port(port);
    exHandlers.configure(app);
    server = app.start();

    playwright = Playwright.create();
    browser = playwright.chromium().launch();
}

@AfterAll
public void tearDown() {
    browser.close();
    playwright.close();
    server.shutdown();
    scope.close();
}
```

Add imports (remove `io.avaje.config.Config`, add same three as `OAuthControllerTest`).

- [ ] **Step 4: Run the full build**

```bash
./gradlew check
```

Expected: `BUILD SUCCESSFUL`. All tests pass.

- [ ] **Step 5: Verify no static Config calls remain in production code**

```bash
grep -r "Config\.get\|Config\.getBool\|Config\.getOptional" src/main
```

Expected: no output (zero matches).

- [ ] **Step 6: Commit**

```bash
git add src/main/java/org/ethelred/kv2/security/JwtConfig.java \
        src/main/java/org/ethelred/kv2/security/OAuthDiscordConfig.java \
        src/main/java/org/ethelred/kv2/security/SecurityConfigFactory.java \
        src/main/java/org/ethelred/kv2/security/JwtService.java \
        src/main/java/org/ethelred/kv2/security/OAuthController.java \
        src/test/java/org/ethelred/kv2/controllers/OAuthControllerTest.java \
        src/uiTest/java/org/ethelred/kv2/ui/LoginUITest.java
git commit -m "refactor: inject JwtConfig and OAuthDiscordConfig; update tests to use bean()"
```
