/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.avaje.config.Config;
import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import jakarta.inject.Named;
import javax.sql.DataSource;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Factory
public class DataSourceFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceFactory.class);

    @Bean
    @SuppressWarnings("nullness")
    public DataSourceConfig dataSourceConfig() {
        record Impl(String url, String driverClassName, String username, String password) implements DataSourceConfig {}
        return new Impl(
                Config.get("datasource.url"),
                Config.get("datasource.driverClassName", "com.mysql.cj.jdbc.Driver"),
                Config.getOptional("datasource.username").orElse(null),
                Config.get("datasource.password", ""));
    }

    @Bean
    @Named("default")
    public DataSource dataSource(DataSourceConfig config) {
        var hikari = new HikariConfig();
        hikari.setJdbcUrl(config.url());
        hikari.setDriverClassName(config.driverClassName());
        var username = config.username();
        if (username != null) {
            hikari.setUsername(username);
            hikari.setPassword(config.password());
        }
        var ds = new HikariDataSource(hikari);
        runLiquibase(ds);
        return ds;
    }

    private void runLiquibase(DataSource ds) {
        try (var conn = ds.getConnection()) {
            var database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(conn));
            var liquibase = new Liquibase("db/liquibase-changelog.xml", new ClassLoaderResourceAccessor(), database);
            liquibase.update("");
            LOGGER.info("Liquibase migrations applied");
        } catch (Exception e) {
            throw new RuntimeException("Liquibase migration failed", e);
        }
    }
}
