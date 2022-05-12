package org.ethelred.kv2.models;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import java.sql.Timestamp;

@MappedEntity
public record User(
        @Id String id, @Nullable String displayName, @Nullable Timestamp createdAt, @Nullable Timestamp updatedAt) {
    public User(String id, String displayName) {
        this(id, displayName, null, null);
    }
}
