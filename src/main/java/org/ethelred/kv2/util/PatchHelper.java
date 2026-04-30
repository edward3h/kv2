/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Singleton;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;

@Singleton
public class PatchHelper {
    private final ObjectMapper objectMapper;

    public PatchHelper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @SuppressWarnings("unchecked")
    public <T> T apply(T oldObj, Map<String, Object> updates) {
        var klass = (Class<T>) oldObj.getClass();
        var components = klass.getRecordComponents();
        if (components == null) {
            throw new IllegalArgumentException("PatchHelper only supports records, got: " + klass.getSimpleName());
        }
        var componentNames = Arrays.stream(components).map(c -> c.getName()).toList();
        for (var key : updates.keySet()) {
            if (!componentNames.contains(key)) {
                throw new IllegalArgumentException(
                        "No property named %s on type %s".formatted(key, klass.getSimpleName()));
            }
        }
        try {
            var constructorTypes =
                    Arrays.stream(components).map(c -> c.getType()).toArray(Class[]::new);
            var constructor = klass.getConstructor(constructorTypes);
            var args = new Object[components.length];
            for (int i = 0; i < components.length; i++) {
                var component = components[i];
                var name = component.getName();
                if (updates.containsKey(name)) {
                    var javaType = objectMapper.getTypeFactory().constructType(component.getGenericType());
                    args[i] = objectMapper.convertValue(updates.get(name), javaType);
                } else {
                    args[i] = component.getAccessor().invoke(oldObj);
                }
            }
            return constructor.newInstance(args);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Failed to apply patch to " + klass.getSimpleName(), e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof IllegalArgumentException iae) {
                throw iae;
            }
            throw new RuntimeException("Failed to apply patch to " + klass.getSimpleName(), e);
        }
    }
}
