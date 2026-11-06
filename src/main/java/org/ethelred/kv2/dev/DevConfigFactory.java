/* (C) Edward Harman and contributors 2026 */
package org.ethelred.kv2.dev;

import io.avaje.config.Configuration;
import io.avaje.inject.Bean;
import io.avaje.inject.Factory;

@Factory
public class DevConfigFactory {

    @Bean
    public DevConfig devConfig(Configuration config) {
        record Impl(boolean enabled) implements DevConfig {}
        return new Impl(config.getBool("kv2.dev.enabled", false));
    }
}
