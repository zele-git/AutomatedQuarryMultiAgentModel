# Automated Quarry MultiAgent Model
The implementation simulates cyclic material transport using a multi-agent system, capturing interactions between dump trucks and loading/unloading stations. The loading station, unloading station and dump truck are modeled as autonomous agent with their own decision making strategy to perform tasks. The agents collaboration aims at completing a mission of transporing a certain tons of material.

**Goals**
- Simulate loader (and unloader), and truk activities in a quarry
- Generate configuration desing (trucks selection of loading, unloading station and roads)
- Analyzing decision making behavior of quarry agents, impacts of road static properties such as slope and dynamic properties such as traffic on roads, collaborative mission accomplishment and performances
- Design space exploration on configuration designs for maximizing mission accomplishment

**Quarry Layout**

![layout](https://github.com/user-attachments/assets/d9ffe0f4-46d1-4697-84de-4f51b999137c)

A road is characterized by two key physical attributes. Distance (length) – this is the horizontal or actual path length of the road segment, given in meters. Slope (Incline) – this indicates the steepness or angle of the road with respect to a flat horizontal surface, typically measured in degrees (°). 

**Implementation Structure**

JADE is used to model and simulate the behavior of quarry agents within a distributed multi-agent system. Each agent is designed as an independent software entity with decision-making capabilities, aligned with the goals of the quarry system. JADE provided the communication infrastructure and lifecycle management necessary for agents to interact using FIPA-compliant messaging protocols.

Specifically, JADE was instrumental in:

- Agent Creation and Lifecycle Management: facilitating the instantiation and coordination of agents with specific roles (e.g., Truck, Loader, or Unloade).

- Inter-agent Communication: enabling structured communication through ACL messages to simulate communication.

- Distributed Execution: enables a more realistic simulation of System-of-Systems (SoS) scenarios.

Quarry scenarios are defined using JSON. It enables structured representation of scenario parameters, including entities, environment settings, behaviors, and temporal sequences. It is well-suited for specifying complex interactions and system behaviors in a modular and scalable way. This structured approach facilitates integration with simulation tools and simplifies scenario validation and modification.

The implemenation contains 
- Agent models
- Scenario specification
- Simulation environment variables

**Agents and Simulation Environment Specification**
- Truck Agent - Represents a hauling vehicle. It chooses routes based on road characteristics, travels between stations (simulated with delays), sends load/unload requests, waits for confirmations, and reports transported tons toward the mission goal.
- Loading Station Agent - Manages a FIFO queue of trucks waiting to be loaded. On each tick it checks the queue, simulates a loading delay for the head truck, removes it when loading is complete, and notifies the truck it may depart.
- Unloading Station Agent - Queues arriving trucks, applies an unloading delay per truck, removes them once done, notifies each truck, and updates the shared mission status.
- Simulation Environment - An object that holds global data—time, station lists, road layouts, and which trucks occupy which roads. It provides thread-safe methods for agents to query or update shared state (e.g., clock ticks, road‐traffic counts).
- Scenario - A declarative data model loaded from JSON specifying all initial parameters: station counts and capacities, truck configurations, road definitions (distance, slope, delay factors), and the mission’s daily material-transport target. It drives a simulation run by feeding these values into the environment and agents.

**Cyclic Transport Model in Quarry**

<img width="1077" alt="image" src="https://github.com/user-attachments/assets/e230ec9b-4cd8-4296-916d-be529070f9bf" />


**Prieliminary result**


