/* (C) Edward Harman and contributors 2022-2024 */
package org.ethelred.kv2.models;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.DateCreated;
import io.micronaut.data.annotation.DateUpdated;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.TypeDef;
import io.micronaut.data.model.DataType;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.ethelred.kv2.services.GeneratedId;
import org.ethelred.kv2.util.AuthAttributesHelper;

@MappedEntity
public record User(
        @GeneratedId @Id String id,
        @Nullable String displayName,
        @Nullable String pictureUrl,
        @TypeDef(type = DataType.INTEGER, converter = UserFlagSetConverter.class) Set<UserFlag> flags,
        @DateCreated @Nullable Timestamp createdAt,
        @DateUpdated @Nullable Timestamp updatedAt)
        implements Owner {

    public User(String id, String displayName, String pictureUrl, UserFlag... flags) {
        this(id, displayName, pictureUrl, Set.of(flags), null, null);
    }

    public Map<String, Object> attributes() {
        return AuthAttributesHelper.map(
                "displayName", displayName,
                "picture", pictureUrl,
                "flags", flags);
    }

    public Collection<String> roles() {
        return flags.stream().filter(UserFlag::isRole).map(Enum::name).toList();
    }

    public View view() {
        return new View(id, displayName);
    }

    public record View(String id, String displayName) {}
}
