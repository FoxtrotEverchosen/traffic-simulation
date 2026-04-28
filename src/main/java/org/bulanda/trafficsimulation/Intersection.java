package org.bulanda.trafficsimulation;

import java.util.*;

public class Intersection {
    HashMap<Direction, List<Queue<Vehicle>>> lanes;
    Set<Direction> closedRoads;

    // default to 1 on no lanes count specified
    public Intersection() {
        this(1);
    }

    public Intersection(int laneCount) {
        this.lanes = new HashMap<>();
        this.closedRoads = new HashSet<>();
        for (Direction d : Direction.values()) {
            List<Queue<Vehicle>> roadLanes = new ArrayList<>();
            for (int i = 0; i < laneCount; i++) {
                roadLanes.add(new ArrayDeque<>());
            }
            lanes.put(d, roadLanes);
        }
    }

    void addVehicle(Vehicle vehicle) {
        List<Queue<Vehicle>> roadLanes = lanes.get(vehicle.startRoad());
        roadLanes.stream()
                .min(Comparator.comparingInt(Queue::size))
                .ifPresent(lane -> lane.add(vehicle));
    }

    void failRoad(Direction direction) {
        closedRoads.add(direction);
    }

    void fixRoad(Direction direction) {
        closedRoads.remove(direction);
    }

    List<String> removeVehicles(TrafficDirection trafficDirection) {
        List<String> departed = new ArrayList<>();
        for (Direction d : trafficDirection.directions) {
            if (closedRoads.contains(d)) {
                continue;
            }

            for (Queue<Vehicle> lane : lanes.get(d)) {
                Vehicle v = lane.poll();
                if (v != null) departed.add(v.vehicleId());
            }
        }
        return departed;
    }

    HashMap<Direction, Integer> getLoad() {
        HashMap<Direction, Integer> map = new HashMap<>();
        for (Direction d : Direction.values()) {
            if (closedRoads.contains(d)) {
                map.put(d, 0);
            } else {
                map.put(d, lanes.get(d).stream()
                        .mapToInt(Queue::size)
                        .sum());
            }
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
