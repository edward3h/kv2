/* (C) Edward Harman and contributors 2022-2023 */
package org.ethelred.kv2.controllers;

import static org.ethelred.kv2.util.ViewsHelper.writable;

import io.micronaut.core.annotation.*;
import io.micronaut.core.io.Writable;
import io.micronaut.http.*;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.*;
import io.micronaut.security.authentication.*;
import io.micronaut.security.rules.*;
import io.micronaut.views.*;
import java.util.*;
import org.ethelred.kv2.models.DefaultLayoutContext;
import org.ethelred.kv2.models.Owner;
import org.ethelred.kv2.template.Templates;
import org.ethelred.kv2.util.Debug;
import org.ethelred.roster.RosterParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author edward
 */
@Controller(produces = MediaType.TEXT_HTML)
@Debug
@Secured({"ROLE_USER"})
public class UIRosterController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UIRosterController.class);

    private final ApiRosterController delegate;
    private final RosterParser parser;
    private final Templates templates;

    public UIRosterController(ApiRosterController delegate, RosterParser parser, Templates templates) {
        this.delegate = delegate;
        this.parser = parser;
        this.templates = templates;
        LOGGER.debug("templates is {}", templates);
    }

    @Secured(SecurityRule.IS_ANONYMOUS)
    @Get
    public Writable index(@Nullable Authentication auth) {
        return writable(templates.layout(
                new DefaultLayoutContext("Home", auth),
                templates.home(auth != null, delegate.userRosters(asOwner(auth)))));
    }

    @Secured(SecurityRule.IS_ANONYMOUS)
    @Get("/roster/{id}")
    public Writable viewRoster(@Nullable Authentication auth, @PathVariable String id) {
        var roster = delegate.getRoster(asOwner(auth), id);
        var parsed = parser.parseRoster(roster.body());
        return writable(templates.layout(new DefaultLayoutContext(roster.title(), auth), templates.roster(parsed)));
    }

    @Post(value = "/roster/new", consumes = MediaType.APPLICATION_FORM_URLENCODED)
    public HttpResponse<?> newRoster(Authentication auth) {
        var roster = delegate.createRoster(asOwner(auth), "# New Roster\n");
        return HttpResponse.seeOther(HttpResponse.uri("/roster/%s/edit".formatted(roster.id())));
    }

    @Get("/roster/{id}/edit")
    public Writable editRoster(Authentication auth, @PathVariable String id) {
        var roster = delegate.getRoster(asOwner(auth), id);
        return writable(templates.layout(
                new DefaultLayoutContext(roster.title(), auth), templates.rosterEdit(roster.id(), roster.body())));
    }

    private @Nullable Owner asOwner(@Nullable Authentication auth) {
        return auth == null ? null : auth::getName;
    }
}
