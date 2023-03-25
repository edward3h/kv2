/* (C) Edward Harman and contributors 2023 */
package org.ethelred.roster;

import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

@JSFunctor
public interface RosterParser extends JSObject {
    ParsedRoster parseRoster(String input);
}
