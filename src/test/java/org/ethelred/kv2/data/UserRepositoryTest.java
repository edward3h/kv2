/* (C) Edward Harman and contributors 2022 */
package org.ethelred.kv2.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.util.Map;
import org.ethelred.kv2.models.Identity;
import org.ethelred.kv2.models.User;
import org.ethelred.kv2.services.*;
import org.junit.jupiter.api.Test;

@MicronautTest
public class UserRepositoryTest {
    @Inject
    UserRepository userRepository;

    @Inject
    IdentityRepository identityRepository;

    @Test
    public void findUserSuccess() {
        var user1 = new User(null, "Bob", null);
        user1 = userRepository.save(user1);
        var id1 = new Identity("facespace", user1, "12345", null, Map.of());
        identityRepository.save(id1);

        var found = userRepository.findByIdentity("facespace", "12345");
        assertTrue(found.isPresent(), "User was found");
        assertEquals("Bob", found.get().displayName());
        assertEquals(user1.id(), found.get().id());
    }
}
