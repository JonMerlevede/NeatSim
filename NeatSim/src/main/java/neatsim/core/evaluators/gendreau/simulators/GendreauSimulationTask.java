package neatsim.core.evaluators.gendreau.simulators;

import rinde.ecj.Heuristic;
import rinde.evo4mas.gendreau06.GSimulationTask;
import rinde.evo4mas.gendreau06.GendreauContext;

/**
 * See {@see GSimulationTask}.
 * @author Jonathan Merlevede
 *
 */
public class GendreauSimulationTask extends GSimulationTask {
	private static final long serialVersionUID = -3859296545351835293L;

	public GendreauSimulationTask(
			final String scenarioName,
			final Heuristic<GendreauContext> p,
			final int vehicles,
			final long tick,
			final SolutionType t) {
		super(scenarioName, p, vehicles, tick, t);
	}

	@Override
	public void run() {
		try {
			super.run();
		} catch (final Exception e) {
			setException(e);
			System.out.println("Exception occured :(");
			System.out.println(getException());
			getException().getCause().printStackTrace();
		}
	}
}
