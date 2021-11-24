package org.ethelred.kv2.providers.discord;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.oauth2.endpoint.authorization.state.State;
import io.micronaut.security.oauth2.endpoint.token.response.OauthAuthenticationMapper;
import io.micronaut.security.oauth2.endpoint.token.response.TokenResponse;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 *
 * @author edward
 */
@Named("discord")
@Singleton
public class DiscordAuthenticationMapper implements OauthAuthenticationMapper {

    private final DiscordApiClient apiClient;

    public DiscordAuthenticationMapper(DiscordApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Publisher<AuthenticationResponse> createAuthenticationResponse(TokenResponse tokenResponse, @Nullable State state) {
        var accessToken = tokenResponse.getAccessToken();
        var auth = DiscordApiClient.authorization(accessToken);
        var userPub = Mono.from(apiClient.getUser(auth));
        var guildsPub = Flux.from(apiClient.getUserGuilds(auth)).collectList();
        return Mono.zip(userPub, guildsPub,
            (user, guilds) -> AuthenticationResponse.success(user.username(),
                Map.of("guilds", guilds.stream().map(DiscordGuild::name).collect(Collectors.toList()),
                    "email", Objects.requireNonNullElse(user.email(), "Unknown"))
            )
        );
    }

}
