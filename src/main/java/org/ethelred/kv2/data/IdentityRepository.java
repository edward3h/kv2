/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.data;

import org.ethelred.kv2.models.Identity;

public interface IdentityRepository {
    void save(Identity identity);
}
