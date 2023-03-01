/* (C) Edward Harman and contributors 2022-2023 */
package org.ethelred.kv2.services;

import com.github.ksuid.KsuidGenerator;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;

@Factory
public class KsuidFactory {
    @Singleton
    IdGenerator ksuidGenerator() {
        return KsuidGenerator::generate;
    }
}
