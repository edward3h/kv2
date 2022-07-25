/* (C) Edward Harman and contributors 2022 */
package org.ethelred.kv2.models;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.*;
import io.micronaut.data.annotation.*;
import io.micronaut.data.model.DataType;
import java.sql.Timestamp;
import java.util.*;
import org.ethelred.kv2.services.*;

@MappedEntity
public record User(
        @GeneratedId @Id String id,
        @Nullable String displayName,
        @Nullable String pictureUrl,
        @TypeDef(type = DataType.INTEGER, converter = UserFlagSetConverter.class) Set<UserFlag> flags,
        @DateCreated @Nullable Timestamp createdAt,
        @DateUpdated @Nullable Timestamp updatedAt) {
    public User(String id, String displayName, String pictureUrl, UserFlag... flags) {
        this(id, displayName, pictureUrl, Set.of(flags), null, null);
    }

    public Map<String, Object> attributes() {
        //noinspection unchecked
        return (Map<String, Object>) CollectionUtils.mapOf(
                "displayName", displayName,
                "picture", pictureUrl,
                "flags", flags,
                "user", this);
    }

    public Collection<String> roles() {
        return flags.stream().filter(UserFlag::isRole).map(Enum::name).toList();
    }
}
