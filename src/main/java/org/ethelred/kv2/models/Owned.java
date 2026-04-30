/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.models;

import org.jspecify.annotations.Nullable;

public interface Owned {
    String ownerId();

    Visibility visibility();

    default boolean isOwnedBy(Owner owner) {
        return ownerId().equals(owner.id());
    }

    default boolean isVisibleTo(@Nullable Owner owner) {
        return visibility() == Visibility.PUBLIC || (owner != null && isOwnedBy(owner));
    }
}
