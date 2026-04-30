/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.MySQLContainer;

public class MySQLContainerExtension
        implements BeforeAllCallback, ExtensionContext.Store.CloseableResource, AutoCloseable {

    private static final String STORE_KEY = "MySQLContainerExtension";
    private MySQLContainer<?> container;

    @Override
    public void beforeAll(ExtensionContext context) {
        var store = context.getRoot().getStore(ExtensionContext.Namespace.GLOBAL);
        if (store.get(STORE_KEY) == null) {
            container = new MySQLContainer<>("mysql:8")
                    .withDatabaseName("db")
                    .withUsername("kv2")
                    .withPassword("12345")
                    .withConfigurationOverride("data/mysql-conf");
            container.start();
            System.setProperty(
                    "datasource.url", container.getJdbcUrl() + "?allowLoadLocalInfile=true&allowUrlInLocalInfile=true");
            store.put(STORE_KEY, this);
        }
    }

    @Override
    public void close() {
        if (container != null && container.isRunning()) {
            container.stop();
        }
    }
}
