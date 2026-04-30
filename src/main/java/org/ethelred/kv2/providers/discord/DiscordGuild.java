/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.providers.discord;

import io.avaje.jsonb.Json;
import org.jspecify.annotations.Nullable;

@Json
public record DiscordGuild(
        String id, // snowflake
        String name,
        @Nullable String icon) {}
