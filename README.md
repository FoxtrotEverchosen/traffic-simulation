# Traffic Control System
This program implements an algorithm for intelligent traffic lights control system. The algorithm simulates the movement of vehicles on a four-way intersection based on provided input.

## Running the program
To run the program you will have to run the provided .jar file. To do so you will need to have installed JDK for Java version >= 21 and execute the following command:

```bash 
java -jar simulation.jar <input> <output> [lane_count]
```

Input and output arguments are required and take a json file. Input must include a list of valid commands. If output file does not exist, it will be created. If an existing file is passed as output, it will be overwritten.

The lane count parameter is optional, and will change how many lanes each road have (defaulting to 1). Lane count must be of type integer.

## Valid input commands
Input should consist of valid commands, each identified by its type. Current valid command types are:

- addVehicle - add vehicle to specified road
- addEmergencyVehicle - add emergency vehicle to specified road
- failRoad - specify a road to be excluded from working intersection
- fixRoad - specify a road to be fixed
- step - make a simulation step

Commands adding vehicle require additional fields, that is "vehicleId", "startRoad", "endRoad". Commands used for manipulating road status require "direction". Directions include: North, East, South, West (the algorithm is not case sensitive). Step command does not require additional fields. Example input:

```json
{
  "commands": [
    {
      "type": "addVehicle",
      "vehicleId": "vehicle1",
      "startRoad": "south",
      "endRoad": "north"
    },
    {
      "type": "addVehicle",
      "vehicleId": "vehicle2",
      "startRoad": "north",
      "endRoad": "south"
    },
    {
      "type": "step"
    },
    {
      "type": "step"
    },
    {
      "type": "addEmergencyVehicle",
      "vehicleId": "emergency",
      "startRoad": "west",
      "endRoad": "east"
    },
    {
      "type": "failRoad",
      "direction": "north"
    },
    {
      "type": "fixRoad",
      "direction": "north"
    },
    {
      "type": "step"
    }
  ]
}
```

The output will return a JSON with a list of vehicles that departed on each step call, which may look like this:
```json
{
  "stepStatuses" : [ 
  {
    "leftVehicles" : [ "vehicle1", "vehicle2" ]
  }, {
    "leftVehicles" : [ ]
  }, {
    "leftVehicles" : [ "emergency" ]
  } ]
}
```

## How it works

### Threshold Strategy
The main algorithm utilizes a threshold strategy to direct the traffic flow.This strategy uses three mechanisms, namely:
- Minimum phase time (`MIN_PHASE_TIME`) - this constant guarantees that the light stays green for at least N steps before switching is possible again. This aims to prevent a constant oscillation with similar traffic on both sides of the intersection.
- Maximum phase time (`MAX_PHASE_TIME`) - this constant guarantees that the light switches after M steps to prevent starvation when one direction has dominant traffic.
- Load threshold - if the waiting vehicle count for the inactive phase exceeds 1.5x the active phase count, the phase switches. The 1.5 ratio makes sure that small imbalances don't cause unnecessary switching.

### Emergency vehicles
Emergency vehicles are checked before any load calculation. If any direction has an emergency vehicle waiting, the phase is immediately forced to serve that direction, bypassing minimum phase time. The emergency vehicle is also placed at the front of its lane queue so it departs in the very next step. The emergency flag is cleared only after all emergency vehicles in that direction have departed, ensuring multiple emergency vehicles retain priority until the last one passes.

### Road failure
When a road is marked as failed, the load calculation function returns 0 for that direction, regardless of its actual size. This means a failed road does not contribute to load pressure and the controller won't select a phase because of a failed road's queue. However, if the other road in the same phase has waiting vehicles, the phase can still be selected normally and vehicles will depart from the healthy road. Failed roads are skipped during vehicle departure. Vehicles on failed roads remain queued and resume departing normally once the road is repaired via "fixRoad".

### Known limitations
- Yellow lights are not simulated - phases switch instantly. I decided, that there was no good way to implement them, as it would either work as another green/red light or be undeterministic, which in turn would make testing significantly more complex. The output would also be undeterministic.
- When emergency vehicles arrive simultaneously on conflicting phases (e.g. north and east at the same time), N_S direction is served first since it appears first in the TrafficDirection enum. The other emergency vehicle acquires the green light the following step.

## Design decisions
- ControllerStrategy interface - the switching algorithm is the most likely thing to change or extend. Extracting it behind an interface allows for easy swapping for alternative strategies (e.g. wait-time based, proportional) without touching Controller, SimulationLoop, or any other class. ThresholdStrategy is the default implementation.

- Emergency vehicles jump the queue - rather than just flagging a direction and waiting for the vehicle's natural turn, emergency vehicles are inserted at the front of their lane via addFirst. This ensures the emergency vehicle departs in the very next green step for that direction, regardless of how many regular vehicles are ahead of it. This could be seen as somewhat more realistic, since in cases of emergency normal vehicles would make way for the emergency one if possible.

- getLoad() function returns 0 for closed roads. The alternative would be to filter closed roads in the controller, but that would leak intersection state knowledge into the algorithm. Returning 0 from getLoad() keeps the controller unaware of road failures, preserving a single responsibility principle.

## Implemented extensions
Beyond the basic requirements this program adds the following:
- Multiple lanes — each road supports N lanes configured at startup. Vehicles join the shortest lane on arrival. Each green step releases one vehicle per lane, so throughput scales linearly with lane count. Lane count is passed as an optional third CLI argument, defaulting to 1.
- Ability to use failRoad and fixRoad commands that take a direction offline and restore it. 
- Emergency vehicle priority — addEmergencyVehicle places the vehicle at the front of its lane and flags the direction. The controller immediately forces the corresponding phase regardless of normal timing rules.

## Test suite
This implementation comes with 20 tests used to verify its correctness. Tests cover the three main classes responsible for most of the work. To run the tests use:
```bash
mvn test
```