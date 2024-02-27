/* (C) Edward Harman and contributors 2022-2024 */
package org.ethelred.kv2;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;

@OpenAPIDefinition()
public class Application {

    public static void main(String[] args) {
        Micronaut.build(args)
                .mainClass(Application.class)
                .packages("org.ethelred.cgi.micronaut")
                .start();
    }
}
