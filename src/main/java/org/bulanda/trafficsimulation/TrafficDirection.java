package org.bulanda.trafficsimulation;

import java.util.Set;

enum TrafficDirection {
    N_S(Set.of(Direction.NORTH, Direction.SOUTH)),
    E_W(Set.of(Direction.EAST, Direction.WEST));

    final Set<Direction> directions;

    TrafficDirection(Set<Direction> directions) {
        this.directions = directions;
    }

    public TrafficDirection other() {
        return this == N_S ? E_W : N_S;
    }
}