package neatsim.sim;

import neatsim.core.BlackBox;
import neatsim.core.BlackBoxHeuristic;
import rinde.evo4mas.gendreau06.CoordinationModel;
import rinde.evo4mas.gendreau06.MyopicTruck;
import rinde.sim.core.Simulator;
import rinde.sim.problem.common.AddVehicleEvent;
import rinde.sim.problem.common.DynamicPDPTWProblem;
import rinde.sim.problem.common.StatsTracker.StatisticsDTO;
import rinde.sim.problem.gendreau06.Gendreau06ObjectiveFunction;
import rinde.sim.problem.gendreau06.Gendreau06Scenario;

/**
 * A Gendreau problem is a specific type of dynamic PDP problem with time
 * windows.
 * 
 * TODO Complete the documentation of this class.
 * 
 * @see Gendreau06Scenario <pre>Gendreau06Schenario</pre>
 * 	- scenario of a Gendreau problem.
 * @see DynamicPDPTWProblem <pre>DynamicPDPTWProblem</pre>
 * 	- dynamic PDP problem with time windows.
 * @author Jonathan Merlevede
 * 
 */
public class GendreauHeuristicProblem extends DynamicPDPTWProblem {
	/**
	 * Handle to Gendreau objective function.
d	 */
	private final Gendreau06ObjectiveFunction objFunction;
	/**
	 * Random seed (used by RinSim).
	 */
	private static final long RANDOM_SEED = 823745;
	/**
	 * Default value for whether or not to enable the UI.
	 */
	private static final boolean ENABLE_UI = false;
	
	public static GendreauHeuristicProblem create(Gendreau06Scenario scenario, BlackBox heuristic, boolean enableUI) {
		assert scenario != null;
		assert heuristic != null;
		
		return new GendreauHeuristicProblem(scenario, heuristic, enableUI);
	}
	
	public static GendreauHeuristicProblem create(Gendreau06Scenario scenario, BlackBox heuristic) {
		assert scenario != null;
		assert heuristic != null;
		
		return new GendreauHeuristicProblem(scenario, heuristic, ENABLE_UI);
	}
	/*
	 * The constructor is private to circumvent the requirement of the constructor
	 * having to be the first call.
	 */
	private GendreauHeuristicProblem(Gendreau06Scenario scenario, BlackBox heuristic, boolean enableUI) {
		// Create a new PDP problem with time windows using the specified scenario.
		// The Gendreau scenario is used here as a generic DynamicPDPTWScenario.
		super(scenario, RANDOM_SEED, new CoordinationModel());
		// Initialise instance variables.
		objFunction = new Gendreau06ObjectiveFunction();
		// Enable the UI if UI should be enabled.
		if (enableUI) {enableUI();}
		// Add a vehicle event creator to this DPDPTW problem.
		addCreator(AddVehicleEvent.class, new MyVehicleEventCreator(heuristic));
		// Add the Gendreau stop condition to this DPDPTW problem.
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
//		protected BlackBox heuristic;
		protected BlackBoxHeuristic heuristic;
		
		public MyVehicleEventCreator(BlackBox box) {
			this.heuristic = new BlackBoxHeuristic(box);
		}
		
		@Override
		public boolean create(Simulator sim, AddVehicleEvent event) {
			/*
			 * We no longer use the heuristic vehicle that I made.
			 * This vehicle can still be found inside of the "old" folder of this
			 * project, mostly because deleting it would make me feel bad :-).
			 * 
			 * @author Jonathan Merlevede
			 */
			// return sim.register(new HeuristicVehicle(event,heuristic));
			// TODO
			return sim.register(new MyopicTruck(event.vehicleDTO, heuristic));
		}
	}
}
