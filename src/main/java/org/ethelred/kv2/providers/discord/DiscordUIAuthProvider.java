/* (C) Edward Harman and contributors 2023-2024 */
package org.ethelred.kv2.providers.discord;

import io.micronaut.core.annotation.Order;
import jakarta.inject.Singleton;
import org.ethelred.kv2.viewmodels.UIAuthProvider;

@Singleton
@Order(100)
public class DiscordUIAuthProvider implements UIAuthProvider {
    @Override
    public String display() {
        return "Login with Discord";
    }

    @Override
    public String path() {
        return "/oauth/login/discord";
    }
}
