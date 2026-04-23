/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.data;

import jakarta.inject.Singleton;
import org.ethelred.kv2.models.Identity;

@Singleton
class IdentityRepositoryImpl implements IdentityRepository {
    private final IdentityDao dao;

    IdentityRepositoryImpl(IdentityDao dao) {
        this.dao = dao;
    }

    @Override
    public void save(Identity identity) {
        dao.insertIdentity(
                identity.id(),
                identity.provider(),
                identity.userId(),
                identity.externalId(),
                identity.email(),
                identity.attributes());
    }
}
