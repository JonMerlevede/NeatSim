package neatsim.experiments.sim;

import java.io.IOException;

import neatsim.experiments.sim.vehicles.HeuristicVehicle;
import neatsim.experiments.sim.vehicles.junk.AverageVehicle;
import rinde.sim.core.Simulator;
import rinde.sim.problem.common.AddVehicleEvent;
import rinde.sim.problem.common.DynamicPDPTWProblem;
import rinde.sim.problem.common.StatsTracker.StatisticsDTO;
import rinde.sim.problem.gendreau06.Gendreau06ObjectiveFunction;
import rinde.sim.problem.gendreau06.Gendreau06Scenario;

public class GendreauProblem extends DynamicPDPTWProblem {
	private final Gendreau06ObjectiveFunction objFunction;
	private static final long RANDOM_SEED = 823745;
	//private final Color PURPLE = 
	//UICreator defaultUICreator = super.defaultUICreator;
	
	public GendreauProblem(Gendreau06Scenario scenario) throws IOException {
		super(scenario, RANDOM_SEED, new CoordinationModel());
		objFunction = new Gendreau06ObjectiveFunction();
		
		//super.defaultUICreator.addRenderer(new DestinationWindowRenderer());
		enableUI();
		addCreator(AddVehicleEvent.class, new MyVehicleEventCreator());
		addStopCondition(new StopCondition() {
			@Override
			public boolean isSatisfiedBy(SimulationInfo context) {
				return objFunction.isValidResult(context.stats);
			}
		});
	}
	
	@Override
	public StatisticsDTO simulate() {
		System.out.println("Starting simulation");
		StatisticsDTO stats = super.simulate();
		System.out.println("Statistics: " + stats);
		System.out.println("Total cost: " + objFunction.computeCost(stats));
		return stats;
	}
	
	private class MyVehicleEventCreator implements DynamicPDPTWProblem.Creator<AddVehicleEvent> {
		@Override
		public boolean create(Simulator sim, AddVehicleEvent event) {
//			return sim.register(new VeryStupidVehicle(event));	// simulationTime=18880000
//			return sim.register(new StupidVehicle(event));		// simulationTime=14654000
//			AverageVehicle v = new AverageVehicle(event);
			return sim.register(new AverageVehicle(event));
//			vehicles.add(v);
//			return sim.register(v);		// simulationTime=14466000
		}
	}
}
