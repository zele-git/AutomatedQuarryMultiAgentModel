package quarrysim;

// Scenario.java
public class ScenarioFile {
    public RoadConfig roadparam;
    public TruckAgentConfig truck_agent;
    public LoadConfig loading_station;
    public UnloadConfig unloading_station;
    public MissionConfig mission;
    public AgentsCount agents;
    public MiscConfig miscparam;
}

class MiscConfig {
    public long clock_tick;
    public double density_penality_weight;
    public double slope_penality_factor;
}

class AgentsCount {
    public int num_trucks;
}
class RoadConfig {
    public double slope;
    public double distance;
    public int base_speed;
    public int max_truck_on_road;
}

class TruckAgentConfig {
    public double max_speed;
    public double base_speed;
    public double carrying_capacity;
}

class LoadConfig {
    public int bucket_size;
    public int max_waiting_queue;
}
class UnloadConfig {
    public int bucket_size;
    public int max_waiting_queue;
}
class MissionConfig {
    public double transport_goal_in_tons;
}
