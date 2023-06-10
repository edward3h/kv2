/* (C) Edward Harman and contributors 2023 */
package org.ethelred.kv2.viewmodels;

import io.micronaut.core.annotation.Nullable;
import java.util.Objects;
import java.util.regex.Pattern;

public interface LayoutContext {
    String title();

    boolean loggedIn();

    Iterable<UIAuthProvider> providers();

    String username();

    @Nullable
    String pictureUrl();

    default String displayName() {
        return username();
    }

    default String placeholder() {
        var username = Objects.requireNonNullElseGet(displayName(), this::username);
        var matcher = Pattern.compile("^(\\p{javaUpperCase})\\p{javaLowerCase}*\\s?(\\p{javaUpperCase}).*")
                .matcher(username);
        if (matcher.matches()) {
            return matcher.group(1) + matcher.group(2);
        }
        return username.substring(0, Math.min(username.length(), 2)).toUpperCase();
    }

    default boolean isFragment() {
        return false;
    }
}
