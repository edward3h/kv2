/* (C) Edward Harman and contributors 2022 */
package org.ethelred.kv2.models;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.annotation.DateCreated;
import io.micronaut.data.annotation.DateUpdated;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.Relation;
import java.sql.Timestamp;
import java.util.Comparator;
import org.ethelred.kv2.services.GeneratedId;

@MappedEntity
public record SimpleRoster(
        @GeneratedId @Id String id,
        String title,
        String body,
        @Relation(Relation.Kind.MANY_TO_ONE) User owner,
        Visibility visibility,
        @DateCreated @Nullable Timestamp createdAt,
        @DateUpdated @Nullable Timestamp updatedAt)
        implements Owned {
    private static final Comparator<String> HEADER_SORT =
            Comparator.comparingLong(s -> s.chars().takeWhile(c -> c == '#').count());

    public SimpleRoster(User owner, String rosterBody, Visibility visibility) {
        this(null, _extractTitle(rosterBody), rosterBody, owner, visibility, null, null);
    }

    private static String _extractTitle(String rosterBody) {
        return rosterBody
                .lines()
                .filter(line -> line.startsWith("#"))
                .min(HEADER_SORT)
                .map(s -> s.replaceAll("^#+\\s*", ""))
                .orElse("My Roster");
    }
}
