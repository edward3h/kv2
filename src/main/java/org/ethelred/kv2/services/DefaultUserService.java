/* (C) Edward Harman and contributors 2022 */
package org.ethelred.kv2.services;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.authentication.*;
import io.micronaut.security.oauth2.endpoint.token.response.*;
import jakarta.inject.*;
import java.util.*;
import org.ethelred.kv2.data.*;
import org.ethelred.kv2.models.*;
import org.slf4j.*;

@Singleton
public class DefaultUserService implements UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultUserService.class);
    private final IdentityRepository identityRepository;
    private final UserRepository userRepository;

    public DefaultUserService(IdentityRepository identityRepository, UserRepository userRepository) {
        this.identityRepository = identityRepository;
        this.userRepository = userRepository;
    }

    @Override
    public AuthenticationResponse identityToInternalUser(AuthenticationResponse identityResponse) {
        var checkAuth = identityResponse.getAuthentication();
        if (checkAuth.isEmpty()) {
            return identityResponse;
        }

        var auth = checkAuth.get();
        var provider = auth.getAttributes().get(OauthAuthenticationMapper.PROVIDER_KEY);
        var externalId = auth.getName();
        var user = userRepository
                .findByIdentity((String) provider, externalId)
                .orElse(createUser(auth, (String) provider, externalId));
        return AuthenticationResponse.success(user.id(), user.roles(), user.attributes());
    }

    private static final List<String> DISPLAY_NAME_KEYS = List.of("name", "username", "email");

    private User createUser(Authentication auth, String provider, String externalId) {
        var displayName = DISPLAY_NAME_KEYS.stream()
                .filter(auth.getAttributes()::containsKey)
                .map(auth.getAttributes()::get)
                .map(String::valueOf)
                .findFirst()
                .orElse("Unknown");
        var user = userRepository.save(
                new User(null, displayName, (String) auth.getAttributes().get("picture"), UserFlag.ROLE_USER));
        LOGGER.info("User attributes {}", auth.getAttributes());
        identityRepository.save(new Identity(
                provider, user, externalId, (String) auth.getAttributes().get("email"), auth.getAttributes()));
        return user;
    }

    @Override
    public User userFromAuthentication(Authentication auth) {
        if (auth == null || !auth.getAttributes().containsKey("user")) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "No user in request");
        }
        return (User) auth.getAttributes().get("user");
    }
}
