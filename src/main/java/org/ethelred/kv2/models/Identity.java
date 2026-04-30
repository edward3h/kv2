/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.models;

import java.time.LocalDateTime;
import org.jspecify.annotations.Nullable;

public record Identity(
        String id,
        String provider,
        String userId,
        String externalId,
        @Nullable String email,
        @Nullable String attributes,
        @Nullable LocalDateTime createdAt,
        @Nullable LocalDateTime updatedAt) {
    public Identity(String provider, User user, String externalId, String email, String attributes) {
        this(provider + "#" + externalId, provider, user.id(), externalId, email, attributes, null, null);
    }
}
