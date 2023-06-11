/* (C) Edward Harman and contributors 2023 */
package org.ethelred.kv2.util;

public interface DebugMapper<T> {
    Class<T> supportedType();

    String inspect(T object);
}
