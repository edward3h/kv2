/* (C) Edward Harman and contributors 2023-2026 */
package org.ethelred.kv2.services;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import io.avaje.config.Config;
import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import java.nio.file.Paths;
import org.ethelred.kv2.template.DynamicTemplates;
import org.ethelred.kv2.template.StaticTemplates;
import org.ethelred.kv2.template.Templates;

@Factory
public class TemplatesFactory {

    @Bean
    public TemplatesConfig templatesConfig() {
        record Impl(boolean dynamic, String dynamicSourcePath) implements TemplatesConfig {}
        return new Impl(
                Config.getBool("kv2.jte.dynamic", false),
                Config.get("kv2.jte.dynamic-source-path", "views/src/main/jte"));
    }

    @Bean
    public Templates templates(TemplatesConfig config) {
        if (config.dynamic()) {
            var engine = TemplateEngine.create(
                    new DirectoryCodeResolver(Paths.get(config.dynamicSourcePath())), ContentType.Html);
            return new DynamicTemplates(engine);
        }
        return new StaticTemplates();
    }
}
