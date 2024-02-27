/* (C) Edward Harman and contributors 2023-2024 */
package org.ethelred.kv2.client;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.dom.html.HTMLTextAreaElement;

public abstract class CodeMirror implements JSObject {
    @JSBody(
            params = {"textarea"},
            // language=js
            script = """
    return CodeMirror.fromTextArea(textarea, {
        lineNumbers: true
    });
    """)
    public static native CodeMirror fromTextArea(HTMLTextAreaElement textarea);

    public abstract void save();

    public abstract void on(String type, CodeMirrorEventListener callback);

    public abstract String getValue();
}
