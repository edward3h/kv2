/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.models;

import io.avaje.jsonb.Json;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import org.jspecify.annotations.Nullable;

public record User(
        String id,
        @Nullable String displayName,
        @Nullable String pictureUrl,
        int flags,
        @Nullable LocalDateTime createdAt,
        @Nullable LocalDateTime updatedAt)
        implements Owner {

    @SuppressWarnings("nullness")
    public User(
            @Nullable String id, @Nullable String displayName, @Nullable String pictureUrl, UserFlag... flagValues) {
        this(id, displayName, pictureUrl, UserFlagSetConverter.fromSet(Set.of(flagValues)), null, null);
    }

    public Set<UserFlag> userFlags() {
        return UserFlagSetConverter.toSet(flags);
    }

    public Collection<String> roles() {
        return userFlags().stream().filter(UserFlag::isRole).map(Enum::name).toList();
    }

    public Info view() {
        return new Info(id, displayName);
    }

    @Json
    public record Info(String id, @Nullable String displayName) {}
}
