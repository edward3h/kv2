/* (C) Edward Harman and contributors 2023-2026 */
package org.ethelred.kv2.dev;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.http.api.Post;
import io.avaje.http.api.Produces;
import io.avaje.jex.http.Context;
import jakarta.inject.Singleton;
import java.util.Map;
import org.ethelred.kv2.security.JwtService;
import org.ethelred.kv2.services.UserService;

@Controller("/dev")
@Singleton
public class DevLoginController {
    private final UserService userService;
    private final JwtService jwtService;
    private final DevConfig devConfig;

    public DevLoginController(UserService userService, JwtService jwtService, DevConfig devConfig) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.devConfig = devConfig;
    }

    private boolean isDevEnabled() {
        return devConfig.enabled();
    }

    @Get("/login")
    @Produces("text/html")
    public String loginForm(Context ctx) {
        if (!isDevEnabled()) {
            throw new io.avaje.jex.http.HttpResponseException(404, "Not found");
        }
        // language=HTML
        return """
                <html>
                <body>
                <form action='/dev/login' method='post'>
                <label for='username'>Login as</label>
                <input type='text' id='username' name='username'>
                <input type='submit' value='Login'>
                </form>
                </body>
                </html>
                """;
    }

    @Post("/login")
    public void doLogin(Context ctx) {
        if (!isDevEnabled()) {
            throw new io.avaje.jex.http.HttpResponseException(404, "Not found");
        }
        var username = ctx.formParam("username");
        if (username == null || username.isBlank()) {
            ctx.redirect("/dev/login");
            return;
        }
        var user = userService.findOrCreateUser("dev", username, Map.of("name", username));
        var jwt = jwtService.generate(user);
        ctx.cookie(Context.Cookie.of("JWT_TOKEN", jwt)
                .maxAge(java.time.Duration.ofSeconds(7 * 24 * 60 * 60))
                .path("/"));
        ctx.redirect("/");
    }
}
