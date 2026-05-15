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
