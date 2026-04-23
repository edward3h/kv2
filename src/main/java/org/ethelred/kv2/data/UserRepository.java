/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.data;

import java.util.Optional;
import org.ethelred.kv2.models.User;

public interface UserRepository {
    Optional<User> findById(String id);

    Optional<User> findByIdentity(String provider, String externalId);

    User save(User user);
}
