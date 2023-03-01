/* (C) Edward Harman and contributors 2022-2023 */
package org.ethelred.kv2.models;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.*;
import io.micronaut.data.model.DataType;
import java.sql.Timestamp;
import java.util.Map;

@MappedEntity
public record Identity(
        @Id String id,
        @NonNull String provider,
        @Relation(Relation.Kind.MANY_TO_ONE) User user,
        @NonNull String externalId,
        @Nullable String email,
        @TypeDef(type = DataType.JSON) Map<String, Object> attributes,
        @DateCreated @Nullable Timestamp createdAt,
        @DateUpdated @Nullable Timestamp updatedAt) {
    public Identity(String provider, User user, String externalId, String email, Map<String, Object> attributes) {
        this(provider + "#" + externalId, provider, user, externalId, email, attributes, null, null);
    }
}
