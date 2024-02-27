/* (C) Edward Harman and contributors 2024 */
package org.ethelred.kv2.util;

import io.micronaut.core.io.ResourceResolver;
import jakarta.inject.Singleton;

@Singleton
public class ResourceContentResolver {
    private final ResourceResolver resourceResolver;

    public ResourceContentResolver(ResourceResolver resourceResolver) {
        this.resourceResolver = resourceResolver;
    }

    public ResourceContent resolve(String name) {
        return resourceResolver
                .getResourceAsStream(name)
                .map(ResourceContent::new)
                .orElse(ResourceContent.empty());
    }
}
