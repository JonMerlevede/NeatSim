package neatsim.distributedtests;
import java.io.IOException;

import neatsim.core.evaluators.GendreauEvaluator;
import neatsim.localtests.TestSimulationEvaluatorHelper;

import org.junit.Test;

import rinde.evo4mas.gendreau06.GSimulationTask.SolutionType;
public class TestSimulationEvaluatorDistributed {
	private final TestSimulationEvaluatorHelper helper;

	public TestSimulationEvaluatorDistributed() {
		helper = new TestSimulationEvaluatorHelper();
	}

	@Test
	public void testSingleDistributedEvaluator() throws IOException {
		helper.testEvaluator(
				GendreauEvaluator.ComputationStrategy.DISTRIBUTED,
				SolutionType.MYOPIC,
				false, // multiple individuals in population
				false); // identical individuals in population
	}

	@Test
	public void testMultipleDifferentDistributedEvaluator() throws IOException {
		helper.testEvaluator(
				GendreauEvaluator.ComputationStrategy.DISTRIBUTED,
				SolutionType.MYOPIC,
				true, // multiple individuals in population
				false); // identical individuals in population
	}

	@Test
	public void testMultipleIdenticalDistributedEvaluator() throws IOException {
		helper.testEvaluator(
				GendreauEvaluator.ComputationStrategy.DISTRIBUTED,
				SolutionType.MYOPIC,
				true, // multiple individuals in population
				true); // identical individuals in population
	}
}
