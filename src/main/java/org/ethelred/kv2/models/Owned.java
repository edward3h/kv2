/* (C) Edward Harman and contributors 2022 */
package org.ethelred.kv2.models;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;

public interface Owned {
    @NonNull
    User owner();

    @NonNull
    Visibility visibility();

    default boolean isOwnedBy(@NonNull User user) {
        return user.id().equals(owner().id());
    }

    default boolean isVisibleTo(@Nullable User user) {
        // TODO group
        return visibility() == Visibility.PUBLIC || (user != null && isOwnedBy(user));
    }
}
