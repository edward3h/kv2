/* (C) Edward Harman and contributors 2022 */
package org.ethelred.kv2.services;

import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.bind.binders.TypedRequestArgumentBinder;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.filters.SecurityFilter;
import jakarta.inject.Singleton;
import java.util.Optional;
import org.ethelred.kv2.models.Owner;

@Singleton
public class OwnerRequestArgumentBinder implements TypedRequestArgumentBinder<Owner> {
    private static final Argument<Owner> TYPE = Argument.of(Owner.class);

    @Override
    public Argument<Owner> argumentType() {
        return TYPE;
    }

    @SuppressWarnings("unchecked")
    @Override
    public BindingResult<Owner> bind(ArgumentConversionContext<Owner> context, HttpRequest<?> source) {
        if (!source.getAttributes().contains(SecurityFilter.KEY)) {
            return BindingResult.UNSATISFIED;
        }

        final Optional<Authentication> existing = source.getUserPrincipal(Authentication.class);
        return existing.isPresent()
                ? (() -> existing.map(authentication -> new AuthenticationOwner(authentication.getName())))
                : BindingResult.EMPTY;
    }

    private record AuthenticationOwner(String id) implements Owner {}
}
