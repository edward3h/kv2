/* (C) Edward Harman and contributors 2023 */
package org.ethelred.kv2.client;

import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

@JSFunctor
public interface CodeMirrorEventListener extends JSObject {
    void onEvent(CodeMirror instance, JSObject arguments);
}
