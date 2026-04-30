/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.util;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Debug {}
