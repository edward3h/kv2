/* (C) Edward Harman and contributors 2022-2025 */
package org.ethelred.kv2.util;

import io.micronaut.aop.Around;
import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Around
public @interface Debug {}
