package org.ethelred.kv2.controllers;

import io.micronaut.core.annotation.*;
import io.micronaut.http.*;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.*;
import io.micronaut.security.authentication.*;
import io.micronaut.security.rules.*;
import io.micronaut.views.*;
import java.util.*;

/** @author edward */
@Controller
public class HomeController {

    @Secured(SecurityRule.IS_ANONYMOUS)
    @Get
    @View("home")
    public HttpResponse<?> index(@Nullable Authentication auth) {
        var o = Optional.ofNullable(auth);
        return HttpResponse.ok(Map.of(
                "loggedIn", o.isPresent(),
                "name", o.map(a -> a.getAttributes().get("displayName")),
                "attributes", o.map(Authentication::getAttributes).orElse(Map.of())));
    }
}
