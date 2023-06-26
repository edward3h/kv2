/* (C) Edward Harman and contributors 2023 */
package org.ethelred.kv2.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teavm.jso.ajax.XMLHttpRequest;
import org.teavm.jso.json.JSON;

public class Api {
    private static final Logger LOGGER = LoggerFactory.getLogger(Api.class);

    public void updateRosterFields(String rosterId, String text, Runnable onComplete) {
        LOGGER.info("begin api patch {}", rosterId);
        var url = "/abc/rosters/" + rosterId;
        var obj = SimpleMap.create();
        obj.put("body", text);
        var xhr = XMLHttpRequest.create();
        xhr.setOnReadyStateChange(() -> {
            if (xhr.getReadyState() != XMLHttpRequest.DONE) {
                return;
            }

            int statusGroup = xhr.getStatus() / 100;
            if (statusGroup != 2 && statusGroup != 3) {
                LOGGER.error("HTTP status: " + xhr.getStatus() + " " + xhr.getStatusText());
            } else {
                LOGGER.info("api patch ok");
            }
            onComplete.run();
        });
        xhr.open("PATCH", url);
        xhr.setRequestHeader("Content-Type", "application/json");
        xhr.send(JSON.stringify(obj));
    }
}
