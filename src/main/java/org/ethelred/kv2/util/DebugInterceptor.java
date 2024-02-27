/* (C) Edward Harman and contributors 2022-2024 */
package org.ethelred.kv2.util;

import static org.ethelred.kv2.util.Lazy.lazy;

import io.micronaut.aop.InterceptorBean;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.core.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Prototype
@InterceptorBean(Debug.class)
public class DebugInterceptor implements MethodInterceptor<Object, Object> {
    private final Map<Class<?>, Logger> loggers = new ConcurrentHashMap<>();

    @SuppressWarnings("rawtypes")
    private final Map<Class<?>, DebugMapper> mappers;

    public DebugInterceptor(Collection<DebugMapper<?>> mappers) {
        this.mappers = mappers.stream().collect(Collectors.toMap(DebugMapper::supportedType, m -> m));
    }

    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {
        var klass = context.getDeclaringType();
        var logger = loggers.computeIfAbsent(klass, LoggerFactory::getLogger);
        logger.debug("@Debug entering {}.{}({})", klass.getName(), context.getName(), parameters(context));
        var result = context.proceed();
        logger.debug("@Debug exiting  {}.{}: {}", klass.getName(), context.getName(), lazy(() -> render(result)));

        return result;
    }

    @SuppressWarnings("unchecked")
    private String render(@Nullable Object object) {
        if (object == null) {
            return "null";
        }
        var mapper = mappers.get(object.getClass());
        if (mapper != null) {
            return mapper.inspect(object);
        }
        return object.toString();
    }

    private Lazy parameters(MethodInvocationContext<Object, Object> context) {
        return lazy(() -> context.getParameterValueMap().entrySet().stream()
                .map(e -> "%s: %s".formatted(e.getKey(), render(e.getValue())))
                .collect(Collectors.joining(", ")));
    }
}
