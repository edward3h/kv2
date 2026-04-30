/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.data;

import org.ethelred.kiwiproc.annotation.DAO;
import org.ethelred.kiwiproc.annotation.SqlUpdate;
import org.jspecify.annotations.Nullable;

@DAO
public interface IdentityDao {
    @SqlUpdate(
            """
            INSERT INTO identity (id, provider, user_id, external_id, email, attributes)
            VALUES (:id, :provider, :userId, :externalId, :email, :attributes)
            """)
    boolean insertIdentity(
            String id,
            String provider,
            String userId,
            String externalId,
            @Nullable String email,
            @Nullable String attributes);
}
