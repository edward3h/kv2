/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.avaje.inject.BeanScope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MySQLContainerExtension.class)
class Kv2Test {

    @Test
    void testItWorks() {
        try (var scope = BeanScope.builder().profiles("test").build()) {
            assertNotNull(scope);
        }
    }
}
