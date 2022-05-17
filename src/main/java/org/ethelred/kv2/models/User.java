package org.ethelred.kv2.models;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.*;
import io.micronaut.data.annotation.*;
import java.sql.Timestamp;
import java.util.*;
import org.ethelred.kv2.services.*;

@MappedEntity
public record User(
        @GeneratedId @Id String id,
        @Nullable String displayName,
        @Nullable String pictureUrl,
        @DateCreated @Nullable Timestamp createdAt,
        @DateUpdated @Nullable Timestamp updatedAt) {
    public User(String id, String displayName, String pictureUrl) {
        this(id, displayName, pictureUrl, null, null);
    }

    public Map<String, Object> attributes() {
        //noinspection unchecked
        return (Map<String, Object>) CollectionUtils.mapOf("displayName", displayName, "picture", pictureUrl);
    }
}
