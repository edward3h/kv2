/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.avaje.inject.BeanScope;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class UserServiceTest {

    @Test
    public void testCreate() {
        try (var scope = BeanScope.builder().profiles("test").build()) {
            var userService = scope.get(UserService.class);

            var user = userService.findOrCreateUser("usTest", "12345", Map.of("name", "username"));
            assertEquals("username", user.displayName());

            var found = userService.findById(user.id());
            assertEquals("username", found.get().displayName());
        }
    }
}
