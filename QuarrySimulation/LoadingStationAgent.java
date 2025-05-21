package quarrysim;

import jade.core.Agent;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.core.behaviours.CyclicBehaviour;

import java.util.*;

public class LoadingStationAgent extends Agent {
    private final List<TruckQueueEntry> truckQueue = new LinkedList<>();
    LoadConfig loadconfig = new LoadConfig();
    SimulationEnvironment env;
    TruckAgentConfig truckconfig = new TruckAgentConfig();


    @Override
    protected void setup() {
        registerService();
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            loadconfig = (LoadConfig) args[0];
            truckconfig = (TruckAgentConfig) args[1];
        } else {
            System.out.println("Not sent");
        }
        StationData sd = new StationData(this.getAID(), "Loading", loadconfig.max_waiting_queue);
        env = SimulationEnvironment.getInstance();
        env.addStationData(sd);
        System.out.println(getLocalName() + " ready.");
        addBehaviour(new HandleTruckRequestsBehaviour());
        addBehaviour(new LoadingProcessBehaviour());
    }

    // Behaviour to handle truck requests to join the queue
    private class HandleTruckRequestsBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg = myAgent.receive(mt);

            if (msg != null) {
                if (myAgent.getLocalName().trim().equals(msg.getContent().trim())) {
                    AID truckAID = msg.getSender();
                    if (isTruckAlreadyInQueue(truckAID)) {
                        //System.out.println(getLocalName() + ": Truck " + truckAID.getLocalName() + " already in queue.");
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.REFUSE);
                        reply.setContent(truckAID.getLocalName());
                        send(reply);
                    } else if (truckQueue.size() >= loadconfig.max_waiting_queue) {
                        //System.out.println(getLocalName() + ": Queue full. Cannot accept truck " + truckAID.getLocalName());
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.REFUSE);
                        reply.setContent(truckAID.getLocalName());
                        send(reply);
                    } else if (env.getRemainingTons() == 0) {
                        //System.out.println(getLocalName() + ": Queue full. Cannot accept truck " + truckAID.getLocalName());
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.REFUSE);
                        reply.setContent(truckAID.getLocalName());
                        send(reply);
                    } else {
                        addToQueue(new TruckQueueEntry(msg.getSender(), System.currentTimeMillis()));
                        //System.out.println(getLocalName() + ": Truck " + truckAID.getLocalName() + " added to queue.");
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.AGREE);
                        reply.setContent(truckAID.getLocalName());
                        send(reply);
                    }
                }
            } else {
                block();
            }
        }
    }

    // Behaviour to simulate loading process (FCFS)
    private class LoadingProcessBehaviour extends CyclicBehaviour {
        private boolean loadingInProgress = false;

        @Override
        public void action() {
            if (!loadingInProgress && !truckQueue.isEmpty() && !env.isMissionComplete()) {
                TruckQueueEntry nextTruck = pollQueue(); // Peek to show the current truck
                loadingInProgress = true;
                ACLMessage startLoadingMsg = new ACLMessage(ACLMessage.INFORM);
                startLoadingMsg.addReceiver(nextTruck.truckAID);
                startLoadingMsg.setContent(nextTruck.truckAID.getLocalName());
                send(startLoadingMsg);
                if (env.getRemainingTons() >= truckconfig.carrying_capacity) {
                    //carriedTons += truckconfig.carrying_capacity;
                    env.updateTonsTransported(truckconfig.carrying_capacity);
                    //mission.reportTransport(truckconfig.carrying_capacity); // carrying capacity of truck
                    System.out.println("Transported so far :" + env.getTotalTransported());
                    System.out.println("Remaining :" + env.getRemainingTons());
                } else {
                    //carriedTons += env.getRemainingTons();
                    env.updateTonsTransported(env.getRemainingTons()); // load available tons
                    System.out.println("Transported so far :" + env.getTotalTransported());
                    System.out.println("Remaining :" + env.getRemainingTons());
                }
                // Simulate loading time with delay
                myAgent.addBehaviour(new LoadingDelayBehaviour(nextTruck.getTruckAID(), 25000)); // 15 tick time loading time
            } else {
                block(1000); // Check every 1 second
            }
        }

        private class LoadingDelayBehaviour extends jade.core.behaviours.WakerBehaviour {
            private AID truckAID;

            public LoadingDelayBehaviour(AID truckAID, long timeout) {
                super(LoadingStationAgent.this, timeout);
                this.truckAID = truckAID;
            }

            @Override
            protected void onWake() {
                // Remove truck from queue
                // pollQueue();
                System.out.println(getLocalName() + ": Finished loading truck " + truckAID.getLocalName());
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(truckAID);
                msg.setContent(truckAID.getLocalName());
                send(msg);
                loadingInProgress = false;
            }
        }
    }

    private boolean isTruckAlreadyInQueue(AID truckAID) {
        for (TruckQueueEntry entry : truckQueue) {
            if (entry.getTruckAID().equals(truckAID)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void addToQueue(TruckQueueEntry entry) {
        truckQueue.add(entry);
    }

    // Peek at head without removing
    public synchronized TruckQueueEntry peekQueue() {
        return truckQueue.isEmpty() ? null : truckQueue.get(0);
    }

    // Remove and return head
    public synchronized TruckQueueEntry pollQueue() {
        return truckQueue.isEmpty() ? null : truckQueue.remove(0);
    }

    public class TruckQueueEntry {
        private final AID truckAID;
        private final long arrivalTime;

        public TruckQueueEntry(AID truckAID, long arrivalTime) {
            this.truckAID = truckAID;
            this.arrivalTime = arrivalTime;
        }

        public AID getTruckAID() {
            return truckAID;
        }

        public long getArrivalTime() {
            return arrivalTime;
        }
    }

    private void registerService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Station");
        sd.setName("LoadingStationService");
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
