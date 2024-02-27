/* (C) Edward Harman and contributors 2022-2024 */
package org.ethelred.kv2.controllers;

import io.micronaut.context.annotation.Factory;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Factory
public class MyExceptionHandlers {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyExceptionHandlers.class);

    @Singleton
    public ExceptionHandler<IllegalArgumentException, HttpResponse<?>> illegalArgumentHandler() {
        return ((request, exception) -> {
            LOGGER.error("{} {} had exception", request.getMethodName(), request.getPath(), exception);
            return HttpResponse.badRequest();
        });
    }
}
