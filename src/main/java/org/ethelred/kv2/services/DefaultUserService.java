/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.services;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;
import jakarta.inject.Singleton;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.ethelred.kiwiproc.exception.UncheckedSQLException;
import org.ethelred.kv2.data.UserRepository;
import org.ethelred.kv2.models.User;
import org.ethelred.kv2.models.UserFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DefaultUserService implements UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultUserService.class);
    private static final List<String> DISPLAY_NAME_KEYS = List.of("name", "username", "email");

    private final UserRepository userRepository;
    private final JsonType<Map<String, Object>> mapType;

    public DefaultUserService(UserRepository userRepository, Jsonb jsonb) {
        this.userRepository = userRepository;
        this.mapType = jsonb.type(Types.mapOf(Object.class));
    }

    @Override
    public User findOrCreateUser(String provider, String externalId, Map<String, Object> attributes) {
        return userRepository
                .findByIdentity(provider, externalId)
                .orElseGet(() -> createUser(provider, externalId, attributes));
    }

    @SuppressWarnings("nullness")
    private User createUser(String provider, String externalId, Map<String, Object> attributes) {
        var displayName = DISPLAY_NAME_KEYS.stream()
                .filter(attributes::containsKey)
                .map(k -> String.valueOf(attributes.get(k)))
                .findFirst()
                .orElse("Unknown");
        LOGGER.info("User attributes {}", attributes);
        String attributesJson;
        try {
            attributesJson = mapType.toJson(attributes);
        } catch (Exception e) {
            attributesJson = "{}";
        }
        var email = (String) attributes.get("email");
        try {
            return userRepository.saveWithIdentity(
                    new User(null, displayName, (String) attributes.get("picture"), UserFlag.ROLE_USER),
                    provider,
                    externalId,
                    email,
                    attributesJson);
        } catch (UncheckedSQLException e) {
            // A concurrent first login for the same identity may have created it since our lookup.
            if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
                return userRepository.findByIdentity(provider, externalId).orElseThrow(() -> e);
            }
            throw e;
        }
    }

    @Override
    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }
}
