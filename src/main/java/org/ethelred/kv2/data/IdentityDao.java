/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.data;

import org.ethelred.kiwiproc.annotation.DAO;
import org.ethelred.kiwiproc.annotation.SqlUpdate;
import org.ethelred.kv2.models.Identity;

@DAO(autoCommit = true)
public interface IdentityDao {
    @SqlUpdate("""
            INSERT INTO identity (id, provider, user_id, external_id, email, attributes)
            VALUES (:id, :provider, :userId, :externalId, :email, :attributes)
            """)
    boolean insertIdentity(Identity identity);
}
