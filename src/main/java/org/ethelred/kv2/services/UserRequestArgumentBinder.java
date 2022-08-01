/* (C) Edward Harman and contributors 2022 */
package org.ethelred.kv2.services;

import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.bind.binders.TypedRequestArgumentBinder;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.filters.SecurityFilter;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Optional;
import org.ethelred.kv2.models.User;

@Singleton
public class UserRequestArgumentBinder implements TypedRequestArgumentBinder<User> {
    private static final Argument<User> TYPE = Argument.of(User.class);

    private final UserService userService;

    @Inject
    public UserRequestArgumentBinder(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Argument<User> argumentType() {
        return TYPE;
    }

    @SuppressWarnings("unchecked")
    @Override
    public BindingResult<User> bind(ArgumentConversionContext<User> context, HttpRequest<?> source) {
        if (!source.getAttributes().contains(SecurityFilter.KEY)) {
            return BindingResult.UNSATISFIED;
        }

        final Optional<Authentication> existing = source.getUserPrincipal(Authentication.class);
        return existing.isPresent() ? (() -> existing.map(userService::userFromAuthentication)) : BindingResult.EMPTY;
    }
}
