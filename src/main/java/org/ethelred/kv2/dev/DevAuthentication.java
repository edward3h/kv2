/* (C) Edward Harman and contributors 2023 */
package org.ethelred.kv2.dev;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Order;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.AuthenticationProvider;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.oauth2.endpoint.token.response.OauthAuthenticationMapper;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Map;
import org.ethelred.kv2.services.UserService;
import org.ethelred.kv2.viewmodels.UIAuthProvider;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

@Singleton
@Requires(env = "dev")
@Order(50)
public class DevAuthentication implements AuthenticationProvider, UIAuthProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevAuthentication.class);
    private final UserService userService;

    public DevAuthentication(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Publisher<AuthenticationResponse> authenticate(
            HttpRequest<?> httpRequest, AuthenticationRequest<?, ?> authenticationRequest) {
        LOGGER.debug("Using dev auth");
        return Flux.just(userService.identityToInternalUser(AuthenticationResponse.success(
                authenticationRequest.getIdentity().toString(),
                List.of("ROLE_USER"),
                Map.of(OauthAuthenticationMapper.PROVIDER_KEY, "dev"))));
    }

    @Override
    public String display() {
        return "Dev mode login";
    }

    @Override
    public String path() {
        return "/dev/login";
    }
}
