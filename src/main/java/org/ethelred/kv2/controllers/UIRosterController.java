/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.controllers;

import static org.ethelred.kv2.util.ViewsHelper.render;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Post;
import io.avaje.http.api.Produces;
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
@Produces("text/html")
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
    public String index(Context ctx) {
        var principal = AuthFilter.getPrincipal(ctx);
        var owner = AuthFilter.getOwner(ctx);
        return render(templates.layout(
                assets,
                new DefaultLayoutContext("Home", principal, authProviders),
                templates.home(principal != null, delegate.userRostersFor(owner))));
    }

    @Get("/roster/{id}")
    public String viewRoster(Context ctx, String id) {
        var principal = AuthFilter.getPrincipal(ctx);
        var roster = delegate.getRosterFor(AuthFilter.getOwner(ctx), id);
        var parsed = parser.parseRoster(roster.body());
        return render(templates.layout(
                assets, new DefaultLayoutContext(roster.title(), principal, authProviders), templates.roster(parsed)));
    }

    @Post("/roster/new")
    public void newRoster(Context ctx) {
        var owner = AuthFilter.getOwner(ctx);
        var roster = delegate.createRosterFor(owner, "# New Roster\n");
        ctx.redirect("/roster/%s/edit".formatted(roster.id()));
    }

    @Get("/roster/{id}/edit")
    public String editRoster(Context ctx, String id) {
        var principal = AuthFilter.getPrincipal(ctx);
        var roster = delegate.getRosterFor(AuthFilter.getOwner(ctx), id);
        return render(templates.layout(
                assets,
                new DefaultLayoutContext(roster.title(), principal, authProviders),
                templates.rosterEdit(roster.id(), roster.body())));
    }
}
