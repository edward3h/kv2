/* (C) Edward Harman and contributors 2022-2025 */
package org.ethelred.kv2.providers;

import io.micronaut.core.io.scan.ClassPathResourceLoader;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.sql.DataSource;

@Singleton
public class TestDataLoader {
    private final DataSource dataSource;
    private final ClassPathResourceLoader resourceLoader;

    @Inject
    public TestDataLoader(DataSource dataSource, ClassPathResourceLoader resourceLoader) {
        this.dataSource = dataSource;
        this.resourceLoader = resourceLoader;
    }

    private record DataMapping(String filename, String tablename, URL path) {}

    public void load(String... filenames) {
        if (filenames.length == 0) {
            return;
        }
        try (var conn = dataSource.getConnection()) {
            var tableNames = _getTableNames(conn);
            var fileMapping = new ArrayList<DataMapping>();
            for (var filename : filenames) {
                var tablename = _tableNameFromFilename(filename);
                if (!tableNames.contains(tablename)) {
                    throw new IllegalArgumentException("No table matching " + tablename);
                }
                var url = resourceLoader.getResource(filename);
                if (url.isEmpty()) {
                    throw new IllegalArgumentException("Could not find file " + filename);
                }
                fileMapping.add(new DataMapping(filename, tablename, url.get()));
            }
            // delete contents in reverse order
            for (int i = fileMapping.size() - 1; i >= 0; i--) {
                _delete(conn, fileMapping.get(i).tablename());
            }
            // insert
            for (var mapping : fileMapping) {
                _insert(conn, mapping);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("load data failed", e);
        }
    }

    private void _insert(Connection conn, DataMapping mapping) throws SQLException {
        try (var s = conn.prepareStatement(
                """
            LOAD DATA LOCAL INFILE ?
            INTO TABLE %s
            FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
            IGNORE 1 LINES
            """
                        .formatted(mapping.tablename()))) {
            s.setURL(1, mapping.path());
            s.execute();
        }
    }

    private void _delete(Connection conn, String tablename) throws SQLException {
        try (var s = conn.createStatement()) {
            s.execute("DELETE FROM %s".formatted(tablename)); // awesome injection right there
        }
    }

    private String _tableNameFromFilename(String filename) {
        if (!filename.contains(".")) {
            throw new IllegalArgumentException("Expected filename to have an extension " + filename);
        }
        if (filename.contains("/")) {
            filename = _last(filename.split("/"));
        }
        return filename.split("\\.")[0];
    }

    private String _last(String[] strings) {
        return strings[strings.length - 1];
    }

    private Set<String> _getTableNames(Connection conn) throws SQLException {
        var dbmd = conn.getMetaData();
        try (var rs = dbmd.getTables(null, null, null, new String[] {"TABLE"})) {
            var tableNames = new HashSet<String>();
            while (rs.next()) {
                tableNames.add(rs.getString("TABLE_NAME"));
            }
            return tableNames;
        }
    }
}
