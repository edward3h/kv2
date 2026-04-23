/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.data;

import java.util.Optional;
import org.ethelred.kiwiproc.annotation.DAO;
import org.ethelred.kiwiproc.annotation.SqlQuery;
import org.ethelred.kiwiproc.annotation.SqlUpdate;
import org.ethelred.kv2.models.User;
import org.jspecify.annotations.Nullable;

@DAO
interface UserDao {
    @SqlQuery(
            """
            SELECT id, display_name, picture_url, flags, created_at, updated_at
            FROM user
            WHERE id = :id
            """)
    Optional<User> findById(String id);

    @SqlQuery(
            """
            SELECT u.id, u.display_name, u.picture_url, u.flags, u.created_at, u.updated_at
            FROM user u
            JOIN identity i ON u.id = i.user_id
            WHERE i.provider = :provider
            AND i.external_id = :externalId
            """)
    Optional<User> findByIdentity(String provider, String externalId);

    @SqlUpdate(
            """
            INSERT INTO user (id, display_name, picture_url, flags)
            VALUES (:id, :displayName, :pictureUrl, :flags)
            """)
    boolean insertUser(String id, @Nullable String displayName, @Nullable String pictureUrl, int flags);
}
