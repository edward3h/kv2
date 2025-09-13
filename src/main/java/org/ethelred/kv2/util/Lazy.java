/* (C) Edward Harman and contributors 2022-2025 */
package org.ethelred.kv2.util;

import java.util.function.Supplier;

public class Lazy {
    private final Supplier<String> supplier;

    public Lazy(Supplier<String> supplier) {
        this.supplier = supplier;
    }

    static Lazy lazy(Supplier<String> supplier) {
        return new Lazy(supplier);
    }

    @Override
    public String toString() {
        return supplier.get();
    }
}
