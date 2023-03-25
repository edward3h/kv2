/* (C) Edward Harman and contributors 2023 */
package org.ethelred.roster;

import org.teavm.jso.JSBody;

public class Loader {
    public static void main(String[] args) {
        RosterParser parser = new RosterParserImpl();
        registerGlobal(parser);
    }

    @JSBody(
            params = {"parseRoster"},
            script = "globalThis['parseRoster'] = parseRoster")
    static native void registerGlobal(RosterParser parseRoster);
}
