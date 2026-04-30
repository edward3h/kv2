/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.security;

import java.util.Collection;
import java.util.Map;
import org.ethelred.kv2.models.Owner;

public record Principal(String id, Collection<String> roles, Map<String, Object> attributes) implements Owner {
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }
}
