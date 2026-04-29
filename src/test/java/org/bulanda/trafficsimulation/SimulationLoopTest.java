package org.bulanda.trafficsimulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SimulationLoopTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final SimulationLoop loop = new SimulationLoop(1);

    @Test
    @DisplayName("test works with provided sample")
    void sample() throws Exception {
        String input = """
                {
                  "commands": [
                    { "type": "addVehicle", "vehicleId": "vehicle1", "startRoad": "south", "endRoad": "north" },
                    { "type": "addVehicle", "vehicleId": "vehicle2", "startRoad": "north", "endRoad": "south" },
                    { "type": "step" },
                    { "type": "step" },
                    { "type": "addVehicle", "vehicleId": "vehicle3", "startRoad": "west", "endRoad": "south" },
                    { "type": "addVehicle", "vehicleId": "vehicle4", "startRoad": "west", "endRoad": "south" },
                    { "type": "step" },
                    { "type": "step" }
                  ]
                }
                """;

        JsonNode commands = mapper.readTree(input).get("commands");
        List<Map<String, List<String>>> result = loop.runSim(commands);

        assertEquals(4, result.size());
        assertTrue(result.get(0).get("leftVehicles").containsAll(List.of("vehicle1", "vehicle2")));
        assertTrue(result.get(1).get("leftVehicles").isEmpty());
        assertTrue(result.get(2).get("leftVehicles").contains("vehicle3"));
        assertTrue(result.get(3).get("leftVehicles").contains("vehicle4"));
    }

    @Test
    @DisplayName("empty commands returns empty stepStatuses")
    void emptyCommands() throws Exception {
        JsonNode commands = mapper.readTree("{\"commands\": []}").get("commands");
        List<Map<String, List<String>>> result = loop.runSim(commands);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("multiple vehicles on same line depart one per step")
    void oneVehiclePerStepPerRoad() throws Exception {
        String input = """
                {
                  "commands": [
                    { "type": "addVehicle", "vehicleId": "v1", "startRoad": "north", "endRoad": "south" },
                    { "type": "addVehicle", "vehicleId": "v2", "startRoad": "north", "endRoad": "south" },
                    { "type": "addVehicle", "vehicleId": "v3", "startRoad": "north", "endRoad": "south" },
                    { "type": "step" },
                    { "type": "step" },
                    { "type": "step" }
                  ]
                }
                """;

        JsonNode commands = mapper.readTree(input).get("commands");
        List<Map<String, List<String>>> result = loop.runSim(commands);

        assertEquals(List.of("v1"), result.get(0).get("leftVehicles"));
        assertEquals(List.of("v2"), result.get(1).get("leftVehicles"));
        assertEquals(List.of("v3"), result.get(2).get("leftVehicles"));
    }

    @Test
    @DisplayName("two lane intersection doubles throughput per step")
    void twoLanesDoublesOutput() throws Exception {
        SimulationLoop twoLaneLoop = new SimulationLoop(2);
        String input = """
                {
                  "commands": [
                    { "type": "addVehicle", "vehicleId": "v1", "startRoad": "north", "endRoad": "south" },
                    { "type": "addVehicle", "vehicleId": "v2", "startRoad": "north", "endRoad": "south" },
                    { "type": "addVehicle", "vehicleId": "v3", "startRoad": "south", "endRoad": "north" },
                    { "type": "addVehicle", "vehicleId": "v4", "startRoad": "south", "endRoad": "north" },
                    { "type": "step" }
                  ]
                }
                """;

        JsonNode commands = mapper.readTree(input).get("commands");
        List<Map<String, List<String>>> result = twoLaneLoop.runSim(commands);

        assertTrue(result.get(0).get("leftVehicles").containsAll(List.of("v1", "v2", "v3", "v4")));
    }

    @Test
    @DisplayName("failed road vehicles do not depart")
    void failedRoadDoesNotDepart() throws Exception {
        String input = """
                {
                  "commands": [
                    { "type": "addVehicle", "vehicleId": "v1", "startRoad": "north", "endRoad": "south" },
                    { "type": "failRoad", "direction": "north" },
                    { "type": "step" }
                  ]
                }
                """;

        JsonNode commands = mapper.readTree(input).get("commands");
        List<Map<String, List<String>>> result = loop.runSim(commands);

        assertTrue(result.get(0).get("leftVehicles").isEmpty());
    }

    @Test
    @DisplayName("fixed road resumes departures after repair")
    void fixedRoadResumesAfterRepair() throws Exception {
        String input = """
                {
                  "commands": [
                    { "type": "addVehicle", "vehicleId": "v1", "startRoad": "north", "endRoad": "south" },
                    { "type": "failRoad", "direction": "north" },
                    { "type": "step" },
                    { "type": "fixRoad", "direction": "north" },
                    { "type": "step" }
                  ]
                }
                """;

        JsonNode commands = mapper.readTree(input).get("commands");
        List<Map<String, List<String>>> result = loop.runSim(commands);

        assertTrue(result.get(0).get("leftVehicles").isEmpty());
        assertTrue(result.get(1).get("leftVehicles").contains("v1"));
    }

    @Test
    @DisplayName("failed road is excluded from load calculation")
    void failedRoadExcludedFromLoad() {
        Intersection intersection = new Intersection();
        intersection.addVehicle(new Vehicle("v1", Direction.NORTH, Direction.SOUTH));
        intersection.addVehicle(new Vehicle("v2", Direction.NORTH, Direction.SOUTH));
        intersection.failRoad(Direction.NORTH);

        assertEquals(0, intersection.getLoad().get(Direction.NORTH));
    }

    @Test
    @DisplayName("emergency vehicle forces immediate phase switch")
    void emergencyVehicleForcesPhaseSwitch() throws Exception {
        String input = """
                {
                  "commands": [
                    { "type": "addVehicle", "vehicleId": "regular", "startRoad": "north", "endRoad": "south" },
                    { "type": "addEmergencyVehicle", "vehicleId": "emergency", "startRoad": "west", "endRoad": "east" },
                    { "type": "step" }
                  ]
                }
                """;

        JsonNode commands = mapper.readTree(input).get("commands");
        List<Map<String, List<String>>> result = loop.runSim(commands);
        
        assertTrue(result.get(0).get("leftVehicles").contains("emergency"));
        assertFalse(result.get(0).get("leftVehicles").contains("regular"));
    }

    @Test
    @DisplayName("emergency vehicle departs before regular vehicles in same lane")
    void emergencyVehicleJumpsQueue() throws Exception {
        String input = """
                {
                  "commands": [
                    { "type": "addVehicle", "vehicleId": "regular", "startRoad": "north", "endRoad": "south" },
                    { "type": "addEmergencyVehicle", "vehicleId": "emergency", "startRoad": "north", "endRoad": "south" },
                    { "type": "step" }
                  ]
                }
                """;

        JsonNode commands = mapper.readTree(input).get("commands");
        List<Map<String, List<String>>> result = loop.runSim(commands);

        assertTrue(result.get(0).get("leftVehicles").contains("emergency"));
        assertFalse(result.get(0).get("leftVehicles").contains("regular"));
    }

    @Test
    @DisplayName("normal algorithm resumes after emergency vehicle departs")
    void normalAlgorithmResumesAfterEmergency() throws Exception {
        String input = """
                {
                  "commands": [
                    { "type": "addEmergencyVehicle", "vehicleId": "emergency", "startRoad": "west", "endRoad": "east" },
                    { "type": "addVehicle", "vehicleId": "regular", "startRoad": "north", "endRoad": "south" },
                    { "type": "step" },
                    { "type": "step" },
                    { "type": "step" }
                  ]
                }
                """;

        JsonNode commands = mapper.readTree(input).get("commands");
        List<Map<String, List<String>>> result = loop.runSim(commands);

        assertTrue(result.get(0).get("leftVehicles").contains("emergency"));
        assertTrue(result.get(1).get("leftVehicles").isEmpty());
        assertTrue(result.get(2).get("leftVehicles").contains("regular"));
    }

    @Test
    @DisplayName("second emergency vehicle retains priority after first departs")
    void multipleEmergencyVehiclesSameLane() throws Exception {
        String input = """
                {
                  "commands": [
                    { "type": "addEmergencyVehicle", "vehicleId": "emergency1", "startRoad": "north", "endRoad": "south" },
                    { "type": "addEmergencyVehicle", "vehicleId": "emergency2", "startRoad": "north", "endRoad": "south" },
                    { "type": "addVehicle", "vehicleId": "regular", "startRoad": "east", "endRoad": "west" },
                    { "type": "step" },
                    { "type": "step" }
                  ]
                }
                """;

        JsonNode commands = mapper.readTree(input).get("commands");
        List<Map<String, List<String>>> result = loop.runSim(commands);

        assertTrue(result.get(0).get("leftVehicles").stream()
                .anyMatch(id -> id.equals("emergency1") || id.equals("emergency2")));
        assertFalse(result.get(0).get("leftVehicles").contains("regular"));

        assertTrue(result.get(1).get("leftVehicles").stream()
                .anyMatch(id -> id.equals("emergency1") || id.equals("emergency2")));
        assertFalse(result.get(1).get("leftVehicles").contains("regular"));
    }
}