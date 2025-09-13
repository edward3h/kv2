/* (C) Edward Harman and contributors 2022-2025 */
package org.ethelred.kv2.services;

import io.micronaut.core.beans.*;
import io.micronaut.data.annotation.event.*;
import io.micronaut.data.event.*;
import io.micronaut.data.model.runtime.*;
import io.micronaut.data.runtime.event.listeners.*;
import jakarta.inject.*;
import java.lang.annotation.*;
import java.util.*;
import java.util.function.*;

@Singleton
public class GeneratedIdEntityEventListener extends AutoPopulatedEntityEventListener {
    private final IdGenerator idGenerator;

    public GeneratedIdEntityEventListener(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Override
    protected List<Class<? extends Annotation>> getEventTypes() {
        return List.of(PrePersist.class);
    }

    @Override
    protected Predicate<RuntimePersistentProperty<Object>> getPropertyPredicate() {
        return rpp ->
                rpp.getType() == String.class && rpp.getAnnotationMetadata().hasAnnotation(GeneratedId.class);
    }

    @Override
    public boolean prePersist(EntityEventContext<Object> context) {
        var persistentProperties = getApplicableProperties(context.getPersistentEntity());
        for (var property : persistentProperties) {
            @SuppressWarnings("unchecked")
            var beanProperty = (BeanProperty<Object, String>) property.getProperty();
            context.setProperty(beanProperty, idGenerator.generate());
        }
        return true;
    }
}
