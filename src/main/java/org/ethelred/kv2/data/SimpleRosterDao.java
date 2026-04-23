/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.ethelred.kiwiproc.annotation.DAO;
import org.ethelred.kiwiproc.annotation.SqlQuery;
import org.ethelred.kiwiproc.annotation.SqlUpdate;
import org.ethelred.kv2.models.DocumentStub;
import org.jspecify.annotations.Nullable;

@DAO
interface SimpleRosterDao {
    record RosterRow(
            String id,
            String title,
            String body,
            String ownerId,
            @Nullable String ownerDisplayName,
            @Nullable String ownerPictureUrl,
            int ownerFlags,
            String visibility,
            @Nullable LocalDateTime createdAt,
            @Nullable LocalDateTime updatedAt) {}

    @SqlQuery(
            """
            SELECT id, title
            FROM simple_roster
            WHERE owner_id = :ownerId
            """)
    List<DocumentStub> findByOwner(String ownerId);

    @SqlQuery(
            """
            SELECT sr.id, sr.title, sr.body, sr.visibility, sr.created_at, sr.updated_at,
                   u.id AS owner_id, u.display_name AS owner_display_name,
                   u.picture_url AS owner_picture_url, u.flags AS owner_flags
            FROM simple_roster sr
            JOIN user u ON sr.owner_id = u.id
            WHERE sr.id = :id
            """)
    Optional<RosterRow> findRosterRowById(String id);

    @SqlUpdate(
            """
            INSERT INTO simple_roster (id, title, body, owner_id, visibility)
            VALUES (:id, :title, :body, :ownerId, :visibility)
            """)
    boolean insertRoster(String id, String title, String body, String ownerId, String visibility);

    @SqlUpdate(
            """
            UPDATE simple_roster
            SET title = :title, body = :body, visibility = :visibility
            WHERE id = :id
            """)
    boolean updateRoster(String id, String title, String body, String visibility);

    @SqlUpdate("""
            DELETE FROM simple_roster
            WHERE id = :id
            """)
    boolean deleteById(String id);
}
