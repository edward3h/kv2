/* (C) Edward Harman and contributors 2024-2026 */
package org.ethelred.kv2.util;

import jakarta.inject.Singleton;
import java.util.Optional;

@Singleton
public class ResourceContentResolver {
    public ResourceContent resolve(String name) {
        // Strip leading "classpath:" prefix if present
        var path = name.startsWith("classpath:") ? name.substring("classpath:".length()) : name;
        // Strip leading slash for ClassLoader lookup
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return Optional.ofNullable(
                        Thread.currentThread().getContextClassLoader().getResourceAsStream(path))
                .map(ResourceContent::new)
                .orElse(ResourceContent.empty());
    }
}
