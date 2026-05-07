/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.controllers;

import static org.ethelred.kv2.util.StreamingOutputAdapter.streaming;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Post;
import io.avaje.http.api.Produces;
import io.avaje.http.api.StreamingOutput;
import io.avaje.jex.http.Context;
import jakarta.inject.Singleton;
import java.util.List;
import org.ethelred.kv2.models.DefaultLayoutContext;
import org.ethelred.kv2.security.AuthFilter;
import org.ethelred.kv2.template.Templates;
import org.ethelred.kv2.viewmodels.Assets;
import org.ethelred.kv2.viewmodels.UIAuthProvider;
import org.ethelred.roster.RosterParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@Singleton
public class UIRosterController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UIRosterController.class);

    private final ApiRosterController delegate;
    private final RosterParser parser;
    private final Templates templates;
    private final List<UIAuthProvider> authProviders;
    private final Assets assets;

    public UIRosterController(
            ApiRosterController delegate,
            RosterParser parser,
            Templates templates,
            List<UIAuthProvider> authProviders,
            Assets assets) {
        this.delegate = delegate;
        this.parser = parser;
        this.templates = templates;
        this.authProviders = authProviders;
        this.assets = assets;
        LOGGER.debug("templates is {}", templates);
    }

    @Get("/")
    @Produces("text/html")
    public StreamingOutput index(Context ctx) {
        var principal = AuthFilter.getPrincipal(ctx);
        var owner = AuthFilter.getOwner(ctx);
        return streaming(templates.layout(
                assets,
                new DefaultLayoutContext("Home", principal, authProviders),
                templates.home(principal != null, delegate.userRostersFor(owner))));
    }

    @Get("/roster/{id}")
    @Produces("text/html")
    public StreamingOutput viewRoster(Context ctx, String id) {
        var principal = AuthFilter.getPrincipal(ctx);
        var roster = delegate.getRosterFor(AuthFilter.getOwner(ctx), id);
        var parsed = parser.parseRoster(roster.body());
        return streaming(templates.layout(
                assets, new DefaultLayoutContext(roster.title(), principal, authProviders), templates.roster(parsed)));
    }

    @Post("/roster/new")
    public void newRoster(Context ctx) {
        var owner = AuthFilter.getOwner(ctx);
        var roster = delegate.createRosterFor(owner, "# New Roster\n");
        ctx.redirect("/roster/%s/edit".formatted(roster.id()));
    }

    @Get("/roster/{id}/edit")
    @Produces("text/html")
    public StreamingOutput editRoster(Context ctx, String id) {
        var principal = AuthFilter.getPrincipal(ctx);
        var roster = delegate.getRosterFor(AuthFilter.getOwner(ctx), id);
        return streaming(templates.layout(
                assets,
                new DefaultLayoutContext(roster.title(), principal, authProviders),
                templates.rosterEdit(roster.id(), roster.body())));
    }
}
