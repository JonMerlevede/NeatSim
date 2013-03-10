package neatsim.core.fitnesstransformers;

import java.util.List;

import neatsim.server.thrift.CFitnessInfo;

public class Invert implements FitnessTransformer {

	@Override
	public void transform(final List<? extends CFitnessInfo> infos) {
		for (final CFitnessInfo i : infos) {
			if (i.getFitness() <= 0)
				throw new IllegalArgumentException();
			i.setFitness(1/i.getFitness());
		}
	}

}
