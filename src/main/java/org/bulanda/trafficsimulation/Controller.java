package org.bulanda.trafficsimulation;

import java.util.HashMap;
import java.util.Set;

public class Controller {
    static final int MIN_PHASE_TIME = 1;
    static final int MAX_PHASE_TIME = 10;

    TrafficDirection setDirection(TrafficDirection current, HashMap<Direction, Integer> trafficLoad, int phaseTime, Set<Direction> emergencyDirections) {
        for (TrafficDirection phase : TrafficDirection.values()) {
            if (phase.directions.stream().anyMatch(emergencyDirections::contains)) {
                return phase;
            }
        }

        int currentLoad = loadFor(current, trafficLoad);
        int otherLoad = loadFor(current.other(), trafficLoad);

        if (phaseTime < MIN_PHASE_TIME) return current;
        if (phaseTime > MAX_PHASE_TIME && otherLoad != 0) return current.other();

        return otherLoad > currentLoad * 1.5 ? current.other() : current;
    }

    private int loadFor(TrafficDirection direction, HashMap<Direction, Integer> trafficLoad) {
        return direction.directions.stream()
                .mapToInt(d -> trafficLoad.getOrDefault(d, 0))
                .sum();
    }
}