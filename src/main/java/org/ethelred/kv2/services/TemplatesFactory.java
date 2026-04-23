/* (C) Edward Harman and contributors 2023-2026 */
package org.ethelred.kv2.services;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import java.nio.file.Paths;
import org.ethelred.kv2.template.DynamicTemplates;
import org.ethelred.kv2.template.StaticTemplates;
import org.ethelred.kv2.template.Templates;

@Factory
public class TemplatesFactory {
    @Singleton
    public Templates templates(
            @Value("${kv2.jte.dynamic:false}") boolean dynamic,
            @Value("${kv2.jte.dynamic-source-path:views/src/main/jte}") String templatePath) {
        if (dynamic) {
            var engine = TemplateEngine.create(new DirectoryCodeResolver(Paths.get(templatePath)), ContentType.Html);
            return new DynamicTemplates(engine);
        }
        return new StaticTemplates();
    }
}
