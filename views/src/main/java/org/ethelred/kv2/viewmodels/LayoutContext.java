/* (C) Edward Harman and contributors 2023 */
package org.ethelred.kv2.viewmodels;

import io.micronaut.core.annotation.Nullable;
import java.util.List;
import java.util.regex.Pattern;

public interface LayoutContext {
    String title();

    boolean loggedIn();

    default Iterable<AuthProvider> providers() {
        return PROVIDERS;
    }

    String username();

    @Nullable
    String pictureUrl();

    default String displayName() {
        return username();
    }

    default String placeholder() {
        var username = displayName();
        var matcher = Pattern.compile("^(\\p{javaUpperCase})\\p{javaLowerCase}*\\s?(\\p{javaUpperCase}).*")
                .matcher(username);
        if (matcher.matches()) {
            return matcher.group(1) + matcher.group(2);
        }
        return username.substring(0, Math.min(username.length(), 2)).toUpperCase();
    }

    List<AuthProvider> PROVIDERS = List.of(
            new AuthProvider("Login with Google", "/oauth/login/google"),
            new AuthProvider("Login with Discord", "/oauth/login/discord"));

    default boolean isFragment() {
        return false;
    }

    static LayoutContext empty() {
        return new LayoutContext() {
            @Override
            public String title() {
                return "";
            }

            @Override
            public boolean loggedIn() {
                return false;
            }

            @Override
            public String username() {
                return "";
            }

            @Override
            public String pictureUrl() {
                return null;
            }

            @Override
            public boolean isFragment() {
                return true;
            }
        };
    }
}
