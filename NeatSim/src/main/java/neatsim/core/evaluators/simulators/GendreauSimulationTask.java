package neatsim.core.evaluators.simulators;

import rinde.ecj.Heuristic;
import rinde.evo4mas.gendreau06.GSimulationTask;
import rinde.evo4mas.gendreau06.GendreauContext;

public class GendreauSimulationTask extends GSimulationTask {
	private static final long serialVersionUID = -3859296545351835293L;

	public GendreauSimulationTask(final String scenario, final Heuristic<GendreauContext> p,
			final int vehicles, final long tick, final SolutionType t) {
		super(scenario, p, vehicles, tick, t);
	}

	@Override
	public void run() {
		try {
			super.run();
		} catch (final Exception e) {
			setException(e);
			System.out.println("Exception occured :(");
		}
	}
}
