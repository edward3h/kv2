/* (C) Edward Harman and contributors 2023-2025 */
package org.ethelred.roster;

public interface Level {
    Level[] getChildren();

    String getText();

    Integer getHeader();

    boolean isAnnotation();

    int getCost();

    int getMultiplier();

    int getTotal();

    boolean isRoot();
}
