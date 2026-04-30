/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.security;

import io.avaje.jex.http.Context;
import io.avaje.jex.http.HttpResponseException;
import jakarta.inject.Singleton;
import java.util.regex.Pattern;
import org.ethelred.kv2.models.Owner;
import org.jspecify.annotations.Nullable;

@Singleton
public class AuthFilter {
    private static final String PRINCIPAL_ATTR = "principal";
    // Pattern for paths that are publicly readable regardless of HTTP method
    private static final Pattern PUBLIC_PATH_PATTERN =
            Pattern.compile("^(/|/oauth/.*|/dev/.*|/assets/.*|/favicon\\.ico|/roster/[^/]+|/abc/rosters/[^/]+)$");

    private final JwtService jwtService;

    public AuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public void before(Context ctx) {
        var jwt = ctx.cookie("JWT_TOKEN");
        if (jwt != null) {
            var principal = jwtService.parse(jwt);
            if (principal != null) {
                ctx.attribute(PRINCIPAL_ATTR, principal);
            }
        }

        if (!requiresAuth(ctx)) {
            return;
        }
        var principal = ctx.<Principal>attribute(PRINCIPAL_ATTR);
        if (principal == null) {
            throw new HttpResponseException(401, "Unauthorized");
        }
        if (!principal.hasRole("ROLE_USER")) {
            throw new HttpResponseException(403, "Forbidden");
        }
    }

    private boolean requiresAuth(Context ctx) {
        var path = ctx.path();
        var method = ctx.method();
        // GETs matching the public pattern don't need auth
        if ("GET".equals(method) && PUBLIC_PATH_PATTERN.matcher(path).matches()) {
            return false;
        }
        // OAuth/dev paths are always public
        if (path.startsWith("/oauth/") || path.startsWith("/dev/")) {
            return false;
        }
        return true;
    }

    @Nullable
    public static Principal getPrincipal(Context ctx) {
        return ctx.attribute(PRINCIPAL_ATTR);
    }

    @Nullable
    public static Owner getOwner(Context ctx) {
        return getPrincipal(ctx);
    }
}
