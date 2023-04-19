/* (C) Edward Harman and contributors 2023 */
package org.ethelred.kv2.models;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.security.authentication.Authentication;
import org.ethelred.kv2.viewmodels.LayoutContext;

public class DefaultLayoutContext implements LayoutContext {
    @NonNull
    private final String title;

    private final @Nullable Authentication o;

    public DefaultLayoutContext(@NonNull String title, @Nullable Authentication o) {
        this.title = title;
        this.o = o;
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public boolean loggedIn() {
        return o != null;
    }

    @Override
    public String username() {
        return o != null ? o.getName() : "Anonymous";
    }

    @Override
    public String pictureUrl() {
        return authAttribute("picture");
    }

    @Override
    public String displayName() {
        return authAttribute("displayName");
    }

    private @Nullable String authAttribute(String key) {
        if (o == null) {
            return null;
        }
        var value = o.getAttributes().get(key);
        if (value == null) {
            return null;
        }
        return value.toString();
    }
}
