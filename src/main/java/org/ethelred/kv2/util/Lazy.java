/* (C) Edward Harman and contributors 2022 */
package org.ethelred.kv2.util;

import java.util.function.Supplier;

public class Lazy {
    private final Supplier<String> supplier;

    public Lazy(Supplier<String> supplier) {
        this.supplier = supplier;
    }

    @Override
    public String toString() {
        return supplier.get();
    }
}
