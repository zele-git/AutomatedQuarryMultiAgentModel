package quarrysim;

import jade.core.AID;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class SimMain {
    public static void main(String[] args) {
        try {
            // Initialize JADE runtime
            Runtime rt = Runtime.instance();
            Profile p = new ProfileImpl();
            ContainerController cc = rt.createMainContainer(p);
            ScenarioFile sf = ScenarioFileLoader.load(SimMain.class.getClassLoader().getResource("quarrysim/scenario1.json").getPath());
            SimulationEnvironment env = SimulationEnvironment.getInstance();
            env.initMission(sf.mission.transport_goal_in_tons);

            int trucknum = sf.agents.num_trucks;
            //agent creation
            Object[] clockargs = new Object[]{sf.miscparam};
            AgentController clock = cc.createNewAgent("ClockAgent", quarrysim.ClockAgent.class.getName(), clockargs);
            clock.start();

            Object[] loadargs = new Object[]{sf.loading_station, sf.truck_agent};
            AgentController ls1 = cc.createNewAgent("L1", quarrysim.LoadingStationAgent.class.getName(), loadargs);
            ls1.start();
            AgentController ls2 = cc.createNewAgent("L2", quarrysim.LoadingStationAgent.class.getName(), loadargs);
            ls2.start();


            Object[] unloadargs = new Object[]{sf.unloading_station};
            AgentController us1 = cc.createNewAgent("U1", quarrysim.UnloadingStationAgent.class.getName(), unloadargs);
            us1.start();
            AgentController us2 = cc.createNewAgent("U2", quarrysim.UnloadingStationAgent.class.getName(), unloadargs);
            us2.start();
            AgentController us3 = cc.createNewAgent("U3", quarrysim.UnloadingStationAgent.class.getName(), unloadargs);
            us3.start();
            //AID initialization
            AID lds1 = new AID("L1", jade.core.AID.ISLOCALNAME);
            AID lds2 = new AID("L2", jade.core.AID.ISLOCALNAME);
            AID ulds1 = new AID("U1", jade.core.AID.ISLOCALNAME);
            AID ulds2 = new AID("U2", jade.core.AID.ISLOCALNAME);
            AID ulds3 = new AID("U3", jade.core.AID.ISLOCALNAME);
            // delay for proper initiation of station data
            try {
                Thread.sleep(2000);  // Delay to allow Truck Agents to register with DFService
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Add roadlayout
            env.addRoad(new Road("lA1",lds1, ulds1, 490, 8));
            env.addRoad(new Road("uA1",ulds1,lds1, 520, 6.8));

            env.addRoad(new Road("lA2",lds1,ulds2, 486, 6.7));
            env.addRoad(new Road("uA2",ulds2,lds1, 489, 5));

            env.addRoad(new Road("lA3",lds1, ulds3, 870, 4.6));
            env.addRoad(new Road("uA3",ulds3,lds1, 753, 5.4));

            env.addRoad(new Road("lB1",lds2,ulds1, 786, 4.5));
            env.addRoad(new Road("uB1",ulds1,lds2, 733, 3.8));

            env.addRoad(new Road("lB2",lds2, ulds2, 635, 7));
            env.addRoad(new Road("uB2",ulds2,lds2, 740, 6));

            env.addRoad(new Road("lB3",lds2,ulds3, 800, 5));
            env.addRoad(new Road("uB3",ulds3,lds2, 1200, 4));

            Object[] truckArgs = new Object[]{sf.truck_agent,sf.miscparam};
            for (int i = 1; i <= trucknum; i++) {
                cc.createNewAgent("Truck"+i, quarrysim.TruckAgent.class.getName(), truckArgs).start();
            }

            cc.createNewAgent("Terminator", quarrysim.SimulationController.class.getName(), null).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
