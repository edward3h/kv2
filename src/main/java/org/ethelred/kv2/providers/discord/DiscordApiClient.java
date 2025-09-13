/* (C) Edward Harman and contributors 2022-2025 */
package org.ethelred.kv2.providers.discord;

import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.client.annotation.Client;
import org.reactivestreams.Publisher;

/** @author edward */
@Header(name = "User-Agent", value = "Ordo Acerbus Login (https://github.com/edward3h/kv2 0.1)")
@Client("https://discord.com/api/v9")
public interface DiscordApiClient {

    @Get("/users/@me")
    @SingleResult
    Publisher<DiscordUser> getUser(@Header(HttpHeaders.AUTHORIZATION) String authorization);

    @Get("/users/@me/guilds")
    Publisher<DiscordGuild> getUserGuilds(@Header(HttpHeaders.AUTHORIZATION) String authorization);

    static String authorization(String token) {
        return "Bearer %s".formatted(token);
    }
}
