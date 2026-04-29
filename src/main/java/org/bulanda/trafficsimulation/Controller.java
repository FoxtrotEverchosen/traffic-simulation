package org.bulanda.trafficsimulation;

import java.util.HashMap;
import java.util.Set;

class Controller {
    private final ControllerStrategy strategy;

    Controller() {
        this(new ThresholdStrategy());
    }

    Controller(ControllerStrategy strategy) {
        this.strategy = strategy;  // only assigned here, chained constructor calls this
    }

    TrafficDirection setDirection(TrafficDirection current,
                                  HashMap<Direction, Integer> trafficLoad,
                                  int phaseTime,
                                  Set<Direction> emergencyDirections) {
        return strategy.setDirection(current, trafficLoad, phaseTime, emergencyDirections);
    }
}