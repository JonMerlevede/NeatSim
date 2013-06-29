package neatsim.core.evaluators.gendreau.simulators;

import rinde.ecj.Heuristic;
import rinde.evo4mas.gendreau06.GendreauContext;
import rinde.sim.problem.common.DynamicPDPTWProblem;

public class GendreauSimulationTaskVisible extends GendreauSimulationTask {

	/**
	 *
	 */
	private static final long serialVersionUID = 6082477883231057763L;

	public GendreauSimulationTaskVisible(final String scenarioName,
			final Heuristic<GendreauContext> p, final int vehicles, final long tick, final SolutionType t) {
		super(scenarioName, p, vehicles, tick, t);
	}

	@Override
	protected void preSimulate(final DynamicPDPTWProblem problem) {
		problem.enableUI();
	}

}
