/* (C) Edward Harman and contributors 2023-2026 */
package org.ethelred.kv2.models;

import io.avaje.config.Config;
import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import java.util.List;
import org.ethelred.kv2.viewmodels.Assets;

@Factory
public class AssetsConfiguration {
    @Bean
    public Assets assets() {
        var styles = Config.list().of("assets.styles");
        var scripts = Config.list().of("assets.scripts");
        return new AssetsRecord(styles, scripts);
    }

    private record AssetsRecord(List<String> styles, List<String> scripts) implements Assets {}
}
