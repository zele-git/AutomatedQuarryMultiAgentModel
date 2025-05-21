package quarrysim;

import jade.core.AID;

public class Road {
    private AID fromStation;
    private AID toStation;
    private double distance;
    private double slope;
    private String roadName;
    private int truckOnRoad = 0;

    public Road(String roadname, AID fromStation, AID toStation, double distance, double slope) {
        this.roadName = roadname;
        this.fromStation = fromStation;
        this.toStation = toStation;
        this.distance = distance;
        this.slope = slope;
    }
    public synchronized void incrementTruck() {
        truckOnRoad++;
    }

    public synchronized void decrementTruck() {
        if (truckOnRoad > 0) {
            truckOnRoad--;
        }
    }

    public synchronized int getCurrentTruckCount() {
        return truckOnRoad;
    }
    public String getRoadName() {
        return roadName;
    }

    public void setRoadName(String roadName) {
        this.roadName = roadName;
    }
    public AID getFromStation() {
        return fromStation;
    }

    public AID getToStation() {
        return toStation;
    }

    public double getDistance() {
        return distance;
    }

    public double getSlope() {
        return slope;
    }

    @Override
    public String toString() {
        return "Road{" +
                "fromStation=" + fromStation.getLocalName() +
                ", toStation=" + toStation.getLocalName() +
                ", distance=" + distance +
                ", slope=" + slope +
                '}';
    }
}
