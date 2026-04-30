/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.controllers;

import io.avaje.http.api.Consumes;
import io.avaje.http.api.Controller;
import io.avaje.http.api.Delete;
import io.avaje.http.api.Get;
import io.avaje.http.api.Patch;
import io.avaje.http.api.Post;
import io.avaje.jex.http.Context;
import io.avaje.jex.http.HttpResponseException;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.ethelred.kv2.data.SimpleRosterRepository;
import org.ethelred.kv2.models.DocumentStub;
import org.ethelred.kv2.models.Owner;
import org.ethelred.kv2.models.SimpleRoster;
import org.ethelred.kv2.models.Visibility;
import org.ethelred.kv2.security.AuthFilter;
import org.ethelred.kv2.services.UserService;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller("/abc/rosters")
@Singleton
public class ApiRosterController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiRosterController.class);

    private final SimpleRosterRepository rosterRepository;
    private final UserService userService;
    private final JsonType<Map<String, Object>> mapType;

    public ApiRosterController(SimpleRosterRepository rosterRepository, UserService userService, Jsonb jsonb) {
        this.rosterRepository = rosterRepository;
        this.userService = userService;
        this.mapType = jsonb.type(Types.mapOf(Object.class));
    }

    @Get
    public List<DocumentStub> userRosters(Context ctx) {
        return userRostersFor(AuthFilter.getOwner(ctx));
    }

    List<DocumentStub> userRostersFor(@Nullable Owner owner) {
        if (owner == null) {
            return List.of();
        }
        return rosterRepository.findByOwner(owner.id());
    }

    @Get("/{id}")
    public SimpleRoster.View getRoster(Context ctx, String id) {
        return getRosterFor(AuthFilter.getOwner(ctx), id);
    }

    SimpleRoster.View getRosterFor(@Nullable Owner owner, String id) {
        var roster = rosterRepository.findById(id).orElseThrow(() -> new HttpResponseException(404, "Not found"));
        if (roster.isVisibleTo(owner)) {
            return roster.view();
        }
        LOGGER.debug("Private roster {} user {}", roster, owner);
        throw new HttpResponseException(403, "Private roster");
    }

    @Post
    @Consumes("text/plain")
    public SimpleRoster.View createRoster(Context ctx) {
        return createRosterFor(AuthFilter.getOwner(ctx), ctx.body());
    }

    SimpleRoster.View createRosterFor(@Nullable Owner owner, @Nullable String rosterBody) {
        if (owner == null) {
            throw new HttpResponseException(401, "Unauthorized");
        }
        var user = userService.findById(owner.id());
        if (user.isEmpty()) {
            throw new HttpResponseException(404, "User not found");
        }
        var roster = new SimpleRoster(user.get(), Objects.requireNonNullElse(rosterBody, ""), Visibility.PRIVATE);
        return rosterRepository.save(roster).view();
    }

    @Delete("/{id}")
    public void deleteRoster(Context ctx, String id) {
        var owner = AuthFilter.getOwner(ctx);
        var oldRoster = rosterRepository.findById(id).orElseThrow(() -> new HttpResponseException(404, "Not found"));
        if (owner == null || !oldRoster.isOwnedBy(owner)) {
            throw new HttpResponseException(403, "Not owner of this roster");
        }
        rosterRepository.deleteById(id);
    }

    @Patch("/{id}")
    @Consumes("application/json")
    public SimpleRoster.View updateRosterFields(Context ctx, String id) {
        Map<String, Object> updates;
        try {
            updates = mapType.fromJson(ctx.body());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON body: " + e.getMessage());
        }
        LOGGER.debug("updateRosterFields {}", updates);
        var owner = AuthFilter.getOwner(ctx);
        var oldRoster = rosterRepository.findById(id).orElseThrow(() -> new HttpResponseException(404, "Not found"));
        if (owner == null || !oldRoster.isOwnedBy(owner)) {
            LOGGER.debug("Not owner of roster {} {}", owner, oldRoster.owner());
            throw new HttpResponseException(403, "Not owner of this roster");
        }
        var allowedKeys = Set.of("body", "title", "visibility");
        for (var key : updates.keySet()) {
            if (!allowedKeys.contains(key)) {
                throw new IllegalArgumentException("No property named %s on type SimpleRoster".formatted(key));
            }
        }
        var body = updates.containsKey("body") ? (String) updates.get("body") : oldRoster.body();
        var title = updates.containsKey("body")
                ? SimpleRoster.extractTitle(body)
                : updates.containsKey("title") ? (String) updates.get("title") : oldRoster.title();
        var visibility = updates.containsKey("visibility")
                ? Visibility.valueOf((String) updates.get("visibility"))
                : oldRoster.visibility();
        var newRoster = new SimpleRoster(
                oldRoster.id(),
                title,
                body,
                oldRoster.owner(),
                visibility,
                oldRoster.createdAt(),
                oldRoster.updatedAt());
        return rosterRepository.update(newRoster).view();
    }
}
