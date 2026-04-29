package org.bulanda.trafficsimulation;

import java.util.HashMap;
import java.util.Set;

class Controller {
    private final ControllerStrategy strategy;

    Controller() {
        this(new ThresholdStrategy());
    }

    Controller(ControllerStrategy strategy) {
        this.strategy = strategy;
    }

    TrafficDirection setDirection(TrafficDirection current,
                                  HashMap<Direction, Integer> trafficLoad,
                                  int phaseTime,
                                  Set<Direction> emergencyDirections) {
        return strategy.setDirection(current, trafficLoad, phaseTime, emergencyDirections);
    }
}