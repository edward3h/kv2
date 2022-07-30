/* (C) Edward Harman and contributors 2022 */
package org.ethelred.kv2.controllers;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Patch;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.ethelred.kv2.data.SimpleRosterRepository;
import org.ethelred.kv2.models.DocumentStub;
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
    public List<DocumentStub> userRosters(@Nullable Authentication auth) {
        var user = userService.userFromAuthentication(auth);
        return rosterRepository.findByOwner(user);
    }

    @Get("/{id}")
    @Secured(SecurityRule.IS_ANONYMOUS)
    public SimpleRoster.View getRoster(@Nullable Authentication auth, @PathVariable String id) {
        var roster = rosterRepository
                .findById(id)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Not found"));
        if (roster.visibility() == Visibility.PUBLIC) {
            return roster.view();
        }
        // group visibility TODO
        var user = userService.userFromAuthentication(auth);
        if (Objects.equals(user, roster.owner())) {
            return roster.view();
        }
        throw new HttpStatusException(HttpStatus.FORBIDDEN, "Private roster");
    }

    @Post
    public SimpleRoster.View createRoster(@Nullable Authentication auth, @Body String rosterBody) {
        var owner = userService.userFromAuthentication(auth);
        var roster = new SimpleRoster(owner, rosterBody, Visibility.PRIVATE);
        return rosterRepository.save(roster).view();
    }

    @Patch("/{id}")
    public SimpleRoster.View updateRosterFields(
            @Nullable Authentication auth, @PathVariable String id, @Body Map<String, Object> updates) {
        var owner = userService.userFromAuthentication(auth);
        var oldRoster = rosterRepository
                .findById(id)
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Not found"));
        if (!Objects.equals(owner, oldRoster.owner())) {
            LOGGER.debug("Not owner of roster {} {}", owner, oldRoster.owner());
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "Not owner of this roster");
        }
        var newRoster = patchHelper.apply(oldRoster, updates);
        return rosterRepository.update(newRoster).view();
    }
}
