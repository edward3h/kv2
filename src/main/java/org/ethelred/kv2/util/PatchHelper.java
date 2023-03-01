/* (C) Edward Harman and contributors 2022-2023 */
package org.ethelred.kv2.util;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.convert.ConversionService;
import jakarta.inject.Singleton;
import java.util.Map;

@Singleton
public record PatchHelper(ConversionService<?> conversionService) {
    public <T> T apply(@NonNull T oldObj, @NonNull Map<String, Object> updates) {
        @SuppressWarnings("unchecked")
        var introspection = BeanIntrospection.getIntrospection((Class<T>) oldObj.getClass());
        var newObj = oldObj;
        for (var entry : updates.entrySet()) {
            var property = introspection.getProperty(entry.getKey());
            if (property.isPresent()) {
                var p = property.get();
                newObj = p.withValue(newObj, conversionService.convertRequired(entry.getValue(), p.getType()));
            } else {
                throw new IllegalArgumentException("No property named %s on type %s"
                        .formatted(entry.getKey(), oldObj.getClass().getSimpleName()));
            }
        }
        return newObj;
    }
}
