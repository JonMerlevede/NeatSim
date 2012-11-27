package neatsim.experiments.sim;

import java.io.IOException;
import java.util.Collection;

import rinde.sim.core.Simulator;
import rinde.sim.core.TimeLapse;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.pdp.Parcel;
import rinde.sim.problem.common.AddVehicleEvent;
import rinde.sim.problem.common.DefaultVehicle;
import rinde.sim.problem.common.DynamicPDPTWProblem;
import rinde.sim.problem.common.DynamicPDPTWProblem.SimulationInfo;
import rinde.sim.problem.common.DynamicPDPTWProblem.StopCondition;
import rinde.sim.problem.gendreau06.Gendreau06ObjectiveFunction;
import rinde.sim.problem.gendreau06.Gendreau06Parser;
import rinde.sim.problem.gendreau06.Gendreau06Scenario;

public class GendreauProblem {
	public static final String SCENARIO_NAME = "data/req_rapide_1_240_24";
	public static final int NUMBER_OF_VEHICLES = 5;	
	private final DynamicPDPTWProblem problem;
	
	public GendreauProblem() throws IOException {
		problem = createProblem();
	}
	
	private final DynamicPDPTWProblem createProblem() throws IOException {
		Gendreau06Scenario scenario = Gendreau06Parser.parse(SCENARIO_NAME, NUMBER_OF_VEHICLES);
		System.out.println(scenario.getPossibleEventTypes().toString());
		
		long randomSeed = 823745;
		final DynamicPDPTWProblem problem = new DynamicPDPTWProblem(scenario, randomSeed);
		problem.enableUI();
		problem.addCreator(AddVehicleEvent.class, new MyVehicleEventCreator());
		problem.addStopCondition(new StopCondition() {
			@Override
			public boolean isSatisfiedBy(SimulationInfo context) {
				return (context.stats.totalDeliveries > 0) && (context.stats.totalParcels == context.stats.totalDeliveries);
			}
		});
		return problem;
	}
	
	public void start() {
		System.out.println("Starting simulation");
		problem.simulate();
		System.out.println("Statistics: " + problem.getStatistics());
		Gendreau06ObjectiveFunction obj = new Gendreau06ObjectiveFunction();
		System.out.println("Total cost: " + obj.computeCost(problem.getStatistics()));
	}
	
	private class MyVehicleEventCreator implements DynamicPDPTWProblem.Creator<AddVehicleEvent> {
		@Override
		public boolean create(Simulator sim, AddVehicleEvent event) {
//			return sim.register(new VeryStupidVehicle(event));	// TC ~ 10^8
			return sim.register(new StupidVehicle(event));		// TC ~ 10^7 
		}
	}
}