/* (C) Edward Harman and contributors 2022-2024 */
package org.ethelred.kv2.data;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import org.ethelred.kv2.models.Identity;

@JdbcRepository(dialect = Dialect.MYSQL)
public interface IdentityRepository extends CrudRepository<Identity, String> {}
