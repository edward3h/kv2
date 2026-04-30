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
    @Named("default")
    public DataSource dataSource() {
        var hikari = new HikariConfig();
        hikari.setJdbcUrl(Config.get("datasource.url"));
        hikari.setDriverClassName(Config.get("datasource.driverClassName", "com.mysql.cj.jdbc.Driver"));
        var username = Config.getOptional("datasource.username");
        username.ifPresent(u -> {
            hikari.setUsername(u);
            hikari.setPassword(Config.get("datasource.password", ""));
        });
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
