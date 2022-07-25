/* (C) Edward Harman and contributors 2022 */
package org.ethelred.kv2.data;

import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import java.util.List;
import org.ethelred.kv2.models.DocumentStub;
import org.ethelred.kv2.models.SimpleRoster;
import org.ethelred.kv2.models.User;

@JdbcRepository(dialect = Dialect.MYSQL)
public interface SimpleRosterRepository extends CrudRepository<SimpleRoster, String> {
    @Query("select id, title from simple_roster where owner_id = :owner")
    List<DocumentStub> findByOwner(User owner);
}
