/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.avaje.inject.BeanScope;
import org.ethelred.kv2.models.Identity;
import org.ethelred.kv2.models.User;
import org.junit.jupiter.api.Test;

public class UserRepositoryTest {

    @Test
    public void findUserSuccess() {
        try (var scope = BeanScope.builder().profiles("test").build()) {
            var userRepository = scope.get(UserRepository.class);
            var identityRepository = scope.get(IdentityRepository.class);

            var user1 = new User(null, "Bob", null);
            user1 = userRepository.save(user1);
            var id1 = new Identity("facespace", user1, "12345", null, "{}");
            identityRepository.save(id1);

            var found = userRepository.findByIdentity("facespace", "12345");
            assertTrue(found.isPresent(), "User was found");
            assertEquals("Bob", found.get().displayName());
            assertEquals(user1.id(), found.get().id());
        }
    }
}
