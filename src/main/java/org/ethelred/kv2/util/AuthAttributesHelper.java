/* (C) Edward Harman and contributors 2022 */
package org.ethelred.kv2.util;

import java.util.*;

public class AuthAttributesHelper {
    private AuthAttributesHelper() {}

    private static Map<String, Object> map0(Object... input) {
        if (input.length % 2 != 0) {
            throw new IllegalArgumentException("Expected an even number of arguments");
        }

        var r = new HashMap<String, Object>(input.length / 2);
        for (var i = 0; i < input.length; i += 2) {
            var k = (String) input[i];
            var v = input[i + 1];
            if (v instanceof Optional<?> optionalV) {
                v = optionalV.orElse(null);
            }
            if (k == null || v == null) {
                continue;
            }
            r.put(k, v);
        }
        return r;
    }

    public static Map<String, Object> map(String k1, Object v1) {
        return map0(k1, v1);
    }

    public static Map<String, Object> map(String k1, Object v1, String k2, Object v2) {
        return map0(k1, v1, k2, v2);
    }

    public static Map<String, Object> map(String k1, Object v1, String k2, Object v2, String k3, Object v3) {
        return map0(k1, v1, k2, v2, k3, v3);
    }

    public static Map<String, Object> map(
            String k1, Object v1, String k2, Object v2, String k3, Object v3, String k4, Object v4) {
        return map0(k1, v1, k2, v2, k3, v3, k4, v4);
    }

    public static Map<String, Object> map(
            String k1,
            Object v1,
            String k2,
            Object v2,
            String k3,
            Object v3,
            String k4,
            Object v4,
            String k5,
            Object v5) {
        return map0(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5);
    }

    public static Map<String, Object> map(
            String k1,
            Object v1,
            String k2,
            Object v2,
            String k3,
            Object v3,
            String k4,
            Object v4,
            String k5,
            Object v5,
            String k6,
            Object v6) {
        return map0(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6);
    }

    public static Map<String, Object> map(
            String k1,
            Object v1,
            String k2,
            Object v2,
            String k3,
            Object v3,
            String k4,
            Object v4,
            String k5,
            Object v5,
            String k6,
            Object v6,
            String k7,
            Object v7) {
        return map0(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7);
    }

    public static Map<String, Object> map(
            String k1,
            Object v1,
            String k2,
            Object v2,
            String k3,
            Object v3,
            String k4,
            Object v4,
            String k5,
            Object v5,
            String k6,
            Object v6,
            String k7,
            Object v7,
            String k8,
            Object v8) {
        return map0(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8);
    }

    public static Map<String, Object> map(
            String k1,
            Object v1,
            String k2,
            Object v2,
            String k3,
            Object v3,
            String k4,
            Object v4,
            String k5,
            Object v5,
            String k6,
            Object v6,
            String k7,
            Object v7,
            String k8,
            Object v8,
            String k9,
            Object v9) {
        return map0(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9);
    }
}
