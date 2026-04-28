package org.bulanda.trafficsimulation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IntersectionTest {

    private Intersection intersection;

    @BeforeEach
    void setUp() {
        intersection = new Intersection();
    }

    @Test
    @DisplayName("addVehicle puts vehicle in correct lane")
    void addVehicleCorrectLane() {
        Vehicle v = new Vehicle("v1", Direction.NORTH, Direction.SOUTH);
        intersection.addVehicle(v);

        assertEquals(1, intersection.getLoad().get(Direction.NORTH));
        assertEquals(0, intersection.getLoad().get(Direction.SOUTH));
    }

    @Test
    @DisplayName("removeVehicles returns departed vehicle ids")
    void removeVehiclesReturnsDeparted() {
        intersection.addVehicle(new Vehicle("v1", Direction.NORTH, Direction.SOUTH));
        intersection.addVehicle(new Vehicle("v2", Direction.SOUTH, Direction.NORTH));

        List<String> departed = intersection.removeVehicles(TrafficDirection.N_S);

        assertTrue(departed.containsAll(List.of("v1", "v2")));
    }

    @Test
    @DisplayName("removeVehicles on empty lane returns empty list")
    void removeVehiclesEmptyLane() {
        List<String> departed = intersection.removeVehicles(TrafficDirection.N_S);
        assertTrue(departed.isEmpty());
    }

    @Test
    @DisplayName("getLoad reflects current queue sizes")
    void getLoadReflectsQueueSizes() {
        intersection.addVehicle(new Vehicle("v1", Direction.EAST, Direction.WEST));
        intersection.addVehicle(new Vehicle("v2", Direction.EAST, Direction.WEST));
        intersection.addVehicle(new Vehicle("v3", Direction.WEST, Direction.EAST));

        HashMap<Direction, Integer> load = intersection.getLoad();

        assertEquals(2, load.get(Direction.EAST));
        assertEquals(1, load.get(Direction.WEST));
        assertEquals(0, load.get(Direction.NORTH));
        assertEquals(0, load.get(Direction.SOUTH));
    }

    @Test
    @DisplayName("removeVehicles only removes from correct phase lanes")
    void removeVehiclesOnlyAffectsCorrectPhase() {
        intersection.addVehicle(new Vehicle("ns", Direction.NORTH, Direction.SOUTH));
        intersection.addVehicle(new Vehicle("ew", Direction.EAST, Direction.WEST));

        intersection.removeVehicles(TrafficDirection.N_S);

        assertEquals(0, intersection.getLoad().get(Direction.NORTH));
        assertEquals(1, intersection.getLoad().get(Direction.EAST));
    }

    @Test
    @DisplayName("vehicles spread across lanes on arrival")
    void vehiclesSpreadAcrossLanes() {
        Intersection intersection = new Intersection(2);
        intersection.addVehicle(new Vehicle("v1", Direction.NORTH, Direction.SOUTH));
        intersection.addVehicle(new Vehicle("v2", Direction.NORTH, Direction.SOUTH));

        assertEquals(2, intersection.getLoad().get(Direction.NORTH));

        List<String> departed = intersection.removeVehicles(TrafficDirection.N_S);
        assertTrue(departed.containsAll(List.of("v1", "v2")));
    }
}