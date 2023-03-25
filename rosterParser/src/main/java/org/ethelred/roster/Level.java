/* (C) Edward Harman and contributors 2023 */
package org.ethelred.roster;

import org.teavm.jso.JSObject;

public interface Level extends JSObject {
    Level[] getChildren();

    String getText();

    Integer getHeader();

    boolean isAnnotation();

    int getCost();

    int getMultiplier();

    int getTotal();
}
