package quarrysim;

import jade.core.AID;

public class StationData {
    private final AID stationAID;
    private final String stationType;
    private final int maxQueueSize;

    public StationData(AID stationAID, String stationType, int maxQueueSize) {
        this.stationAID = stationAID;
        this.stationType = stationType;
        this.maxQueueSize = maxQueueSize;
    }

    public AID getStationAID() {
        return stationAID;
    }

    public String getStationType() {
        return stationType;
    }

    public int getMaxQueueSize() {
        return maxQueueSize;
    }

    @Override
    public String toString() {
        return "StationInfo{" +
                "stationAID=" + stationAID.getLocalName() +
                ", stationType='" + stationType + '\'' +
                ", maxQueueSize=" + maxQueueSize +
                '}';
    }
}
