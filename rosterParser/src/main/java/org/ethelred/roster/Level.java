/* (C) Edward Harman and contributors 2023-2026 */
package org.ethelred.roster;

import org.jspecify.annotations.Nullable;

public interface Level {
    Level[] getChildren();

    String getText();

    @Nullable
    Integer getHeader();

    boolean isAnnotation();

    int getCost();

    int getMultiplier();

    int getTotal();

    boolean isRoot();
}
