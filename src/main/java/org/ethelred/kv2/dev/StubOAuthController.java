/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.dev;

import io.avaje.config.Config;
import io.avaje.jex.Routing;
import io.avaje.jex.http.Context;
import io.avaje.jex.http.HttpResponseException;
import jakarta.inject.Singleton;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.ethelred.kv2.providers.discord.DiscordGuild;
import org.ethelred.kv2.providers.discord.DiscordUser;

@Singleton
public class StubOAuthController implements Routing.HttpService {

    private static final List<DiscordUser> STUB_USERS = List.of(
            new DiscordUser("111111111111111111", "devuser", "0", null, "devuser@example.com"),
            new DiscordUser("999999999999999999", "testuser", "0", null, "test@example.com"));

    private static final Map<String, List<DiscordGuild>> STUB_GUILDS = Map.of(
            "111111111111111111",
            List.of(new DiscordGuild("888888888888888888", "Dev Guild", null)),
            "999999999999999999",
            List.of(new DiscordGuild("888888888888888888", "Test Guild", null)));

    @Override
    public void add(Routing routing) {
        routing.get("/stub/oauth/authorize", this::authorize);
        routing.get("/stub/oauth/authorize/select", this::select);
        routing.post("/stub/oauth/token", this::token);
        // Use wildcard to avoid potential @me routing issues; dispatch in handler
        routing.get("/stub/api/v9/users/*", this::usersDispatch);
    }

    private boolean isDevEnabled() {
        return Config.getBool("kv2.dev.enabled", false);
    }

    private void authorize(Context ctx) {
        if (!isDevEnabled()) throw new HttpResponseException(404, "Not found");
        var redirectUri = ctx.queryParam("redirect_uri");
        var state = ctx.queryParam("state");
        var html = new StringBuilder("<html><body><h1>Stub OAuth2 Login</h1><ul>");
        for (var user : STUB_USERS) {
            var selectUrl = "/stub/oauth/authorize/select?user_id=" + encode(user.id())
                    + "&redirect_uri=" + encode(redirectUri)
                    + "&state=" + encode(state);
            html.append("<li><a href='")
                    .append(selectUrl)
                    .append("'>Login as ")
                    .append(user.username())
                    .append("</a></li>");
        }
        html.append("</ul></body></html>");
        ctx.html(html.toString());
    }

    private void select(Context ctx) {
        if (!isDevEnabled()) throw new HttpResponseException(404, "Not found");
        var userId = ctx.queryParam("user_id");
        var redirectUri = ctx.queryParam("redirect_uri");
        var state = ctx.queryParam("state");
        ctx.redirect(redirectUri + "?code=" + encode(userId) + "&state=" + encode(state));
    }

    private void token(Context ctx) {
        if (!isDevEnabled()) throw new HttpResponseException(404, "Not found");
        var code = ctx.formParam("code");
        ctx.contentType("application/json");
        ctx.write("{\"access_token\":\"" + code + "\",\"token_type\":\"Bearer\",\"scope\":\"identify guilds email\"}");
    }

    private void usersDispatch(Context ctx) {
        if (!isDevEnabled()) throw new HttpResponseException(404, "Not found");
        var path = ctx.path();
        if (path.endsWith("/@me/guilds")) {
            getGuilds(ctx);
        } else if (path.endsWith("/@me")) {
            getUser(ctx);
        } else {
            throw new HttpResponseException(404, "Not found");
        }
    }

    private void getUser(Context ctx) {
        var auth = ctx.header("Authorization");
        var userId = auth != null ? auth.replaceFirst("^Bearer ", "") : "";
        var user = STUB_USERS.stream()
                .filter(u -> u.id().equals(userId))
                .findFirst()
                .orElseThrow(() -> new HttpResponseException(404, "User not found"));
        ctx.contentType("application/json");
        ctx.write(userToJson(user));
    }

    private void getGuilds(Context ctx) {
        var auth = ctx.header("Authorization");
        var userId = auth != null ? auth.replaceFirst("^Bearer ", "") : "";
        var guilds = STUB_GUILDS.getOrDefault(userId, List.of());
        ctx.contentType("application/json");
        ctx.write(guildsToJson(guilds));
    }

    private static String userToJson(DiscordUser u) {
        return "{\"id\":\"%s\",\"username\":\"%s\",\"discriminator\":\"%s\",\"avatar\":%s,\"email\":%s}"
                .formatted(
                        u.id(),
                        u.username(),
                        u.discriminator(),
                        u.avatar() != null ? "\"" + u.avatar() + "\"" : "null",
                        u.email() != null ? "\"" + u.email() + "\"" : "null");
    }

    private static String guildsToJson(List<DiscordGuild> guilds) {
        var sb = new StringBuilder("[");
        for (int i = 0; i < guilds.size(); i++) {
            if (i > 0) sb.append(",");
            var g = guilds.get(i);
            sb.append("{\"id\":\"")
                    .append(g.id())
                    .append("\",\"name\":\"")
                    .append(g.name())
                    .append("\",\"icon\":null}");
        }
        return sb.append("]").toString();
    }

    private static String encode(String s) {
        return URLEncoder.encode(s != null ? s : "", StandardCharsets.UTF_8);
    }
}
