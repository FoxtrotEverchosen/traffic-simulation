package org.bulanda.trafficsimulation;

import com.fasterxml.jackson.databind.JsonNode;

public class Controller {
    public static void handleCommands(JsonNode root) {
        for (JsonNode command : root.get("commands")) {
            switch (command.get("type").asText()) {
                case "addVehicle" -> {
                    System.out.println("Inspecting vehicle of ID: " + command.get("vehicleId").asText() +
                            "\nStarting at: " + command.get("startRoad").asText() +
                            "\nDestination: " + command.get("endRoad").asText() + "\n"
                    );
                }
                case "step" -> {
                    System.out.println("Called step function" + "\n");
                }
                default -> throw new IllegalArgumentException("Unknown command: " + command.get("type").asText());
            }
        }
    }
}
