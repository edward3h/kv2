/* (C) Edward Harman and contributors 2022-2024 */
package org.ethelred.kv2.data;

import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import java.util.Optional;
import org.ethelred.kv2.models.User;

@JdbcRepository(dialect = Dialect.MYSQL)
public interface UserRepository extends CrudRepository<User, String> {
    @Query("select u.* from user u join identity i on u.id = i.user_id where i.provider = :provider and"
            + " i.external_id = :externalId")
    Optional<User> findByIdentity(String provider, String externalId);
}
