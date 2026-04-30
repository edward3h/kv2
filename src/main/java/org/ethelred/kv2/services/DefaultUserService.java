/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.services;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.ethelred.kv2.data.IdentityRepository;
import org.ethelred.kv2.data.UserRepository;
import org.ethelred.kv2.models.Identity;
import org.ethelred.kv2.models.User;
import org.ethelred.kv2.models.UserFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DefaultUserService implements UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultUserService.class);
    private static final List<String> DISPLAY_NAME_KEYS = List.of("name", "username", "email");

    private final IdentityRepository identityRepository;
    private final UserRepository userRepository;
    private final JsonType<Map<String, Object>> mapType;

    public DefaultUserService(IdentityRepository identityRepository, UserRepository userRepository, Jsonb jsonb) {
        this.identityRepository = identityRepository;
        this.userRepository = userRepository;
        this.mapType = jsonb.type(Types.mapOf(Object.class));
    }

    @Override
    public User findOrCreateUser(String provider, String externalId, Map<String, Object> attributes) {
        return userRepository
                .findByIdentity(provider, externalId)
                .orElseGet(() -> createUser(provider, externalId, attributes));
    }

    private User createUser(String provider, String externalId, Map<String, Object> attributes) {
        var displayName = DISPLAY_NAME_KEYS.stream()
                .filter(attributes::containsKey)
                .map(k -> String.valueOf(attributes.get(k)))
                .findFirst()
                .orElse("Unknown");
        var user = userRepository.save(
                new User(null, displayName, (String) attributes.get("picture"), UserFlag.ROLE_USER));
        LOGGER.info("User attributes {}", attributes);
        String attributesJson;
        try {
            attributesJson = mapType.toJson(attributes);
        } catch (Exception e) {
            attributesJson = "{}";
        }
        identityRepository.save(
                new Identity(provider, user, externalId, (String) attributes.get("email"), attributesJson));
        return user;
    }

    @Override
    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }
}
