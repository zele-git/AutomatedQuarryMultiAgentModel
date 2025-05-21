package quarrysim;

import jade.core.AID;

import java.util.*;
import java.util.stream.Collectors;

public class SimulationEnvironment {
    private static final SimulationEnvironment instance = new SimulationEnvironment();
    private static List<String> truckActionTimeSequence;
    private final List<StationData> stationInfoList = new ArrayList<>();
    private List<Road> roadlayout = new ArrayList<>();
    private final Map<String, Set<AID>> roadTruckMap = new HashMap<>();
    private int time = 0;
    private double missionInTons  = 0;
    private double transportedInTons = 0;

    private SimulationEnvironment() {
        truckActionTimeSequence = Collections.synchronizedList(new LinkedList<>());
    }

    public static SimulationEnvironment getInstance() {
        return instance;
    }
    public synchronized int getTime() {
        return time;
    }
    public synchronized void incrementTime() {
        time += 1;
    }
    //truck mission update
    public void initMission(double init) {
        missionInTons = init;
    }

    public synchronized void updateTonsTransported(double tons) {
        transportedInTons += tons;
    }
    public synchronized double getTotalTransported() {
        return transportedInTons;
    }
    public synchronized boolean isMissionComplete() {
        return transportedInTons >= missionInTons;
    }
    public synchronized double getRemainingTons(){
        return missionInTons - transportedInTons;
    }
    //truck action time
    public synchronized void addTruckActionTimeSequence(String timeSequence) {
        truckActionTimeSequence.add(timeSequence);
    }
    public synchronized List<String> getTruckActionTimeSequence() {
        return truckActionTimeSequence;
    }
   //road data
   public synchronized void truckEnterRoad(String roadName, AID truckAID) {
       roadTruckMap.computeIfAbsent(roadName, k -> new HashSet<>()).add(truckAID);
       System.out.println("Truck " + truckAID.getLocalName() + " ENTERED road " + roadName +
               " | Total trucks now: " + getTruckCountOnRoad(roadName));
   }
    public synchronized void truckExitRoad(String roadName, AID truckAID) {
        Set<AID> trucks = roadTruckMap.get(roadName);
        if (trucks != null) {
            trucks.remove(truckAID);
            System.out.println("Truck " + truckAID.getLocalName() + " EXITED road " + roadName +
                    " | Total trucks now: " + getTruckCountOnRoad(roadName));
            if (trucks.isEmpty()) {
                roadTruckMap.remove(roadName);
            }
        }
    }
    public synchronized int getTruckCountOnRoad(String roadName) {
        return roadTruckMap.getOrDefault(roadName, Collections.emptySet()).size();
    }

    public void addRoad(Road road) {
        roadlayout.add(road);
    }
    public List<Road> getRoadLayout() {
        return roadlayout;
    }
    public List<Road> getRoadsFrom(AID station) {
        return roadlayout.stream()
                .filter(r -> r.getFromStation().equals(station))
                .collect(Collectors.toList());
    }
    public List<Road> getRoadsTo(AID station) {
        return roadlayout.stream()
                .filter(r -> r.getToStation().equals(station))
                .collect(Collectors.toList());
    }
   //station data
    public List<StationData> getStationData() {
        return stationInfoList;
    }
    public void addStationData(StationData stationData) {
        stationInfoList.add(stationData);
    }
    public List<StationData> getStationsByType(String stationType) {
        List<StationData> result = new ArrayList<>();
        for (StationData data : getStationData()) {
            if (data.getStationType().equalsIgnoreCase(stationType)) {
                result.add(data);
            }
        }
        return result;
    }
    public String getStationType(AID station) {
        for (StationData info : stationInfoList) {
            if (info.getStationAID().equals(station)) {
                return info.getStationType();
            }
        }
        return null;
    }
}
