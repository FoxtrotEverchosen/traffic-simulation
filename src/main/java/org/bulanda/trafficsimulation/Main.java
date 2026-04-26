package org.bulanda.trafficsimulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class Main {
    static void main(String[] args) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        JsonNode root = mapper.readTree(new File(args[0]));
        Controller.handleCommands(root);
    }
}
