/* (C) Edward Harman and contributors 2022-2023 */
package org.ethelred.kv2.controllers;

import io.micronaut.core.annotation.*;
import io.micronaut.http.*;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.*;
import io.micronaut.security.authentication.*;
import io.micronaut.security.rules.*;
import io.micronaut.views.*;
import java.security.Principal;
import java.util.*;
import org.ethelred.kv2.viewmodels.HomeContext;

/** @author edward */
@Controller
public class HomeController {

    @Secured(SecurityRule.IS_ANONYMOUS)
    @Get
    @View("home")
    public HttpResponse<?> index(@Nullable Authentication auth) {
        var o = Optional.ofNullable(auth);
        return HttpResponse.ok(Map.of("context", new HomeContext() {
            @Override
            public String title() {
                return "Home";
            }

            @Override
            public boolean loggedIn() {
                return o.isPresent();
            }

            @Override
            public String username() {
                return o.map(Principal::getName).orElse("Anonymous");
            }
        }));
    }
}
