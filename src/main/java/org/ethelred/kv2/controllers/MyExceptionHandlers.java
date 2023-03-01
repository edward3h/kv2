/* (C) Edward Harman and contributors 2022-2023 */
package org.ethelred.kv2.controllers;

import io.micronaut.context.annotation.Factory;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;

@Factory
public class MyExceptionHandlers {
    @Singleton
    public ExceptionHandler<IllegalArgumentException, HttpResponse<?>> illegalArgumentHandler() {
        return ((request, exception) -> HttpResponse.badRequest());
    }
}
