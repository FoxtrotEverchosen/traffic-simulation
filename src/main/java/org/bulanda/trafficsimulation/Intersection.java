package org.bulanda.trafficsimulation;

import java.util.*;

public class Intersection {
    private final HashMap<Direction, List<Queue<Vehicle>>> lanes;
    private final Set<Direction> closedDirections;
    private final Set<String> emergencyVehicleIds;
    private final Set<Direction> emergencyDirections;

    // default to 1 on no lanes count specified
    public Intersection() {
        this(1);
    }

    public Intersection(int laneCount) {
        this.lanes = new HashMap<>();
        this.emergencyDirections = new HashSet<>();
        this.emergencyVehicleIds = new HashSet<>();
        this.closedDirections = new HashSet<>();

        for (Direction d : Direction.values()) {
            List<Queue<Vehicle>> roadLanes = new ArrayList<>();
            for (int i = 0; i < laneCount; i++) {
                roadLanes.add(new ArrayDeque<>());
            }
            lanes.put(d, roadLanes);
        }
    }

    Set<Direction> getEmergencyDirections() {
        return this.emergencyDirections;
    }

    void addVehicle(Vehicle vehicle) {
        List<Queue<Vehicle>> roadLanes = lanes.get(vehicle.startRoad());
        roadLanes.stream()
                .min(Comparator.comparingInt(Queue::size))
                .ifPresent(lane -> lane.add(vehicle));
    }

    void failRoad(Direction direction) {
        closedDirections.add(direction);
    }

    void fixRoad(Direction direction) {
        closedDirections.remove(direction);
    }

    void addEmergencyVehicle(Vehicle vehicle) {
        // Emergency vehicle will be put at the front of the queue
        // to simulate it having priority in traffic
        emergencyVehicleIds.add(vehicle.vehicleId());
        emergencyDirections.add(vehicle.startRoad());
        ((ArrayDeque<Vehicle>) lanes.get(vehicle.startRoad()).get(0)).addFirst(vehicle);
    }

    List<String> removeVehicles(TrafficDirection trafficDirection) {
        List<String> departed = new ArrayList<>();
        for (Direction d : trafficDirection.directions) {
            if (closedDirections.contains(d)) {
                continue;
            }

            for (Queue<Vehicle> lane : lanes.get(d)) {
                Vehicle v = lane.poll();
                if (v != null) {
                    departed.add(v.vehicleId());
                    emergencyVehicleIds.remove(v.vehicleId());
                }
            }

            boolean emergencyStillWaiting = lanes.get(d).stream()
                    .flatMap(Collection::stream)
                    .anyMatch(vehicle -> emergencyVehicleIds.contains(vehicle.vehicleId()));
            if (!emergencyStillWaiting) emergencyDirections.remove(d);
        }
        return departed;
    }

    HashMap<Direction, Integer> getLoad() {
        HashMap<Direction, Integer> map = new HashMap<>();
        for (Direction d : Direction.values()) {
            if (closedDirections.contains(d)) {
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
