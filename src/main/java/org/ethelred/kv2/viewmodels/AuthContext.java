/* (C) Edward Harman and contributors 2023 */
package org.ethelred.kv2.viewmodels;

import java.util.List;

public interface AuthContext {
    boolean loggedIn();

    default Iterable<AuthProvider> providers() {
        return PROVIDERS;
    }

    String username();

    List<AuthProvider> PROVIDERS = List.of(
            new AuthProvider("Login with Google", "/oauth/login/google"),
            new AuthProvider("Login with Discord", "/oauth/login/discord"));
}
