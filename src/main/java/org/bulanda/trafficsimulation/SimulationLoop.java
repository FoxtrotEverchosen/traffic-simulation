package org.bulanda.trafficsimulation;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SimulationLoop {
    List<Map<String, List<String>>> runSim(JsonNode commands) {
        Intersection intersection = new Intersection();
        Controller controller = new Controller();
        TrafficDirection currentDirection = TrafficDirection.N_S;
        List<Map<String, List<String>>> stepStatuses = new ArrayList<>();
        int phaseTime = 1;

        for (JsonNode command : commands) {
            switch (command.get("type").asText()) {
                case "addVehicle" -> {
                    Vehicle v = new Vehicle(command.get("vehicleId").asText(),
                            Direction.valueOf(command.get("startRoad").asText().toUpperCase()),
                            Direction.valueOf(command.get("endRoad").asText().toUpperCase())
                    );

                    intersection.addVehicle(v);

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
