package neatsim.core.evaluators.gendreau.simulators;

import java.io.BufferedReader;
import java.io.StringReader;

import rinde.ecj.Heuristic;
import rinde.evo4mas.common.ResultDTO;
import rinde.evo4mas.gendreau06.CoordinationModel;
import rinde.evo4mas.gendreau06.GendreauContext;
import rinde.evo4mas.gendreau06.MyopicTruck;
import rinde.sim.core.Simulator;
import rinde.sim.problem.common.AddVehicleEvent;
import rinde.sim.problem.common.DynamicPDPTWProblem;
import rinde.sim.problem.common.DynamicPDPTWProblem.Creator;
import rinde.sim.problem.common.DynamicPDPTWProblem.SimulationInfo;
import rinde.sim.problem.common.DynamicPDPTWProblem.StopCondition;
import rinde.sim.problem.common.ObjectiveFunction;
import rinde.sim.problem.common.StatsTracker.StatisticsDTO;
import rinde.sim.problem.gendreau06.Gendreau06ObjectiveFunction;
import rinde.sim.problem.gendreau06.Gendreau06Parser;
import rinde.sim.problem.gendreau06.Gendreau06Scenario;

public class GendreauSimulationTaskSelfsufficient extends GendreauSimulationTask {
	/**
	 *
	 */
	private static final long serialVersionUID = 4253566284926811038L;
	protected final String actualScenario;

	public GendreauSimulationTaskSelfsufficient(final String scenarioKey, final Heuristic<GendreauContext> p, final int vehicles, final long tick, final SolutionType t, final String scenario) {
		super(scenarioKey, p, vehicles, tick, t);
		this.actualScenario = scenario;
	}

	@Override
	public void run() {
		final ObjectiveFunction objFunc = new Gendreau06ObjectiveFunction();
		try {
			//System.out.println(taskData.getId());
			final Gendreau06Scenario scenario = Gendreau06Parser.parse(new BufferedReader(new StringReader(this.actualScenario)), scenarioKey, numVehicles, tickSize);
			final DynamicPDPTWProblem problem = new DynamicPDPTWProblem(scenario, 123, new CoordinationModel());

			if (solutionType == SolutionType.MYOPIC) {
				problem.addCreator(AddVehicleEvent.class, new Creator<AddVehicleEvent>() {
					@Override
					public boolean create(final Simulator sim, final AddVehicleEvent event) {
						return sim.register(new MyopicTruck(event.vehicleDTO, taskData));
					}
				});
			} else {
//				problem.addCreator(AddParcelEvent.class, new Creator<AddParcelEvent>() {
//					public boolean create(Simulator sim, AddParcelEvent event) {
////						return sim.register(new AuctionParcel(event.parcelDTO));
//					}
//				});
//				problem.addCreator(AddVehicleEvent.class, new Creator<AddVehicleEvent>() {
//					public boolean create(Simulator sim, AddVehicleEvent event) {
//						return sim.register(new AuctionTruck(event.vehicleDTO, taskData));
//					}
//				});
			}
			problem.addStopCondition(new StopCondition() {
				@Override
				public boolean isSatisfiedBy(final SimulationInfo context) {
					return false;// context.stats.computationTime > 5 * 60 *
									// 1000;
				}
			});
			problem.addStopCondition(new StopCondition() {
				@Override
				public boolean isSatisfiedBy(final SimulationInfo context) {

//						return
//								JONATHAN AANPASSING
					return context.stats.simulationTime >
						(scenarioKey.contains("_450")
							? 2 * 7.5 * 60 * 60 * 1000
							: 2 * 4 * 60 * 60 * 1000);
				}
			});
			preSimulate(problem);
			final StatisticsDTO stats = problem.simulate();
			final boolean isValid = objFunc.isValidResult(stats);

			final float fitness = isValid ? (float) objFunc.computeCost(stats) : Float.MAX_VALUE;
			setResult(new ResultDTO(scenarioKey, taskData.getId(), stats, fitness));

			//System.out
			//		.println(fitness + " valid:" + isValid + " task done: " + objFunc.printHumanReadableFormat(stats));
			// we don't throw an exception when just one vehicle has moved, this
			// usually just indicates a very bad solution and is the reason why
			// it didn't finish in time
			// if (!isValid && stats.movedVehicles > 1) {
			// throw new SimulationException("Fail: " + taskData, taskData,
			// stats, scenarioKey);
			// }
		} catch (final SimulationException e) {
			throw e;
		} catch (final Exception e) {
			throw new RuntimeException("Failed simulation task: " + taskData + " " + scenarioKey, e);
		}
	}
}
