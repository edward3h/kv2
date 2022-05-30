/* (C) Edward Harman and contributors 2022 */
package org.ethelred.kv2.providers;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.core.annotation.*;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.config.*;
import io.micronaut.security.oauth2.configuration.*;
import io.micronaut.security.oauth2.endpoint.authorization.state.State;
import io.micronaut.security.oauth2.endpoint.token.response.*;
import jakarta.inject.Singleton;
import org.ethelred.kv2.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author edward
 */
@Singleton
@Replaces(DefaultOpenIdAuthenticationMapper.class)
public class OpenIdUserAuthenticationMapper extends DefaultOpenIdAuthenticationMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenIdUserAuthenticationMapper.class);
    private final UserService userService;

    public OpenIdUserAuthenticationMapper(
            OpenIdAdditionalClaimsConfiguration openIdAdditionalClaimsConfiguration,
            AuthenticationModeConfiguration authenticationModeConfiguration,
            UserService userService) {
        super(openIdAdditionalClaimsConfiguration, authenticationModeConfiguration);
        this.userService = userService;
    }

    @Override
    @NonNull
    public AuthenticationResponse createAuthenticationResponse(
            String providerName, OpenIdTokenResponse tokenResponse, OpenIdClaims openIdClaims, State state) {
        var r = super.createAuthenticationResponse(providerName, tokenResponse, openIdClaims, state);

        return userService.identityToInternalUser(r);
    }
}
