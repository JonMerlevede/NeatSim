package neatsim;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import neatsim.server.thrift.CFitnessInfo;
import neatsim.server.thriftadapters.FitnessInfo;
import neatsim.util.FitnessTransformer;

import org.junit.Test;

public class TestRanker {
	public static double EPSILON = 10e-5;
	FitnessTransformer transformer = new FitnessTransformer();

	@Test
	public void costToAbsoluteFitness() {
		final List<? extends CFitnessInfo> list = Arrays.asList(
				new FitnessInfo(1),//0, rank 5
				new FitnessInfo(10),//1, rank 2
				new FitnessInfo(8),//2, rank 3
				new FitnessInfo(2),//3, rank 4
				new FitnessInfo(17));//4, rank 1
		transformer.costToAbsoluteFitness(list);
		final double[] expecteds = {16, 7, 9, 15, 0};
		for (int i = 0; i < expecteds.length; i++)
			assertEquals(expecteds[i], list.get(i).fitness, EPSILON);
	}

	@Test
	public void testLinearRankingOfCosts() {
		final List<? extends CFitnessInfo> list = Arrays.asList(
				new FitnessInfo(1),//0, rank 5
				new FitnessInfo(10),//1, rank 2
				new FitnessInfo(8),//2, rank 3
				new FitnessInfo(2),//3, rank 4
				new FitnessInfo(17));//4, rank 1
		transformer.linearRankingOfCosts(list, 2);
		final double[] expecteds = {2d, 2d*1/4, 2d*2/4, 2d*3/4, 0d};
		for (int i = 0; i < expecteds.length; i++)
			assertEquals(expecteds[i], list.get(i).fitness, EPSILON);
	}
}
