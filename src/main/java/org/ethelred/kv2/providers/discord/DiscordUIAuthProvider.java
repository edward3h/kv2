/* (C) Edward Harman and contributors 2023-2026 */
package org.ethelred.kv2.providers.discord;

import jakarta.inject.Singleton;
import org.ethelred.kv2.viewmodels.UIAuthProvider;

@Singleton
public class DiscordUIAuthProvider implements UIAuthProvider {
    @Override
    public String display() {
        return "Login with Discord";
    }

    @Override
    public String path() {
        return "/oauth/discord";
    }
}
