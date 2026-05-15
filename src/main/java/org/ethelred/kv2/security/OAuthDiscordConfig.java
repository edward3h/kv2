/* (C) Edward Harman and contributors 2026 */
package org.ethelred.kv2.security;

public interface OAuthDiscordConfig {
    String clientId();

    String clientSecret();

    String redirectUri();

    String authorizeUrl();

    String tokenUrl();
}
