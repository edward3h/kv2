/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.services;

import java.util.Map;
import java.util.Optional;
import org.ethelred.kv2.models.User;

public interface UserService {
    User findOrCreateUser(String provider, String externalId, Map<String, Object> attributes);

    Optional<User> findById(String id);
}
