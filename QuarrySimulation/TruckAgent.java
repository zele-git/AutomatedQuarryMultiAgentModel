package quarrysim;

import jade.core.Agent;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.core.behaviours.Behaviour;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class TruckAgent extends Agent {

    private boolean travelToLoad = false;
    private boolean travelToUnload = false;
    private boolean rqstLoad = false;
    private boolean rqstUnoad = false;
    private boolean searchLoadStation = false;
    private long selectedRoadTravelTimeMs = 0; // Travel time in milliseconds
    private int cycle = 1;
    //private double carriedTons = 0;
    private double totalDeliveredTons = 0;
    private String timeReport;
    TruckAgentConfig truckconfig = new TruckAgentConfig();
    MiscConfig misc = new MiscConfig();
    private AID currentStation = null;
    SimulationEnvironment env;
    Road selectedRoad = null;

    @Override
    protected void setup() {
        registerService();
        System.out.println(getLocalName() + " started.");
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            truckconfig = (TruckAgentConfig) args[0];
            misc = (MiscConfig) args[1];
        } else {
            System.out.println("Not sent");
        }
        env = SimulationEnvironment.getInstance();

        List<StationData> allStations = env.getStationsByType("Unloading");
        if (!allStations.isEmpty()) {
            Random random = new Random();
            StationData selectedStation = allStations.get(random.nextInt(allStations.size()));
            currentStation = selectedStation.getStationAID();
            System.out.println(getLocalName() + ":Selected Unloading Station AID: " + currentStation.getLocalName());
        } else {
            System.out.println("No stations of type unloading found.");
        }
        addBehaviour(new TruckMainBehaviour());
    }

    private class TruckMainBehaviour extends Behaviour {
        private int step = 0;

        @Override
        public void action() {
            switch (step) {
                case 0:
                    if (env.getRemainingTons() == 0) {
                        System.out.println(getLocalName() + ": Mission complete. Terminating.");
                        doDelete();
                    } else
                        step = 1;
                    break;
                case 1:
                    //select next station
                    selectStationAndTravel(currentStation, "Loading");
                    step = 2;
                    break;
                case 2:
                    if (travelToLoad) {
                        myAgent.addBehaviour(new TravelDelayBehaviour(myAgent, selectedRoadTravelTimeMs, "Loading"));
                    }else step = 2;

                case 3:
                    // Send request to load
                    ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
                    try {
                        req.addReceiver(currentStation);
                        req.setContent(currentStation.getLocalName());
                        send(req);
                        if (!rqstLoad) {
                            rqstLoad = true;
                            timeReport = getLocalName() + ", " + env.getTime();
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                    System.out.println(getLocalName() + ": Sent load request.");
                    step = 4;
                    break;
                case 4:
                    // Wait for AGREE or REFUSE
                    MessageTemplate mt = MessageTemplate.or(
                            MessageTemplate.MatchPerformative(ACLMessage.AGREE),
                            MessageTemplate.MatchPerformative(ACLMessage.REFUSE));
                    ACLMessage loadreply = receive(mt);
                    if (loadreply != null) {
//                        if (loadreply.getPerformative() == ACLMessage.REFUSE && loadreply.getContent().equals(getLocalName()) && env.getStationType(loadreply.getSender()).equalsIgnoreCase("Loading")) {
//                            step = 1;
//                            break;
//                        }
                        if (loadreply.getPerformative() == ACLMessage.AGREE && loadreply.getContent().equals(getLocalName()) && env.getStationType(loadreply.getSender()).equalsIgnoreCase("Loading")) {
                            //System.out.println(getLocalName() + ": Accepted into loading queue.");
                            //myAgent.addBehaviour(new SimulateLoadOperation(myAgent, 25000));
                            timeReport += ", " + currentStation.getLocalName() + ", " + env.getTime();
                            step = 5;
                            break;
                        } else {
                            block(1000);    // short poll interval
                        }
                    } else {
                        block(1000);    // short poll interval
                    }

                case 5:
                    // Wait for "Loading complete time"
                    MessageTemplate loadingmt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                    ACLMessage loadingmsg = receive(loadingmt);
                    if (loadingmsg != null && loadingmsg.getContent().equals(getLocalName()) && env.getStationType(loadingmsg.getSender()).equalsIgnoreCase("Loading")) {
                        timeReport += ", " + env.getTime();
                        step = 6;
                    } else {
                        block(1000);    // short poll interval
                    }

                case 6:
                    selectStationAndTravel(currentStation, "Unloading");
                    step = 7;
                    break;

                case 7:
                    if (travelToLoad) {
                        myAgent.addBehaviour(new TravelDelayBehaviour(myAgent, selectedRoadTravelTimeMs, "Unloading"));
                    }else step = 7;

                case 8:
                    ACLMessage unloadReq = new ACLMessage(ACLMessage.REQUEST);
                    unloadReq.addReceiver(currentStation);
                    unloadReq.setContent(currentStation.getLocalName());
                    send(unloadReq);
                    if (!rqstUnoad) {
                        rqstUnoad = true;
                        timeReport += ", " + env.getTime() + ", " + currentStation.getLocalName();
                    }
                    System.out.println(getLocalName() + ": Sent unload request.");
                    step = 9;
                    break;

                case 9:
                    // Wait for AGREE or REFUSE
                    MessageTemplate replytm = MessageTemplate.or(
                            MessageTemplate.MatchPerformative(ACLMessage.AGREE),
                            MessageTemplate.MatchPerformative(ACLMessage.REFUSE));
                    ACLMessage unloadReply = receive(replytm);
                    if (unloadReply != null) {
                        if (unloadReply.getPerformative() == ACLMessage.AGREE && unloadReply.getContent().equals(getLocalName()) && env.getStationType(unloadReply.getSender()).equalsIgnoreCase("Unloading")) {
                            //System.out.println(getLocalName() + ": Accepted into unloading queue.");
                            timeReport += ", " + env.getTime();
                            step = 10;
                            break;
                        } else {
                            block(100);    // short poll interval
                        }
                    } else {
                        block(100);    // short poll interval
                    }

                case 10:
                    // Wait for Unloading complete inform
                    MessageTemplate unloadingmt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                    ACLMessage unloadingmsg = receive(unloadingmt);
                    if (unloadingmsg != null && unloadingmsg.getContent().equalsIgnoreCase(getLocalName()) && env.getStationType(unloadingmsg.getSender()).equalsIgnoreCase("Unloading")) {
                        //totalDeliveredTons += carriedTons;
                        timeReport += ", " + env.getTime();
                        //System.out.println(getLocalName() + ": Unloaded. Total delivered: " + totalDeliveredTons + " tons.");
                        rqstUnoad = false;
                        rqstLoad = false;
                        //carriedTons = 0;
                        System.out.println(getLocalName() + " round trip :" + cycle);
                        env.addTruckActionTimeSequence(timeReport);
                        cycle++;
                        // nowAtThisStation = operationCompletedAt;
                        searchLoadStation = false;
                        selectedRoad = null;
                        //reset
                        step = 0; // repeat
                        break;

                    } else {
                        block(100);    // short poll interval
                    }
            }
        }

        @Override
        public boolean done() {
            return false; // Run forever or until mission is complete
        }

        private synchronized void selectStationAndTravel(AID nowlocation, String targetStationType) {
            StationData bestStation = null;
            Road bestRoad = null;
            double bestCost = Double.MAX_VALUE;
            List<StationData> candidates = env.getStationsByType(targetStationType);

            try {
                for (StationData candidate : candidates) {
                    List<Road> possibleRoads = env.getRoadLayout().stream()
                            .filter(r -> r.getFromStation().equals(nowlocation)
                                    && r.getToStation().equals(candidate.getStationAID())).collect(Collectors.toList());
                    if (!possibleRoads.isEmpty()) {
                        for (Road road : possibleRoads) {
                            double adjustedDistance = road.getDistance() + (road.getSlope() * misc.slope_penality_factor);
                            double densityPenalty = env.getTruckCountOnRoad(road.getRoadName()) / road.getDistance(); // Add traffic density penalty
                            adjustedDistance += densityPenalty * misc.density_penality_weight; // Tune this weight
                            if (adjustedDistance <= bestCost) {
                                bestCost = adjustedDistance;
                                bestStation = candidate;
                                bestRoad = road;
                            }
                        }
                    } else {
                        System.out.println(getLocalName() + " No candidate road from: " + nowlocation.getLocalName() + " to " + candidate.getStationAID().getLocalName());
                    }
                }

                if (bestRoad != null) {
                    // Only now increment on the best selected road
                    currentStation = bestStation.getStationAID();
                    selectedRoad = bestRoad;
                    env.truckEnterRoad(selectedRoad.getRoadName(), getAID());
                    System.out.println(getLocalName() + ": Entered Road " + selectedRoad.getFromStation().getLocalName() + " -> " + selectedRoad.getToStation().getLocalName());
                    selectedRoadTravelTimeMs = (long) ((bestCost / convertSpeedToMetersPerTick(truckconfig.base_speed)) * 100);
                    System.out.println(getLocalName() + ": Travel Time " + selectedRoadTravelTimeMs + "ms on road :" + selectedRoad.getRoadName());
                    if (targetStationType.equals("Loading".trim())) {
                        travelToLoad = true;
                    }
                    if (targetStationType.equals("Unloading".trim())) {
                        travelToUnload = true;
                    }
                } else {
                    System.out.println(getLocalName() + ": No available road to " + targetStationType);
                }

            } catch (NullPointerException e) {
                e.printStackTrace();
            }

        }

        private class TravelDelayBehaviour extends jade.core.behaviours.WakerBehaviour {
            String stype;

            public TravelDelayBehaviour(Agent a, long sleepduaration, String stationType) {
                super(a, sleepduaration);
                stype = stationType;
            }

            @Override
            protected void onWake() {
                // Remove truck from queue
                env.truckExitRoad(selectedRoad.getRoadName(), getAID());
                //System.out.println(getLocalName() + " Arrived next station : ---> " + env.getTime());
                if (stype.equals("Loading".trim())) {
                    travelToLoad = false;
                    step = 3;
                }
                if (stype.equals("Unloading".trim())) {
                    travelToUnload = false;
                    step = 8;
                }
            }
        }

    }

    public double convertSpeedToMetersPerTick(double speedKmh) {
        return (speedKmh * 1000) / 3600;  // km/h → m/s → m/tick (since 1 tick = 1 sec)
    }

    private void registerService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Quarry");
        sd.setName("Truck");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
//            System.out.println(getLocalName() + " registered in DFService.");
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    protected void takeDown() {
        System.out.println(getLocalName() + " finished mission.");
        //System.out.println(getLocalName() + " action time sequence \n" + );
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

    }
}
