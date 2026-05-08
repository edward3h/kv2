/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2;

import io.avaje.inject.BeanScope;
import io.avaje.jex.Jex;
import java.io.File;
import java.util.concurrent.TimeUnit;
import org.ethelred.kv2.controllers.MyExceptionHandlers;
import org.ethelred.kv2.security.AuthFilter;

public class Application {

    public static void main(String[] args) {
        System.out.println("Running...");
        registerDevShutdownHook();
        var beanScope = BeanScope.builder().build();
        var authFilter = beanScope.get(AuthFilter.class);
        var exceptionHandlers = beanScope.get(MyExceptionHandlers.class);

        var app =
                Jex.create().configureWith(beanScope).before(authFilter::before).port(8080);

        exceptionHandlers.configure(app);

        app.start();
    }

    private static void registerDevShutdownHook() {
        var profiles = System.getProperty("avaje.profiles", "");
        if (!profiles.contains("dev")) return;
        Runtime.getRuntime()
                .addShutdownHook(new Thread(
                        () -> {
                            System.err.println("[dev] Stopping MySQL...");
                            try {
                                new ProcessBuilder("docker", "compose", "stop", "mysql")
                                        .directory(new File(System.getProperty("user.dir")))
                                        .inheritIO()
                                        .start()
                                        .waitFor(30, TimeUnit.SECONDS);
                            } catch (Exception ignored) {
                            }
                        },
                        "dev-mysql-stop"));
    }
}
