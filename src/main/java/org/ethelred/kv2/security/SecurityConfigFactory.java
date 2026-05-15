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
        return new Impl(Config.get("kv2.security.jwt-secret", "MTExMTExMTExMTExMTExMTExMTExMTExMTExMTExMTE="));
    }

    @Bean
    public OAuthDiscordConfig oauthDiscordConfig() {
        record Impl(String clientId, String clientSecret, String redirectUri, String authorizeUrl, String tokenUrl)
                implements OAuthDiscordConfig {}
        return new Impl(
                Config.get("kv2.oauth.discord.client-id", "test_discord_id"),
                Config.get("kv2.oauth.discord.client-secret", "test_discord_secret"),
                Config.get("kv2.oauth.discord.redirect-uri", "http://localhost:8080/oauth/callback/discord"),
                Config.get("kv2.oauth.discord.authorize-url", "https://discord.com/api/oauth2/authorize"),
                Config.get("kv2.oauth.discord.token-url", "https://discord.com/api/oauth2/token"));
    }
}
