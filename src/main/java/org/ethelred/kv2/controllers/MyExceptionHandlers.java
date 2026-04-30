/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.controllers;

import io.avaje.jex.Jex;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MyExceptionHandlers {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyExceptionHandlers.class);

    public void configure(Jex app) {
        app.error(IllegalArgumentException.class, (ctx, ex) -> {
            LOGGER.error("had exception", ex);
            ctx.status(400);
            ctx.text("Bad Request: " + ex.getMessage());
        });
    }
}
