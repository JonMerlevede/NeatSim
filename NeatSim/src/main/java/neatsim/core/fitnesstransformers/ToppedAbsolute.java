package neatsim.core.fitnesstransformers;

import java.util.List;

import neatsim.server.thrift.CFitnessInfo;

public class ToppedAbsolute implements FitnessTransformer {
	double maxCost;

	public ToppedAbsolute(final double maxCost) {
		this.maxCost = maxCost;
	}

	@Override
	public void transform(final List<? extends CFitnessInfo> infos) {
		for (final CFitnessInfo i : infos) {
			if (i.getFitness() <= 0d)
				throw new IllegalArgumentException();
			i.setFitness(Math.max(maxCost - i.getFitness(), 0d));
		}
	}
}
