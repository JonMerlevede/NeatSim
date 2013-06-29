package neatsim.core.fitnesstransformers;

import java.util.List;

import neatsim.server.thrift.CFitnessInfo;

public class Invert implements FitnessTransformer {
	public static final double MULTIPLIER = 10000;
	@Override
	public void transform(final List<? extends CFitnessInfo> infos) {
		for (final CFitnessInfo i : infos) {
			if (i.getFitness() <= 0)
				throw new IllegalArgumentException();
			i.setFitness(MULTIPLIER/i.getFitness());
		}
	}

}
