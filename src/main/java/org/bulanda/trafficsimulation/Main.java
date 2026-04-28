package org.bulanda.trafficsimulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File(args[0]));

        int nRoads = args.length > 2 ? Integer.parseInt(args[2]) : 1;
        SimulationLoop loop = new SimulationLoop(nRoads);
        List<Map<String, List<String>>> stepStatuses = loop.runSim(root.get("commands"));

        Map<String, Object> output = Map.of("stepStatuses", stepStatuses);
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(args[1]), output);

    }
}
