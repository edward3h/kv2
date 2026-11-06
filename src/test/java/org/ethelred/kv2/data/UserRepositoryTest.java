/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.avaje.inject.BeanScope;
import javax.sql.DataSource;
import org.ethelred.kiwiproc.exception.UncheckedSQLException;
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
            var userDao = scope.get(UserDao.class);

            var user1 = new User(null, "Bob", null);
            user1 = userRepository.save(user1);
            var id1 = new Identity("facespace", user1, "12345", null, "{}");
            userDao.insertIdentity(id1);

            var found = userRepository.findByIdentity("facespace", "12345");
            assertTrue(found.isPresent(), "User was found");
            assertEquals("Bob", found.get().displayName());
            assertEquals(user1.id(), found.get().id());
        }
    }

    @Test
    public void duplicateIdentityRollsBackUser() throws Exception {
        try (var scope = BeanScope.builder().profiles("test").build()) {
            var userRepository = scope.get(UserRepository.class);
            var dataSource = scope.get(DataSource.class, "default");

            var first =
                    userRepository.saveWithIdentity(new User(null, "Alice", null), "facespace", "dup-1", null, "{}");
            var before = countUsers(dataSource);

            assertThrows(
                    UncheckedSQLException.class,
                    () -> userRepository.saveWithIdentity(
                            new User(null, "Alice again", null), "facespace", "dup-1", null, "{}"));

            assertEquals(before, countUsers(dataSource), "failed identity insert must not leave an orphan user");
            assertEquals(
                    first.id(),
                    userRepository
                            .findByIdentity("facespace", "dup-1")
                            .orElseThrow()
                            .id());
        }
    }

    private static int countUsers(DataSource dataSource) throws Exception {
        try (var conn = dataSource.getConnection();
                var st = conn.createStatement();
                var rs = st.executeQuery("SELECT COUNT(*) FROM user")) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
