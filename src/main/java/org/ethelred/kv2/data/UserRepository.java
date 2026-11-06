/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.data;

import java.util.Optional;
import org.ethelred.kv2.models.User;
import org.jspecify.annotations.Nullable;

public interface UserRepository {
    Optional<User> findById(String id);

    Optional<User> findByIdentity(String provider, String externalId);

    User save(User user);

    /**
     * Saves a new user and its identity in a single transaction.
     *
     * @throws org.ethelred.kiwiproc.exception.UncheckedSQLException if either insert fails; neither is kept
     */
    User saveWithIdentity(
            User user, String provider, String externalId, @Nullable String email, @Nullable String attributes);
}
