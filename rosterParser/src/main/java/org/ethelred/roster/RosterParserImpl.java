/* (C) Edward Harman and contributors 2023-2026 */
package org.ethelred.roster;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

public class RosterParserImpl implements RosterParser {
    private static final Pattern INDENTS = Pattern.compile("^\\s*");
    private static final Pattern POINTS = Pattern.compile("\\s\\[\\s*(-?\\d+)\\s*]");
    private static final Pattern MULTIPLIER = Pattern.compile("\\s[x*]\\s*(\\d+)");
    private static final Pattern ANNOTATION = Pattern.compile(".+:.+");
    private static final Pattern HEADER = Pattern.compile("^\\s*(#+)\\s*(.+)");
    private static final Pattern STYLE = Pattern.compile("^\\s*!\\s*(.+)");

    @Override
    public ParsedRosterImpl parseRoster(String input) {
        var lineInfos = Stream.of(input.split("\n"))
                .map(this::parseLineInfo)
                .filter(lineInfo -> !lineInfo.isEmpty())
                .toList();

        var styles =
                lineInfos.stream().filter(LineInfo::isStyle).map(LineInfo::text).toList();
        var others = lineInfos.stream().filter(l -> !l.isStyle()).toList();
        var root = build(others);

        return new ParsedRosterImpl(styles, root);
    }

    @SuppressWarnings("initialization")
    private LevelImpl build(List<LineInfo> lineInfos) {
        var indents =
                lineInfos.stream().map(LineInfo::indent).distinct().sorted().collect(Collectors.toList());
        if (indents.isEmpty()) {
            indents = List.of(0);
        }
        var root = new LevelImpl();
        var parents = new LinkedList<>(List.of(root));
        var last = root;
        for (var li : lineInfos) {
            if (!(li instanceof NormalLineInfo || li instanceof HeaderInfo)) {
                continue;
            }
            var liDepth = indents.indexOf(li.indent());
            var liLevel = levelFromLineInfo(li);
            while (liDepth < (parents.size() - 1)) {
                parents.pop();
            }
            if (liDepth > (parents.size() - 1)) {
                parents.push(last);
            }
            var parent = parents.getFirst();
            if (parent == null) throw new IllegalStateException("parent stack is empty");
            parent.addChild(liLevel);
            last = liLevel;
        }
        return root;
    }

    private LevelImpl levelFromLineInfo(LineInfo li) {
        if (li instanceof HeaderInfo) {
            return new LevelImpl(((HeaderInfo) li).header(), li.text());
        } else if (li instanceof NormalLineInfo) {
            return new LevelImpl(
                    li.text(),
                    ((NormalLineInfo) li).annotation(),
                    ((NormalLineInfo) li).cost(),
                    ((NormalLineInfo) li).multiplier());
        }
        throw new IllegalArgumentException(li.toString());
    }

    private LineInfo parseLineInfo(String s) {
        if (s.isBlank()) {
            return EMPTY;
        }
        var styleMatch = find(STYLE, s);
        if (styleMatch != null) {
            var g1 = styleMatch.group(1);
            if (g1 == null) throw new IllegalStateException("STYLE group 1 missing");
            return new StyleInfo(g1);
        }
        var indent = 0;
        var indentMatch = find(INDENTS, s);
        if (indentMatch != null) {
            indent = indentMatch.group().length();
        }
        var headerMatch = find(HEADER, s);
        if (headerMatch != null) {
            var g1 = headerMatch.group(1);
            var g2 = headerMatch.group(2);
            if (g1 == null || g2 == null) throw new IllegalStateException("HEADER groups missing");
            return new HeaderInfo(g2, g1.length(), indent);
        }
        var annotation = find(ANNOTATION, s) != null;
        var cost = 0;
        var multiplier = 1;
        var pointsMatch = find(POINTS, s);
        if (pointsMatch != null) {
            var g1 = pointsMatch.group(1);
            if (g1 == null) throw new IllegalStateException("POINTS group 1 missing");
            cost = Integer.parseInt(g1, 10);
            var multiplierMatch = find(MULTIPLIER, s);
            if (multiplierMatch != null) {
                var mg1 = multiplierMatch.group(1);
                if (mg1 == null) throw new IllegalStateException("MULTIPLIER group 1 missing");
                multiplier = Integer.parseInt(mg1, 10);
            }
        }
        return new NormalLineInfo(s.trim(), indent, annotation, cost, multiplier);
    }

    private @Nullable MatchResult find(Pattern p, String s) {
        var matcher = p.matcher(s);
        return matcher.find() ? matcher.toMatchResult() : null;
    }

    interface LineInfo {

        default boolean isEmpty() {
            return false;
        }

        default boolean isStyle() {
            return false;
        }

        String text();

        default boolean isHeader() {
            return false;
        }

        default int indent() {
            return 0;
        }
    }

    private static final LineInfo EMPTY = new LineInfo() {
        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public String text() {
            throw new UnsupportedOperationException();
        }
    };

    private record StyleInfo(String text) implements LineInfo {
        @Override
        public boolean isStyle() {
            return true;
        }
    }

    private record HeaderInfo(String text, int header, int indent) implements LineInfo {
        @Override
        public boolean isHeader() {
            return true;
        }
    }

    private record NormalLineInfo(String text, int indent, boolean annotation, int cost, int multiplier)
            implements LineInfo {}
}
