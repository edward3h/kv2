package org.ethelred.kv2.data;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.util.Map;
import org.ethelred.kv2.models.Identity;
import org.ethelred.kv2.models.User;
import org.ethelred.kv2.services.IdGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@MicronautTest
public class UserRepositoryTest {
    @Inject
    UserRepository userRepository;

    @Inject
    IdentityRepository identityRepository;

    @Inject
    IdGenerator idGenerator;

    @Test
    public void findUserSuccess() {
        var user1 = new User(idGenerator.generate(), "Bob");
        var id1 = new Identity(idGenerator.generate(), "facespace", user1, "12345", null, Map.of(), null, null);
        userRepository.save(user1);
        identityRepository.save(id1);

        var found = userRepository.findByIdentity("facespace", "12345");
        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals("Bob", found.get().displayName());
    }
}
