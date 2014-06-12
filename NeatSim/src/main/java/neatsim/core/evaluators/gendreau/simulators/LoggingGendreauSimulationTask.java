package neatsim.core.evaluators.gendreau.simulators;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import neatsim.Gendreau06ObjectiveFunction;
import rinde.ecj.Heuristic;
import rinde.evo4mas.gendreau06.GSimulationTask;
import rinde.evo4mas.gendreau06.GendreauContext;
import rinde.sim.core.TickListener;
import rinde.sim.core.TimeLapse;
import rinde.sim.problem.common.DynamicPDPTWProblem;
import rinde.sim.problem.common.StatsTracker.StatisticsDTO;

/**
 * Like a {@link GendreauSimulationTask}, but outputs the total simulation time,
 * travel time, overtime and tardiness to the system output at each simulation
 * time tick.
 *
 * @author Jonathan Merlevede
 *
 */
public class LoggingGendreauSimulationTask extends GSimulationTask {
	private static final long serialVersionUID = -3859296545351835293L;

	public final List<Double> traveltime = new ArrayList<>();
	public final List<Double> overtime = new ArrayList<>();
	public final List<Double> tardiness = new ArrayList<>();
	public final List<Long> times = new ArrayList<>();

	private DynamicPDPTWProblem problem;

	public LoggingGendreauSimulationTask(
			final String scenarioName,
			final Heuristic<GendreauContext> p,
			final int vehicles,
			final long tick,
			final SolutionType t) {
		super(scenarioName, p, vehicles, tick, t);

	}

	@Override
	protected void preSimulate(final DynamicPDPTWProblem problem) {
		this.problem = problem;
		this.times.clear();
		this.traveltime.clear();
		this.overtime.clear();
		this.tardiness.clear();

		final Gendreau06ObjectiveFunction f = new Gendreau06ObjectiveFunction();
		problem.getSimulator().addTickListener(new TickListener() {
			private final int maxi=10;
			private int i = maxi;

			@Override
			public void tick(final TimeLapse timeLapse) {

			}

			@Override
			public void afterTick(final TimeLapse timeLapse) {
				final StatisticsDTO s = problem.getStatistics();
				i--;
				if (i<=0) {
					times.add(s.simulationTime);
					traveltime.add(f.travelTime(s));
					overtime.add(f.overTime(s));
					tardiness.add(f.tardiness(s));
					i = maxi;
				}
			}
		});

	}

	protected <E> String printCsv(final List<E> l) {
		final Iterator<E> it= l.iterator();
		final StringBuilder b = new StringBuilder();
		while (it.hasNext()) {
			b.append(it.next());
			if (it.hasNext())
				b.append(",");
		}
		return b.toString();
	}

	@Override
	public void run() {
		try {
			super.run();
			System.out.println(problem.getStatistics());
			System.out.println(printCsv(times));
			System.out.println(printCsv(traveltime));
			System.out.println(printCsv(overtime));
			System.out.println(printCsv(tardiness));
			// Problem is not serializable !
			problem = null;
		} catch (final Exception e) {
			setException(e);
			System.out.println("Exception occured :(");
			System.out.println(getException());
			getException().getCause().printStackTrace();
		}
	}
}
