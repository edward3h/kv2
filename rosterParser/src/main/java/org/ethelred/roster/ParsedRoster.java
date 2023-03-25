/* (C) Edward Harman and contributors 2023 */
package org.ethelred.roster;

import org.teavm.jso.JSObject;

public interface ParsedRoster extends JSObject {
    String[] getStyles();

    Level getRoot();
}
