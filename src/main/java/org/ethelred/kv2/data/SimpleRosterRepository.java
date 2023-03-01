/* (C) Edward Harman and contributors 2022-2023 */
package org.ethelred.kv2.data;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import java.util.List;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import org.ethelred.kv2.models.DocumentStub;
import org.ethelred.kv2.models.SimpleRoster;

@JdbcRepository(dialect = Dialect.MYSQL)
public interface SimpleRosterRepository extends CrudRepository<SimpleRoster, String> {
    @Query("select id, title from simple_roster where owner_id = :ownerId")
    List<DocumentStub> findByOwner(String ownerId);

    @Override
    @NonNull
    @Join(value = "owner", type = Join.Type.FETCH)
    Optional<SimpleRoster> findById(@NotNull String s);
}
