package quarrysim;

public class Mission {
    private double totalMaterial;  // in tons
    private double transportedMaterial = 0;  // how much has been transported so far

    public void setTotalMaterial(double totalMaterial) {
        this.totalMaterial = totalMaterial;
    }

    public double getTotalMaterial() {
        return totalMaterial;
    }

    public void updateTransportedMaterial(double tons) {
        this.transportedMaterial = getTransportedMaterial() + tons;
    }

    public double getTransportedMaterial() {
        return transportedMaterial;
    }


    @Override
    public synchronized String toString() {
        return String.format("Mission Status: %.2f/%.2f tons transported (%.2f%% complete)",
                transportedMaterial, totalMaterial);//getCompletionPercentage()
    }
}
