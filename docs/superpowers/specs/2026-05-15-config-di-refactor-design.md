# Config DI Refactor Design

**Date:** 2026-05-15  
**Topic:** Remove static `Config` calls from beans; replace with injected typed interfaces

---

## Context

The codebase uses `io.avaje:avaje-config` for configuration and `io.avaje:avaje-inject` for dependency injection. Currently, 16 production-code call sites access configuration via static methods (`Config.get()`, `Config.getBool()`, `Config.getOptional()`) directly inside bean constructors and instance methods. This violates DI principles: beans should receive their dependencies, not reach out to fetch them. The existing `AssetsConfiguration` class already demonstrates the correct pattern and serves as the model for this refactor.

---

## Goal

Consolidate all static `Config` reads into `@Bean` factory methods. Consumers receive typed interfaces via constructor injection. No static `Config` calls remain in constructors or instance methods of non-factory beans.

---

## Architecture

### Per-domain configuration interfaces

One public interface per configuration domain, defined as a top-level type in the package of its consumer(s). Implementations are private records created inside the `@Bean` factory method — never exposed.

| Interface | Package | Accessor methods |
|---|---|---|
| `OAuthDiscordConfig` | `security/` | `clientId()`, `clientSecret()`, `redirectUri()`, `authorizeUrl()`, `tokenUrl()` |
| `JwtConfig` | `security/` | `jwtSecret()` |
| `DataSourceConfig` | `data/` | `url()`, `driverClassName()`, `@Nullable String username()`, `password()` |
| `TemplatesConfig` | `services/` | `dynamic()`, `dynamicSourcePath()` |
| `DevConfig` | `dev/` | `enabled()` |
| `DiscordApiConfig` | `providers/discord/` | `apiBaseUrl()` |

### Factory classes

| Factory class | New or existing | Produces |
|---|---|---|
| `SecurityConfigFactory` | New `@Factory` in `security/` | `OAuthDiscordConfig`, `JwtConfig` |
| `DataSourceFactory` | Existing — add `@Bean dataSourceConfig()` | `DataSourceConfig`, existing `DataSource` |
| `TemplatesFactory` | Existing — add `@Bean templatesConfig()` | `TemplatesConfig`, existing `Templates` |
| `DevConfigFactory` | New `@Factory` in `dev/` | `DevConfig` |
| `DiscordConfigFactory` | New `@Factory` in `providers/discord/` | `DiscordApiConfig` |

---

## Implementation Pattern

```java
// Public interface — top-level in its package
public interface OAuthDiscordConfig {
    String clientId();
    String clientSecret();
    String redirectUri();
    String authorizeUrl();
    String tokenUrl();
}

// Factory — private record implements the interface
@Factory
class SecurityConfigFactory {
    @Bean
    OAuthDiscordConfig oauthDiscordConfig() {
        record Impl(
            String clientId, String clientSecret, String redirectUri,
            String authorizeUrl, String tokenUrl
        ) implements OAuthDiscordConfig {}
        return new Impl(
            Config.get("kv2.oauth.discord.client-id", "test_discord_id"),
            Config.get("kv2.oauth.discord.client-secret", "test_discord_secret"),
            Config.get("kv2.oauth.discord.redirect-uri", "http://localhost:8080/oauth/discord/callback"),
            Config.get("kv2.oauth.discord.authorize-url", "https://discord.com/api/oauth2/authorize"),
            Config.get("kv2.oauth.discord.token-url", "https://discord.com/api/oauth2/token")
        );
    }

    @Bean
    JwtConfig jwtConfig() {
        record Impl(String jwtSecret) implements JwtConfig {}
        return new Impl(
            Config.get("kv2.security.jwt-secret", "MTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTE=")
        );
    }
}

// Consumer — no static Config calls
@Singleton
class OAuthController {
    private final OAuthDiscordConfig config;

    OAuthController(OAuthDiscordConfig config, /* other deps */) {
        this.config = config;
    }
}
```

---

## DataSourceFactory Restructure

The existing `DataSourceFactory` currently reads Config in its `@Bean DataSource` method. Split into two methods so config and datasource creation are separate:

```java
@Factory
class DataSourceFactory {
    @Bean
    DataSourceConfig dataSourceConfig() {
        record Impl(String url, String driverClassName, @Nullable String username, String password)
                implements DataSourceConfig {}
        return new Impl(
            Config.get("datasource.url"),
            Config.get("datasource.driverClassName", "com.mysql.cj.jdbc.Driver"),
            Config.getOptional("datasource.username").orElse(null),
            Config.get("datasource.password", "")
        );
    }

    @Bean
    DataSource dataSource(DataSourceConfig config) {
        // uses config.url(), config.username(), etc. — no static Config
    }
}
```

---

## DevConfig — Shared Interface

Both `DevLoginController` and `StubOAuthController` currently call `Config.getBool("kv2.dev.enabled", false)` in instance methods. Both receive `DevConfig` via constructor injection and store it as a field:

```java
@Singleton
class DevLoginController {
    private final DevConfig devConfig;

    DevLoginController(DevConfig devConfig) {
        this.devConfig = devConfig;
    }

    // instance method replaces Config.getBool() with:
    if (!devConfig.enabled()) { ... }
}
```

---

## AssetsConfiguration

`AssetsConfiguration` uses `Config.asConfiguration()` to obtain the full avaje `Configuration` object as a bean. This is already the correct pattern (factory produces a bean, consumer injects it) and is left unchanged.

---

## Test Impact

Tests currently use `Config.setProperty()` / `Config.clearProperty()` around `BeanScope` construction. With typed interfaces, tests supply implementations directly:

```java
// Before
Config.setProperty("kv2.oauth.discord.client-id", "test_id");
var scope = BeanScope.builder().build();
Config.clearProperty("kv2.oauth.discord.client-id");

// After
var scope = BeanScope.builder()
    .bean(OAuthDiscordConfig.class, new OAuthDiscordConfig() {
        public String clientId() { return "test_id"; }
        // ...
    })
    .build();
```

---

## Error Handling

No change in startup behaviour. `Config.get()` without a default already throws if the property is absent; `Config.getOptional()` returns `Optional.empty()`, which the factory converts to `null` via `.orElse(null)` for the `@Nullable` field. These semantics are preserved — the calls simply move into factory methods.

---

## Verification

1. `./gradlew check` — all tests pass
2. `grep -r "Config\.get\|Config\.getBool\|Config\.getOptional" src/main` — zero results
3. Start the application; verify OAuth login, JWT auth, datasource connection, and dev mode all function correctly
