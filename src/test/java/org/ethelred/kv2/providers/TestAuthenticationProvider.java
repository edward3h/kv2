/* (C) Edward Harman and contributors 2022-2025 */
package org.ethelred.kv2.providers;

import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.AuthenticationProvider;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.oauth2.endpoint.token.response.OauthAuthenticationMapper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Map;
import org.ethelred.kv2.services.UserService;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

@Singleton
public class TestAuthenticationProvider implements AuthenticationProvider {
    @Inject
    UserService userService;

    @Override
    public Publisher<AuthenticationResponse> authenticate(
            HttpRequest<?> httpRequest, AuthenticationRequest<?, ?> authenticationRequest) {
        return Flux.just(userService.identityToInternalUser(AuthenticationResponse.success(
                String.valueOf(authenticationRequest.getIdentity()),
                List.of("ROLE_USER"),
                Map.of(OauthAuthenticationMapper.PROVIDER_KEY, "test"))));
    }
}
