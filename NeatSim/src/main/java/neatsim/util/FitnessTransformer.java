package neatsim.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import neatsim.server.thrift.CFitnessInfo;

public class FitnessTransformer {
	public FitnessTransformer() {

	}

	public void costToAbsoluteFitness(final List<? extends CFitnessInfo> infos) {
		assert infos != null;
		double max = 0;
		for (final CFitnessInfo i : infos) {
			max = i.getFitness() > max ? i.getFitness() : max;
		}
		for (final CFitnessInfo i : infos) {
			i.setFitness(max - i.getFitness());
		}
	}

	public void linearRankingOfCosts(final List<? extends CFitnessInfo> infos, final double selectivePressure) {
		assert AssertionHelper.isEffectiveCollection(infos);
		assert selectivePressure > 0 && selectivePressure <= 2;

		final Integer[] ranksMinusOne = new Integer[infos.size()];
		for (int i = 0; i < infos.size(); i ++) {
			ranksMinusOne[i] = i;
		}
		Arrays.sort(ranksMinusOne, new Comparator<Integer>() {
			@Override
			public int compare(final Integer arg0, final Integer arg1) {
				if (infos.get(arg0).getFitness() < infos.get(arg1).getFitness())
					return 1;
				if (infos.get(arg0).getFitness() == infos.get(arg1).getFitness())
					return 0;
				return -1;
			}
		});
		// This prints out sorted fitness values
//		for (int i = 0; i < infos.size(); i++)
//			System.out.println(""+infos.get(ranksMinusOne[i]).getFitness());
		final int sizeMinusOne = infos.size() -1 ;
		for (int i = 0; i < infos.size(); i++) {
			final double rankingFitness =
					2 - selectivePressure +
					2*(selectivePressure - 1)*(i)/sizeMinusOne;
			//rankingFitness = rankingFitness*rankingFitness + 1;
			infos.get(ranksMinusOne[i]).setFitness(rankingFitness);
			// This prints out sorted distance (descending) and fitness values (ascending)
//			System.out.println("Distance: "
//					+ infos.get(ranksMinusOne[i]).getAuxFitness().get(0).getValue()
//					+ ", fitness: "
//					+ infos.get(ranksMinusOne[i]).getFitness()
//					+ ", rank: "
//					+ i);
		}
	}
}
