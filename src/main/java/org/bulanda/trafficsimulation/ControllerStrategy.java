package org.bulanda.trafficsimulation;

import java.util.HashMap;
import java.util.Set;

interface ControllerStrategy {
    TrafficDirection setDirection(TrafficDirection current,
                                  HashMap<Direction, Integer> trafficLoad,
                                  int phaseTime,
                                  Set<Direction> emergencyDirections);
}