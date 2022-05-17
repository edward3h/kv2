package org.ethelred.kv2.services;

import io.micronaut.data.annotation.*;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
@Documented
@AutoPopulated(updateable = false)
public @interface GeneratedId {
    String NAME = GeneratedId.class.getName();
}
