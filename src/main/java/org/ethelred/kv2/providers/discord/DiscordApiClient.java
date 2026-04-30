/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.providers.discord;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Singleton
public class DiscordApiClient {
    private static final String BASE_URL = "https://discord.com/api/v9";
    private static final String USER_AGENT = "Ordo Acerbus Login (https://github.com/edward3h/kv2 0.1)";

    private final HttpClient http = HttpClient.newHttpClient();
    private final JsonType<DiscordUser> userType;
    private final JsonType<List<DiscordGuild>> guildsType;

    DiscordApiClient(Jsonb jsonb) {
        this.userType = jsonb.type(DiscordUser.class);
        this.guildsType = jsonb.type(Types.listOf(DiscordGuild.class));
    }

    public DiscordUser getUser(String authorization) {
        return get("/users/@me", authorization, userType);
    }

    public List<DiscordGuild> getUserGuilds(String authorization) {
        return get("/users/@me/guilds", authorization, guildsType);
    }

    public static String authorization(String token) {
        return "Bearer %s".formatted(token);
    }

    private <T> T get(String path, String authorization, JsonType<T> type) {
        try {
            var response = http.send(request(path, authorization), HttpResponse.BodyHandlers.ofString());
            return type.fromJson(response.body());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpRequest request(String path, String authorization) {
        return HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Authorization", authorization)
                .header("User-Agent", USER_AGENT)
                .GET()
                .build();
    }
}
