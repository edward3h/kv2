/* (C) Edward Harman and contributors 2022 */
package org.ethelred.kv2.controllers;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Patch;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.ethelred.kv2.data.SimpleRosterRepository;
import org.ethelred.kv2.models.DocumentStub;
import org.ethelred.kv2.models.Owner;
import org.ethelred.kv2.models.SimpleRoster;
import org.ethelred.kv2.models.Visibility;
import org.ethelred.kv2.services.UserService;
import org.ethelred.kv2.util.PatchHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller("/abc/rosters")
@Secured({"ROLE_USER"})
public record RosterController(
        SimpleRosterRepository rosterRepository, UserService userService, PatchHelper patchHelper) {
    private static final Logger LOGGER = LoggerFactory.getLogger(RosterController.class);

    @Get
    public List<DocumentStub> userRosters(@Nullable Owner user) {
        return rosterRepository.findByOwner(user.id());
    }

    @Get("/{id}")
    @Secured(SecurityRule.IS_ANONYMOUS)
    public SimpleRoster.View getRoster(@Nullable Owner user, @PathVariable String id) {
        var roster = rosterRepository
                .findById(id)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Not found"));
        if (roster.isVisibleTo(user)) {
            return roster.view();
        }
        throw new HttpStatusException(HttpStatus.FORBIDDEN, "Private roster");
    }

    @Post
    public SimpleRoster.View createRoster(@Nullable Owner owner, @Body @Nullable String rosterBody) {
        if (owner == null) {
            throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "wat");
        }
        var user = userService.findById(owner.id());
        if (user.isEmpty()) {
            throw new HttpStatusException(HttpStatus.NOT_FOUND, "also wat");
        }
        var roster = new SimpleRoster(user.get(), Objects.requireNonNullElse(rosterBody, ""), Visibility.PRIVATE);
        return rosterRepository.save(roster).view();
    }

    @Delete("/{id}")
    public void deleteRoster(Owner owner, @PathVariable String id) {
        var oldRoster = rosterRepository
                .findById(id)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Not found"));
        if (!oldRoster.isOwnedBy(owner)) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "Not owner of this roster");
        }
        rosterRepository.deleteById(id);
    }

    @Patch(value = "/{id}", consumes = MediaType.APPLICATION_JSON)
    public SimpleRoster.View updateRosterFields(
            Owner owner, @PathVariable String id, @Body Map<String, Object> updates) {
        LOGGER.debug("updateRosterFields {}", updates);
        var oldRoster = rosterRepository
                .findById(id)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Not found"));
        if (!oldRoster.isOwnedBy(owner)) {
            LOGGER.debug("Not owner of roster {} {}", owner, oldRoster.owner());
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "Not owner of this roster");
        }
        if (updates.containsKey("body")) {
            updates = new HashMap<>(updates);
            updates.put("title", SimpleRoster.extractTitle((String) updates.get("body")));
        }
        var newRoster = patchHelper.apply(oldRoster, updates);
        return rosterRepository.update(newRoster).view();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (RosterController) obj;
        return Objects.equals(this.rosterRepository, that.rosterRepository)
                && Objects.equals(this.userService, that.userService)
                && Objects.equals(this.patchHelper, that.patchHelper);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rosterRepository, userService, patchHelper);
    }

    @Override
    public String toString() {
        return "RosterController[" + "rosterRepository="
                + rosterRepository + ", " + "userService="
                + userService + ", " + "patchHelper="
                + patchHelper + ']';
    }
}
