/* (C) Edward Harman and contributors 2023 */
package org.ethelred.kv2.models;

import io.micronaut.context.annotation.ConfigurationProperties;
import java.util.List;
import org.ethelred.kv2.viewmodels.Assets;

@ConfigurationProperties("assets")
public class AssetsConfiguration implements Assets {
    private List<String> styles;
    private List<String> scripts;

    public List<String> getStyles() {
        return styles;
    }

    public void setStyles(List<String> styles) {
        this.styles = styles;
    }

    public List<String> getScripts() {
        return scripts;
    }

    public void setScripts(List<String> scripts) {
        this.scripts = scripts;
    }

    @Override
    public List<String> styles() {
        return getStyles();
    }

    @Override
    public List<String> scripts() {
        return getScripts();
    }
}
