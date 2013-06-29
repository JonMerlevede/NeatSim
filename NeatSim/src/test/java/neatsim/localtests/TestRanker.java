package neatsim.localtests;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import neatsim.core.FitnessInfo;
import neatsim.core.fitnesstransformers.Absolute;
import neatsim.core.fitnesstransformers.FitnessTransformer;
import neatsim.core.fitnesstransformers.Invert;
import neatsim.core.fitnesstransformers.LinearRanking;
import neatsim.server.thrift.CFitnessInfo;

import org.junit.Test;

public class TestRanker {
	public static double EPSILON = 10e-5;
//	FitnessTransformer transformer = new FitnessTransformer();

	private void transformAndCompare(
			final double[] fitnesses,
			final double[] expected,
			final FitnessTransformer transformer) {
		assert fitnesses.length == expected.length;
		final List<CFitnessInfo> list = new ArrayList<CFitnessInfo>(fitnesses.length);
		for (final double fitness : fitnesses) {
			list.add(new FitnessInfo(fitness));
		}
		transformer.transform(list);
		for (int i = 0; i < fitnesses.length; i++) {
			assertEquals(expected[i], list.get(i).fitness, EPSILON);
		}
	}

	@Test
	public void testInvert() {
		final double[] fitnesses = { 1, 2, 5, 0.1 };
		final double[] expecteds = { 1d/1, 1d/2, 1d/5, 1d/0.1 };
		for (int i = 0; i< expecteds.length; i++) {
			expecteds[i] = expecteds[i] * Invert.MULTIPLIER;
		}
		transformAndCompare(fitnesses, expecteds, new Invert());
	}

	@Test
	public void costToAbsoluteFitness() {
		//									Expected rankings
		//									  5  2   3  4  1
		final double[] fitnesses = { 1, 10, 8, 2, 17 };
		final double[] expecteds = { 16, 7, 9, 15, 0 };
		transformAndCompare(fitnesses, expecteds, new Absolute());
	}

	@Test
	public void testLinearRankingOfCosts() {
		//									Expected rankings
		//									  5  2   3  4  1
		final double[] fitnesses = { 1, 10, 8, 2, 17 };
		final double[] expecteds = {2d, 2d*1/4, 2d*2/4, 2d*3/4, 0d};
		transformAndCompare(fitnesses, expecteds, new LinearRanking(2));
	}
}
