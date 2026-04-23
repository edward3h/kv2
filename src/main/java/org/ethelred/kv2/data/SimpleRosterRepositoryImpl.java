/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.data;

import jakarta.inject.Singleton;
import java.util.List;
import java.util.Optional;
import org.ethelred.kv2.models.DocumentStub;
import org.ethelred.kv2.models.SimpleRoster;
import org.ethelred.kv2.models.User;
import org.ethelred.kv2.models.Visibility;
import org.ethelred.kv2.services.IdGenerator;

@Singleton
class SimpleRosterRepositoryImpl implements SimpleRosterRepository {
    private final SimpleRosterDao dao;
    private final IdGenerator idGenerator;

    SimpleRosterRepositoryImpl(SimpleRosterDao dao, IdGenerator idGenerator) {
        this.dao = dao;
        this.idGenerator = idGenerator;
    }

    @Override
    public List<DocumentStub> findByOwner(String ownerId) {
        return dao.findByOwner(ownerId);
    }

    @Override
    public Optional<SimpleRoster> findById(String id) {
        return dao.findRosterRowById(id).map(this::toRoster);
    }

    @Override
    public SimpleRoster save(SimpleRoster roster) {
        var id = idGenerator.generate();
        dao.insertRoster(
                id,
                roster.title(),
                roster.body(),
                roster.ownerId(),
                roster.visibility().name());
        return dao.findRosterRowById(id).map(this::toRoster).orElseThrow();
    }

    @Override
    public SimpleRoster update(SimpleRoster roster) {
        dao.updateRoster(
                roster.id(), roster.title(), roster.body(), roster.visibility().name());
        return dao.findRosterRowById(roster.id()).map(this::toRoster).orElseThrow();
    }

    @Override
    public void deleteById(String id) {
        dao.deleteById(id);
    }

    private SimpleRoster toRoster(SimpleRosterDao.RosterRow row) {
        var owner =
                new User(row.ownerId(), row.ownerDisplayName(), row.ownerPictureUrl(), row.ownerFlags(), null, null);
        return new SimpleRoster(
                row.id(),
                row.title(),
                row.body(),
                owner,
                Visibility.valueOf(row.visibility()),
                row.createdAt(),
                row.updatedAt());
    }
}
