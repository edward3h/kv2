/* (C) Edward Harman and contributors 2022 */
package org.ethelred.kv2.models;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;

public interface Owned {
    @NonNull
    String ownerId();

    @NonNull
    Visibility visibility();

    default boolean isOwnedBy(@NonNull Owner owner) {
        return ownerId().equals(owner.id());
    }

    default boolean isVisibleTo(@Nullable Owner owner) {
        return visibility() == Visibility.PUBLIC || (owner != null && isOwnedBy(owner));
    }
}
