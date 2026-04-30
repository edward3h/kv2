/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.services;

import com.github.ksuid.KsuidGenerator;
import io.avaje.inject.Bean;
import io.avaje.inject.Factory;

@Factory
public class KsuidFactory {
    @Bean
    public IdGenerator ksuidGenerator() {
        return KsuidGenerator::generate;
    }
}
