package neatsim.experiments.sim;

import java.io.IOException;

import neatsim.experiments.sim.vehicles.AverageVehicle;
import rinde.sim.core.Simulator;
import rinde.sim.problem.common.AddVehicleEvent;
import rinde.sim.problem.common.DynamicPDPTWProblem;
import rinde.sim.problem.common.DynamicPDPTWProblem.SimulationInfo;
import rinde.sim.problem.common.DynamicPDPTWProblem.StopCondition;
import rinde.sim.problem.gendreau06.Gendreau06ObjectiveFunction;
import rinde.sim.problem.gendreau06.Gendreau06Scenario;

public class GendreauProblem {
	private final DynamicPDPTWProblem problem;
	private final Gendreau06ObjectiveFunction objFunction;
	
	public GendreauProblem(Gendreau06Scenario scenario) throws IOException {
		problem = createProblem(scenario);
		objFunction = new Gendreau06ObjectiveFunction();
	}
	
	
	private final DynamicPDPTWProblem createProblem(Gendreau06Scenario scenario) throws IOException {
		
		long randomSeed = 823745;
		final CoordinationModel coordinationModel = new CoordinationModel();
		final DynamicPDPTWProblem problem = new DynamicPDPTWProblem(scenario, randomSeed, coordinationModel);
		problem.enableUI();
		problem.addCreator(AddVehicleEvent.class, new MyVehicleEventCreator());
		problem.addStopCondition(new StopCondition() {
			@Override
			public boolean isSatisfiedBy(SimulationInfo context) {
//				return (context.stats.totalDeliveries > 0)
//						&& (context.stats.totalParcels == context.stats.totalDeliveries
//						&& context.stats.simFinish);
				return objFunction.isValidResult(context.stats);
			}
		});
		return problem;
	}
	
	public void start() {
		System.out.println("Starting simulation");
		problem.simulate();
		System.out.println("Statistics: " + problem.getStatistics());
		System.out.println("Total cost: " + objFunction.computeCost(problem.getStatistics()));
	}
	
	private class MyVehicleEventCreator implements DynamicPDPTWProblem.Creator<AddVehicleEvent> {
		@Override
		public boolean create(Simulator sim, AddVehicleEvent event) {
//			return sim.register(new VeryStupidVehicle(event));	// simulationTime=18880000
//			return sim.register(new StupidVehicle(event));		// simulationTime=14654000
			return sim.register(new AverageVehicle(event));		// simulationTime=14466000
		}
	}
}
