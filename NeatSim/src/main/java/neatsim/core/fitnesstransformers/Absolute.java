package neatsim.core.fitnesstransformers;

import java.util.List;

import neatsim.server.thrift.CFitnessInfo;

public class Absolute implements FitnessTransformer {

	@Override
	public void transform(final List<? extends CFitnessInfo> infos) {
		assert infos != null;
		double max = 0;
		for (final CFitnessInfo i : infos) {
			max = i.getFitness() > max ? i.getFitness() : max;
		}
		for (final CFitnessInfo i : infos) {
			i.setFitness(max - i.getFitness());
		}
	}
}
