package neatsim.experiments.sim;

import java.io.IOException;

import neatsim.core.BlackBox;
import rinde.sim.core.Simulator;
import rinde.sim.problem.common.AddVehicleEvent;
import rinde.sim.problem.common.DynamicPDPTWProblem;
import rinde.sim.problem.common.StatsTracker.StatisticsDTO;
import rinde.sim.problem.gendreau06.Gendreau06ObjectiveFunction;
import rinde.sim.problem.gendreau06.Gendreau06Scenario;

public class GendreauHeuristicProblem extends DynamicPDPTWProblem {
	private final Gendreau06ObjectiveFunction objFunction;
	private static final long RANDOM_SEED = 823745;
	
	public GendreauHeuristicProblem(Gendreau06Scenario scenario, BlackBox heuristic) throws IOException {
		super(scenario, RANDOM_SEED, new CoordinationModel());
		objFunction = new Gendreau06ObjectiveFunction();
		//super.defaultUICreator.addRenderer(new DestinationWindowRenderer());
//		enableUI();
		addCreator(AddVehicleEvent.class, new MyVehicleEventCreator(heuristic));
		addStopCondition(new StopCondition() {
			@Override
			public boolean isSatisfiedBy(SimulationInfo context) {
				return objFunction.isValidResult(context.stats);
			}
		});
	}
	
	@Override
	public StatisticsDTO simulate() {
		StatisticsDTO stats = super.simulate();
		return stats;
	}
	
	private class MyVehicleEventCreator implements DynamicPDPTWProblem.Creator<AddVehicleEvent> {
		protected BlackBox heuristic;
		public MyVehicleEventCreator(BlackBox heuristic) {
			this.heuristic = heuristic;
		}
		@Override
		public boolean create(Simulator sim, AddVehicleEvent event) {
			/*
			 * We no longer use the heuristic vehicle that I made.
			 * This vehicle can still be found inside of the "old" folder of this
			 * project.
			 * 
			 * @author Jonathan Merlevede
			 */
			// return sim.register(new HeuristicVehicle(event,heuristic));
			// TODO
			return false;
		}
	}
}
