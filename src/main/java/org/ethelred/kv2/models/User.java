/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.models;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.ethelred.kv2.util.AuthAttributesHelper;
import org.jspecify.annotations.Nullable;

public record User(
        String id,
        @Nullable String displayName,
        @Nullable String pictureUrl,
        int flags,
        @Nullable LocalDateTime createdAt,
        @Nullable LocalDateTime updatedAt)
        implements Owner {

    public User(String id, String displayName, String pictureUrl, UserFlag... flagValues) {
        this(id, displayName, pictureUrl, UserFlagSetConverter.fromSet(Set.of(flagValues)), null, null);
    }

    public Set<UserFlag> userFlags() {
        return UserFlagSetConverter.toSet(flags);
    }

    public Map<String, Object> attributes() {
        return AuthAttributesHelper.map(
                "displayName", displayName,
                "picture", pictureUrl,
                "flags", userFlags());
    }

    public Collection<String> roles() {
        return userFlags().stream().filter(UserFlag::isRole).map(Enum::name).toList();
    }

    public View view() {
        return new View(id, displayName);
    }

    public record View(String id, String displayName) {}
}
