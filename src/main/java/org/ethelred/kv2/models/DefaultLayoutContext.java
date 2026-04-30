/* (C) Edward Harman and contributors 2023-2026 */
package org.ethelred.kv2.models;

import org.ethelred.kv2.security.Principal;
import org.ethelred.kv2.viewmodels.LayoutContext;
import org.ethelred.kv2.viewmodels.UIAuthProvider;
import org.jspecify.annotations.Nullable;

public class DefaultLayoutContext implements LayoutContext {
    private final String title;
    private final @Nullable Principal principal;
    private final Iterable<UIAuthProvider> providers;

    public DefaultLayoutContext(String title, @Nullable Principal principal, Iterable<UIAuthProvider> providers) {
        this.title = title;
        this.principal = principal;
        this.providers = providers;
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public boolean loggedIn() {
        return principal != null;
    }

    @Override
    public Iterable<UIAuthProvider> providers() {
        return providers;
    }

    @Override
    public String username() {
        return principal != null ? principal.id() : "Anonymous";
    }

    @Override
    public @Nullable String pictureUrl() {
        return attribute("picture");
    }

    @Override
    public String displayName() {
        return attribute("displayName");
    }

    private @Nullable String attribute(String key) {
        if (principal == null) {
            return null;
        }
        var value = principal.attributes().get(key);
        return value != null ? value.toString() : null;
    }
}
