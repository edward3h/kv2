/* (C) Edward Harman and contributors 2026 */
package org.ethelred.kv2.data;

import org.jspecify.annotations.Nullable;

public interface DataSourceConfig {
    String url();

    String driverClassName();

    @Nullable
    String username();

    String password();
}
