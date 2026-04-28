package org.bulanda.trafficsimulation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class ControllerTest {

    private Controller controller;

    @BeforeEach
    void setUp() {
        controller = new Controller();
    }

    @Test
    @DisplayName("heavy E_W load causes switch from N_S")
    void switchOnImbalance() {
        HashMap<Direction, Integer> load = new HashMap<>();
        load.put(Direction.NORTH, 0);
        load.put(Direction.SOUTH, 0);
        load.put(Direction.EAST, 4);
        load.put(Direction.WEST, 4);

        TrafficDirection result = controller.setDirection(
                TrafficDirection.N_S, load, Controller.MIN_PHASE_TIME + 1
        );

        assertEquals(TrafficDirection.E_W, result);
    }

    @Test
    @DisplayName("phase switches after max phase time when other load is non-zero")
    void switchAfterTimeoutWithLoad() {
        HashMap<Direction, Integer> load = new HashMap<>();
        load.put(Direction.NORTH, 10);
        load.put(Direction.SOUTH, 10);
        load.put(Direction.EAST, 1);
        load.put(Direction.WEST, 0);

        TrafficDirection result = controller.setDirection(
                TrafficDirection.N_S, load, Controller.MAX_PHASE_TIME + 1
        );

        assertEquals(TrafficDirection.E_W, result);
    }

    @Test
    @DisplayName("phase does not switch after max phase time when other load is zero")
    void noSwitchAfterTimeoutZeroLoad() {
        HashMap<Direction, Integer> load = new HashMap<>();
        load.put(Direction.NORTH, 10);
        load.put(Direction.SOUTH, 10);
        load.put(Direction.EAST, 0);
        load.put(Direction.WEST, 0);

        TrafficDirection result = controller.setDirection(
                TrafficDirection.N_S, load, Controller.MAX_PHASE_TIME + 1
        );

        assertEquals(TrafficDirection.N_S, result);
    }
}