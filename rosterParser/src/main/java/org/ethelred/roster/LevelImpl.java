/* (C) Edward Harman and contributors 2023 */
package org.ethelred.roster;

import java.util.ArrayList;
import java.util.List;

public class LevelImpl implements Level {
    @Override
    public Level[] getChildren() {
        return children.toArray(Level[]::new);
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public Integer getHeader() {
        return header;
    }

    @Override
    public boolean isAnnotation() {
        return annotation;
    }

    @Override
    public int getCost() {
        return cost;
    }

    @Override
    public int getMultiplier() {
        return multiplier;
    }

    private final List<Level> children = new ArrayList<>();
    private String text;
    private Integer header;
    private boolean annotation;
    private int cost;
    private int multiplier;

    LevelImpl(int header, String text) {
        this.header = header;
        this.text = text;
    }

    LevelImpl(String text, boolean annotation, int cost, int multiplier) {
        this.text = text;
        this.annotation = annotation;
        this.cost = cost;
        this.multiplier = multiplier;
    }

    LevelImpl() {}

    public void addChild(LevelImpl level) {
        children.add(level);
    }

    @Override
    public int getTotal() {
        return cost * multiplier + children.stream().mapToInt(Level::getTotal).sum();
    }
}
