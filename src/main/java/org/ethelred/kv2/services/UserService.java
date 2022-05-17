package org.ethelred.kv2.services;

import io.micronaut.security.authentication.*;

public interface UserService {
    AuthenticationResponse identityToInternalUser(AuthenticationResponse identityResponse);
}
