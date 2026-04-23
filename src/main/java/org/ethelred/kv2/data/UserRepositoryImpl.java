/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.data;

import jakarta.inject.Singleton;
import java.util.Optional;
import org.ethelred.kv2.models.User;
import org.ethelred.kv2.services.IdGenerator;

@Singleton
class UserRepositoryImpl implements UserRepository {
    private final UserDao dao;
    private final IdGenerator idGenerator;

    UserRepositoryImpl(UserDao dao, IdGenerator idGenerator) {
        this.dao = dao;
        this.idGenerator = idGenerator;
    }

    @Override
    public Optional<User> findById(String id) {
        return dao.findById(id);
    }

    @Override
    public Optional<User> findByIdentity(String provider, String externalId) {
        return dao.findByIdentity(provider, externalId);
    }

    @Override
    public User save(User user) {
        var id = idGenerator.generate();
        dao.insertUser(id, user.displayName(), user.pictureUrl(), user.flags());
        return dao.findById(id).orElseThrow();
    }
}
