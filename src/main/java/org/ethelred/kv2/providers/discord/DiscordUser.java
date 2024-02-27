/* (C) Edward Harman and contributors 2022-2024 */
package org.ethelred.kv2.providers.discord;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable;

/** @author edward */
@Introspected
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record DiscordUser(
        String id, // snowflake, but api returns as string
        String username,
        String discriminator,
        @Nullable String avatar,
        @Nullable String email) {}
