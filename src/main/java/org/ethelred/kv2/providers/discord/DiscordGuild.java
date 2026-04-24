/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.providers.discord;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.jspecify.annotations.Nullable;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record DiscordGuild(
        String id, // snowflake
        String name,
        @Nullable String icon) {}
