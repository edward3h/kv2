package org.ethelred.kv2.controllers;

import static io.micronaut.http.HttpResponse.ok;
import static io.micronaut.http.MediaType.TEXT_HTML;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.views.rocker.RockerWritable;
import java.util.Map;
import java.util.Optional;
import views.home;

/** @author edward */
@Controller
public class HomeController {

    @Secured(SecurityRule.IS_ANONYMOUS)
    @Produces(TEXT_HTML)
    @Get
    public HttpResponse<?> index(@Nullable Authentication auth) {
        var o = Optional.ofNullable(auth);
        return ok(new RockerWritable(home.template(
                auth != null,
                o.map(Authentication::getName),
                o.map(Authentication::getAttributes).orElse(Map.of()))));
    }
}
