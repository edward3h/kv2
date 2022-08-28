/* (C) Edward Harman and contributors 2022 */
package org.ethelred.kv2.util;

import io.micronaut.aop.InterceptorBean;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.context.annotation.Prototype;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Prototype
@InterceptorBean(Debug.class)
public class DebugInterceptor implements MethodInterceptor<Object, Object> {
    private final Map<Class<?>, Logger> loggers = new ConcurrentHashMap<>();

    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {
        var klass = context.getDeclaringType();
        var logger = loggers.computeIfAbsent(klass, LoggerFactory::getLogger);
        logger.debug("start {}.{}({})", klass.getName(), context.getName(), parameters(context));
        var result = context.proceed();
        logger.debug("end   {}.{}: {}", klass.getName(), context.getName(), result);

        return result;
    }

    private Lazy parameters(MethodInvocationContext<Object, Object> context) {
        return new Lazy(() -> context.getParameterValueMap().entrySet().stream()
                .map(e -> "%s: %s".formatted(e.getKey(), e.getValue()))
                .collect(Collectors.joining(", ")));
    }
}
