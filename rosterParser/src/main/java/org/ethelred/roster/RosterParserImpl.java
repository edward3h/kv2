/* (C) Edward Harman and contributors 2023 */
package org.ethelred.roster;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RosterParserImpl implements RosterParser {
    private static final Pattern BLANK = Pattern.compile("^\\s*$");
    private static final Pattern INDENTS = Pattern.compile("^\\s*");
    private static final Pattern POINTS = Pattern.compile("\\s\\[\\s*(\\d+)\\s*]");
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
            parents.getLast().addChild(liLevel);
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
        if (find(BLANK, s)) {
            return EMPTY;
        }
        if (find(STYLE, s)) {
            return new StyleInfo(matchResult.group(1));
        }
        var indent = 0;
        if (find(INDENTS, s)) {
            indent = matchResult.group().length();
        }
        if (find(HEADER, s)) {
            return new HeaderInfo(matchResult.group(2), matchResult.group(1).length(), indent);
        }
        var annotation = find(ANNOTATION, s);
        var cost = 0;
        var multiplier = 1;
        if (find(POINTS, s)) {
            cost = Integer.parseInt(matchResult.group(1), 10);
            if (find(MULTIPLIER, s)) {
                multiplier = Integer.parseInt(matchResult.group(1), 10);
            }
        }
        return new NormalLineInfo(s.trim(), indent, annotation, cost, multiplier);
    }

    private MatchResult matchResult;

    private boolean find(Pattern p, String s) {
        var matcher = p.matcher(s);
        if (matcher.find()) {
            matchResult = matcher.toMatchResult();
            return true;
        } else {
            matchResult = null;
            return false;
        }
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
            return null;
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
