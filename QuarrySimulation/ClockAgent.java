package quarrysim;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

public class ClockAgent extends Agent {
    MiscConfig misc = new MiscConfig();

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            misc = (MiscConfig) args[0];
        } else {
            System.out.println("Not sent");
        }
        System.out.println(getLocalName() + " started.");

        addBehaviour(new TickerBehaviour(this, misc.clock_tick) {  // Every 1 second
            @Override
            protected void onTick() {
                SimulationEnvironment.getInstance().incrementTime();
                //System.out.println("ClockAgent: Global Time updated to " + SimulationEnvironment.getInstance().getTime());
            }
        });
    }
}
