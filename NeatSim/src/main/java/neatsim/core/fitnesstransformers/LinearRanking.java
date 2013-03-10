package neatsim.core.fitnesstransformers;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import neatsim.server.thrift.CFitnessInfo;
import neatsim.util.AssertionHelper;

public class LinearRanking implements FitnessTransformer {
	protected final double selectivePressure;

	public LinearRanking(final double selectivePressure) {
		this.selectivePressure = selectivePressure;
	}

	@Override
	public void transform(final List<? extends CFitnessInfo> infos) {
		assert AssertionHelper.isEffectiveCollection(infos);
		assert selectivePressure > 0 && selectivePressure <= 2;

		final Integer[] ranksMinusOne = new Integer[infos.size()];
		// ranksMinusOne = {0,1,2,...,infos.size()-1}
		for (int i = 0; i < infos.size(); i ++) {
			ranksMinusOne[i] = i;
		}
		Arrays.sort(ranksMinusOne, new Comparator<Integer>() {
			@Override
			public int compare(final Integer arg0, final Integer arg1) {
				// Remember that getFitness() actually returns a cost; a higher
				// value therefore means a higher rank (higher = better)
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
