/* (C) Edward Harman and contributors 2023 */
package org.ethelred.kv2.client;

import org.ethelred.kv2.template.StaticTemplates;
import org.ethelred.kv2.template.Templates;
import org.ethelred.roster.RosterParser;
import org.ethelred.roster.RosterParserImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teavm.jso.browser.TimerHandler;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.html.HTMLTextAreaElement;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        var app = new Main();
        app.run();
    }

    private RosterParser rosterParser = new RosterParserImpl();
    private Templates templates = new StaticTemplates();
    private String rosterId;
    private HTMLTextAreaElement editor;
    private HTMLElement viewer;
    private int bounceTimeout;
    private int remoteTimeout;

    private void run() {
        LOGGER.info("Editor starting");
        var document = Window.current().getDocument();
        var editorElement = document.getElementById("editor");
        if (editorElement == null) {
            LOGGER.info("editor element not found");
            return;
        }
        editor = editorElement.cast();
        rosterId = editor.getAttribute("data-roster-id");
        if (rosterId == null || rosterId.isBlank()) {
            LOGGER.info("roster ID not found");
            return;
        }
        viewer = document.getElementById("viewer");
        if (viewer == null) {
            LOGGER.info("viewer element not found");
        }
        editor.addEventListener("input", debounce(this::textChanged));
        textChanged();
    }

    private EventListener<?> debounce(TimerHandler task) {
        return e -> {
            if (bounceTimeout > 0) {
                Window.clearTimeout(bounceTimeout);
            }
            bounceTimeout = Window.setTimeout(task, 500);
        };
    }

    private void textChanged() {
        LOGGER.info("text changed");
        var text = editor.getValue();
        updateView(text);
        storeLocal(text);
        if (remoteTimeout == 0) {
            remoteTimeout = Window.setTimeout(this::storeRemote, 5000);
        }
    }

    private void storeRemote() {
        LOGGER.info("store remote");
        var text = editor.getValue();
        Api.updateRosterFields(rosterId, text, () -> {
            remoteTimeout = 0;
        });
    }

    private void storeLocal(String text) {
        LOGGER.info("store local");
        var localStorage = Window.current().getLocalStorage();
        localStorage.setItem("roster." + rosterId, text);
    }

    private void updateView(String text) {
        LOGGER.info("update view");
        var parsed = rosterParser.parseRoster(text);
        viewer.setInnerHTML(templates.roster(parsed).render());
    }
}
