package org.bulanda.trafficsimulation;

import java.util.*;

public class Intersection {
    HashMap<Direction, Queue<Vehicle>> lanes;

    public Intersection() {
        this.lanes = new HashMap<>();
        for (Direction d : Direction.values()) {
            lanes.put(d, new ArrayDeque<>());
        }
    }

    void addVehicle(Vehicle vehicle) {
        lanes.get(vehicle.startRoad()).add(vehicle);
    }

    List<String> removeVehicles(TrafficDirection direction) {
        List<String> departed = new ArrayList<>();
        for (Direction d : direction.directions) {
            Vehicle v = lanes.get(d).poll();
            if (v != null) departed.add(v.vehicleId());
        }
        return departed;
    }

    HashMap<Direction, Integer> getLoad() {
        HashMap<Direction, Integer> map = new HashMap<>();
        for (Direction d : Direction.values()) {
            map.put(d, lanes.get(d).size());
        }
        return map;
    }

    @Override
    public String toString() {
        return "Intersection{" +
                "lanes=" + lanes +
                '}';
    }
}
