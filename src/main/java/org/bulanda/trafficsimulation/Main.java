package org.bulanda.trafficsimulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {
    static void main(String[] args) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File(args[0]));
        Intersection intersection = new Intersection();
        Controller controller = new Controller();
        TrafficDirection currentDirection = TrafficDirection.N_S;
        List<Map<String, List<String>>> stepStatuses = new ArrayList<>();
        int phaseTime = 1;

        for (JsonNode command : root.get("commands")) {
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

        Map<String, Object> output = Map.of("stepStatuses", stepStatuses);
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(args[1]), output);

    }
}
