package quarrysim;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class SimulationController extends Agent {
    private static int liveAgents = 0;
    SimulationEnvironment env;


    @Override
    protected void setup() {
        System.out.println(getLocalName() + " started.");
        // Add a periodic behavior to check for live agents

        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Quarry");  // Same type as registered
        template.addServices(sd);
        env = SimulationEnvironment.getInstance();

        addBehaviour(new TickerBehaviour(this, 2000) { // Check every 2 seconds
            protected void onTick() {
                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    // Search for all live agents
                    int liveAgents = result.length;
                    //System.out.println("Live user agents: " + liveAgents);
                    if (liveAgents == 0) {
                        System.out.println("No more live agents. Shutting down platform...");
                        CSVexporter.writeActionTimeSeqToCsv(SimulationEnvironment.getInstance().getTruckActionTimeSequence(), "/Users/zby01/Documents/SimOutput/trucks01.csv");
                        doDelete();  // Clean up this agent
                        jade.core.Runtime.instance().shutDown();  // Shutdown JADE platform
                    }

                } catch (Exception e) {
                    System.err.println("Error while checking live agents: " + e);
                }
            }
        });
    }

    @Override
    protected void takeDown() {
        System.out.println(getLocalName() + " terminating.");
    }
}