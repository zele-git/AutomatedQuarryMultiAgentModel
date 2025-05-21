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
import jade.core.behaviours.WakerBehaviour;

import java.util.LinkedList;
import java.util.List;

public class UnloadingStationAgent extends Agent {
    private final List<TruckQueueEntry> truckQueue = new LinkedList<>();
    private final int maxQueueSize = 5;
    private boolean unloadingInProgress = false;
    UnloadConfig unloadconfig = new UnloadConfig();
    SimulationEnvironment env;

    @Override
    protected void setup() {
        registerService();
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            unloadconfig = (UnloadConfig) args[0];
        } else {
            System.out.println("Not sent");
        }
        StationData sd = new StationData(this.getAID(), "Unloading", unloadconfig.max_waiting_queue);
        env = SimulationEnvironment.getInstance();
        env.addStationData(sd);
        System.out.println(getLocalName() + " ready.");
        addBehaviour(new HandleTruckRequestsBehaviour());
        addBehaviour(new UnloadingProcessBehaviour());
    }

    private class HandleTruckRequestsBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg = myAgent.receive(mt);

            if (msg != null) {
                if (myAgent.getLocalName().trim().equals(msg.getContent().trim())) {
                    AID truckAID = msg.getSender();
                    if (isTruckAlreadyInQueue(truckAID)) {
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.REFUSE);
                        reply.setContent(truckAID.getLocalName());
                        send(reply);
                    } else if (truckQueue.size() >= maxQueueSize) {
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.REFUSE);
                        reply.setContent(truckAID.getLocalName());
                        send(reply);
                    } else {
                        truckQueue.add(new TruckQueueEntry(truckAID, env.getTime()));
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

    private class UnloadingProcessBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            if (!unloadingInProgress && !truckQueue.isEmpty()) {
                TruckQueueEntry nextTruck = pollQueue();
                //System.out.println(getLocalName() + ": Starting unloading truck " + nextTruck.getTruckAID().getLocalName());
                unloadingInProgress = true;
                ACLMessage startUnloadingMsg = new ACLMessage(ACLMessage.INFORM);
                startUnloadingMsg.addReceiver(nextTruck.truckAID);
                startUnloadingMsg.setContent(nextTruck.truckAID.getLocalName());
                send(startUnloadingMsg);
                // Simulate unloading time with delay
                myAgent.addBehaviour(new UnloadingDelayBehaviour(nextTruck.getTruckAID(), 15000));//10 tick time
            } else {
                block(1000);
            }
        }

        private class UnloadingDelayBehaviour extends WakerBehaviour {
            private AID truckAID;

            public UnloadingDelayBehaviour(AID truckAID, long timeout) {
                super(UnloadingStationAgent.this, timeout);
                this.truckAID = truckAID;
            }

            @Override
            protected void onWake() {
                //pollQueue();
                System.out.println(getLocalName() + ": Finished unloading truck " + truckAID.getLocalName());
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(truckAID);
                msg.setContent(truckAID.getLocalName());
                send(msg);
                unloadingInProgress = false;
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

    private void registerService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Station");
        sd.setName("UnloadingStationService");
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
