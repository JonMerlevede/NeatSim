package neatsim;

import rinde.sim.problem.common.StatsTracker.StatisticsDTO;


public class Gendreau06ObjectiveFunction extends rinde.sim.problem.gendreau06.Gendreau06ObjectiveFunction {
	//@Override
	//public long travelTime(final StatisticsDTO stats) {
	//	return super.travelTime(stats);
	//}

	@Override
	public double tardiness(final StatisticsDTO stats) {
		return super.tardiness(stats);
	}

	@Override
	public double overTime(final StatisticsDTO stats) {
		return super.overTime(stats);
	}


}
