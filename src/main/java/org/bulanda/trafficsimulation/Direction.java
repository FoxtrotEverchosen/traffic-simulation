package org.bulanda.trafficsimulation;

public enum Direction {
    NORTH,
    EAST,
    SOUTH,
    WEST;

    public static Direction fromString(String s) {
        return valueOf(s.toUpperCase());
    }
}
