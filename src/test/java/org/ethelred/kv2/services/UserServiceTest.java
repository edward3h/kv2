/* (C) Edward Harman and contributors 2022-2023 */
package org.ethelred.kv2.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.oauth2.endpoint.token.response.OauthAuthenticationMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@MicronautTest
public class UserServiceTest {

    @Inject
    UserService userService;

    @Test
    public void testCreate() {
        var result = userService.identityToInternalUser(AuthenticationResponse.success(
                "12345",
                List.of("ROLE_USER"),
                Map.of(OauthAuthenticationMapper.PROVIDER_KEY, "usTest", "name", "username")));
        var optionalAuth = result.getAuthentication();
        Assertions.assertTrue(optionalAuth.isPresent());
        var auth = optionalAuth.get();
        var user = userService.userFromAuthentication(auth);
        assertEquals("username", user.displayName());
    }
}
