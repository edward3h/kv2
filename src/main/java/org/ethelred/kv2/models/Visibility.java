/* (C) Edward Harman and contributors 2022-2023 */
package org.ethelred.kv2.models;

import io.micronaut.core.annotation.Introspected;

@Introspected
public enum Visibility {
    PUBLIC,
    GROUP,
    PRIVATE
}
