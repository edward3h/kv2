/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.avaje.inject.BeanScope;
import org.ethelred.kv2.MySQLContainerExtension;
import org.ethelred.kv2.models.Identity;
import org.ethelred.kv2.models.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MySQLContainerExtension.class)
public class UserRepositoryTest {

    @Test
    public void findUserSuccess() {
        try (var scope = BeanScope.builder().profiles("test").build()) {
            var userRepository = scope.get(UserRepository.class);
            var identityDao = scope.get(IdentityDao.class);

            var user1 = new User(null, "Bob", null);
            user1 = userRepository.save(user1);
            var id1 = new Identity("facespace", user1, "12345", null, "{}");
            identityDao.insertIdentity(
                    id1.id(), id1.provider(), id1.userId(), id1.externalId(), id1.email(), id1.attributes());

            var found = userRepository.findByIdentity("facespace", "12345");
            assertTrue(found.isPresent(), "User was found");
            assertEquals("Bob", found.get().displayName());
            assertEquals(user1.id(), found.get().id());
        }
    }
}
