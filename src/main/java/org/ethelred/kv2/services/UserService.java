/* (C) Edward Harman and contributors 2022 */
package org.ethelred.kv2.services;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.security.authentication.*;
import java.util.Optional;
import org.ethelred.kv2.models.User;

public interface UserService {
    AuthenticationResponse identityToInternalUser(AuthenticationResponse identityResponse);

    User userFromAuthentication(@Nullable Authentication auth);

    Optional<User> findById(String id);
}
