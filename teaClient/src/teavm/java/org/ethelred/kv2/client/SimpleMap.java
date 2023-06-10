/* (C) Edward Harman and contributors 2023 */
package org.ethelred.kv2.client;

import org.teavm.jso.JSIndexer;
import org.teavm.jso.JSObject;
import org.teavm.jso.core.JSObjects;

public interface SimpleMap extends JSObject {
    static SimpleMap create() {
        return JSObjects.create();
    }

    @JSIndexer
    String get(String key);

    @JSIndexer
    void put(String key, String value);
}
