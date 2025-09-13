/* (C) Edward Harman and contributors 2023-2025 */
package org.ethelred.roster;

import java.util.ArrayList;
import java.util.List;

public class ParsedRosterImpl implements ParsedRoster {
    private final List<String> styles;

    @Override
    public String[] getStyles() {
        return styles.toArray(String[]::new);
    }

    @Override
    public Level getRoot() {
        return root;
    }

    private final LevelImpl root;

    public ParsedRosterImpl(List<String> styles, LevelImpl root) {
        this.styles = new ArrayList<>(styles);
        this.root = root;
    }
}
