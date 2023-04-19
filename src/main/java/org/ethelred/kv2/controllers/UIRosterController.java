/* (C) Edward Harman and contributors 2022-2023 */
package org.ethelred.kv2.controllers;

import io.micronaut.core.annotation.*;
import io.micronaut.http.*;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.*;
import io.micronaut.security.authentication.*;
import io.micronaut.security.rules.*;
import io.micronaut.views.*;
import java.util.*;
import org.ethelred.kv2.models.DefaultLayoutContext;
import org.ethelred.kv2.models.Owner;
import org.ethelred.kv2.util.Debug;
import org.ethelred.kv2.viewmodels.HomeContext;
import org.ethelred.kv2.viewmodels.RosterContext;
import org.ethelred.kv2.viewmodels.RosterView;
import org.ethelred.kv2.viewmodels.StubView;
import org.ethelred.roster.ParsedRoster;
import org.ethelred.roster.RosterParser;

/**
 * @author edward
 */
@Controller
@Debug
public class UIRosterController {
    private final ApiRosterController delegate;
    private final RosterParser parser;

    public UIRosterController(ApiRosterController delegate, RosterParser parser) {
        this.delegate = delegate;
        this.parser = parser;
    }

    @Secured(SecurityRule.IS_ANONYMOUS)
    @Get
    @View("home")
    public HttpResponse<?> index(@Nullable Authentication auth) {
        var o = Optional.ofNullable(auth);
        return HttpResponse.ok(Map.of(
                "context",
                new HomeContext() {
                    @Override
                    public List<? extends StubView> rosters() {
                        return delegate.userRosters(asOwner(auth));
                    }
                },
                "layoutContext",
                new DefaultLayoutContext("Home", auth)));
    }

    @Secured(SecurityRule.IS_ANONYMOUS)
    @Get("/roster/{id}")
    @View("roster")
    public HttpResponse<?> viewRoster(@Nullable Authentication auth, @PathVariable String id) {
        var roster = delegate.getRoster(asOwner(auth), id);
        var parsed = parser.parseRoster(roster.body());
        return HttpResponse.ok(Map.of(
                "roster",
                new RosterContext() {
                    @Override
                    public RosterView roster() {
                        return roster;
                    }

                    @Override
                    public ParsedRoster parsed() {
                        return parsed;
                    }
                },
                "layout",
                new DefaultLayoutContext(roster.title(), auth)));
    }

    private @Nullable Owner asOwner(@Nullable Authentication auth) {
        return auth == null ? null : auth::getName;
    }
}
