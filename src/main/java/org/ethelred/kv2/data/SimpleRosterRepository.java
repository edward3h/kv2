/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.data;

import java.util.List;
import java.util.Optional;
import org.ethelred.kv2.models.DocumentStub;
import org.ethelred.kv2.models.SimpleRoster;

public interface SimpleRosterRepository {
    List<DocumentStub> findByOwner(String ownerId);

    Optional<SimpleRoster> findById(String id);

    SimpleRoster save(SimpleRoster roster);

    SimpleRoster update(SimpleRoster roster);

    void deleteById(String id);
}
