package org.bulanda.trafficsimulation;

enum Direction {
    NORTH,
    EAST,
    SOUTH,
    WEST;

    public static Direction fromString(String s) {
        return valueOf(s.toUpperCase());
    }
}
