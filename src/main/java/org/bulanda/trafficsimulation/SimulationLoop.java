package org.bulanda.trafficsimulation;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SimulationLoop {
    List<Map<String, List<String>>> runSim(JsonNode commands) {
        Intersection intersection = new Intersection();
        Controller controller = new Controller();
        List<Map<String, List<String>>> stepStatuses = new ArrayList<>();
        TrafficDirection currentDirection = TrafficDirection.N_S;
        int phaseTime = 1;

        for (JsonNode command : commands) {
            switch (command.get("type").asText()) {
                case "addVehicle" -> {
                    Vehicle v = new Vehicle(command.get("vehicleId").asText(),
                            Direction.fromString(command.get("startRoad").asText()),
                            Direction.fromString(command.get("endRoad").asText())
                    );

                    intersection.addVehicle(v);

                }
                case "failRoad" -> {
                    Direction direction = Direction.fromString(command.get("direction").asText());
                    intersection.failRoad(direction);

                }
                case "fixRoad" -> {
                    Direction direction = Direction.fromString(command.get("direction").asText());
                    intersection.fixRoad(direction);

                }
                case "step" -> {
                    TrafficDirection next = controller.setDirection(currentDirection, intersection.getLoad(), phaseTime);
                    phaseTime = next != currentDirection ? 1 : phaseTime + 1;
                    currentDirection = next;

                    List<String> departed = intersection.removeVehicles(currentDirection);
                    stepStatuses.add(Map.of("leftVehicles", departed));

                }
                default -> throw new IllegalArgumentException("Unknown command: " + command.get("type").asText());
            }
        }

        return stepStatuses;
    }
}
