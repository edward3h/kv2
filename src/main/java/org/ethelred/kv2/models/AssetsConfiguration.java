/* (C) Edward Harman and contributors 2023-2026 */
package org.ethelred.kv2.models;

import io.avaje.config.Config;
import io.avaje.config.Configuration;
import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import java.util.List;
import org.ethelred.kv2.viewmodels.Assets;

@Factory
public class AssetsConfiguration {
    @Bean
    public Configuration configuration() {
        return Config.asConfiguration();
    }

    @Bean
    public Assets assets(Configuration config) {
        var styles = config.list().of("assets.styles");
        var scripts = config.list().of("assets.scripts");
        return new AssetsRecord(styles, scripts);
    }

    private record AssetsRecord(List<String> styles, List<String> scripts) implements Assets {}
}
