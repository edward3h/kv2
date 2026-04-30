/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.models;

import io.avaje.jsonb.Json;
import java.time.LocalDateTime;
import java.util.Comparator;
import org.jspecify.annotations.Nullable;

public record SimpleRoster(
        String id,
        String title,
        String body,
        User owner,
        Visibility visibility,
        @Nullable LocalDateTime createdAt,
        @Nullable LocalDateTime updatedAt)
        implements Owned {
    private static final Comparator<String> HEADER_SORT =
            Comparator.comparingLong(s -> s.chars().takeWhile(c -> c == '#').count());

    public SimpleRoster(User owner, String rosterBody, Visibility visibility) {
        this(null, extractTitle(rosterBody), rosterBody, owner, visibility, null, null);
    }

    public static String extractTitle(String rosterBody) {
        return rosterBody
                .lines()
                .filter(line -> line.startsWith("#"))
                .min(HEADER_SORT)
                .map(s -> s.replaceAll("^#+\\s*", ""))
                .orElse("My Roster");
    }

    public View view() {
        return new View(id, title, body, owner.view());
    }

    @Override
    public String ownerId() {
        return owner.id();
    }

    @Json
    public record View(String id, String title, String body, User.Info owner) {}
}
