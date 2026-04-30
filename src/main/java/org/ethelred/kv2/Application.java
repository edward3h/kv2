/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2;

import io.avaje.inject.BeanScope;
import io.avaje.jex.Jex;
import io.avaje.jex.Routing;
import org.ethelred.kv2.controllers.MyExceptionHandlers;
import org.ethelred.kv2.security.AuthFilter;

public class Application {

    public static void main(String[] args) {
        var context = BeanScope.builder().build();
        var authFilter = context.get(AuthFilter.class);
        var exceptionHandlers = context.get(MyExceptionHandlers.class);

        var app = Jex.create()
                .routing(context.list(Routing.HttpService.class))
                .before(authFilter::before)
                .port(8080);

        exceptionHandlers.configure(app);

        app.start();
    }
}
