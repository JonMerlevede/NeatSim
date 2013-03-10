package neatsim.localtests;
import java.io.IOException;

import neatsim.core.evaluators.GendreauEvaluator;

import org.junit.Test;

import rinde.evo4mas.gendreau06.GSimulationTask.SolutionType;

public class TestSimulationEvaluatorLocal {
	protected TestSimulationEvaluatorHelper helper;

	public TestSimulationEvaluatorLocal() {
		helper = new TestSimulationEvaluatorHelper();
	}

	@Test
	public void testSingleLocalMultithreadedEvaluator() throws IOException {
		helper.testEvaluator(
				GendreauEvaluator.ComputationStrategy.MULTITHREADED,
				SolutionType.MYOPIC,
				false,
				false);
	}

	@Test
	public void testMultipleDifferentLocalMultithreadedEvaluator() throws IOException {
		helper.testEvaluator(
				GendreauEvaluator.ComputationStrategy.MULTITHREADED,
				SolutionType.MYOPIC,
				true,
				false);
	}

	@Test
	public void testMultipleIdenticalLocalMultithreadedEvaluator() throws IOException {
		helper.testEvaluator(
				GendreauEvaluator.ComputationStrategy.MULTITHREADED,
				SolutionType.MYOPIC,
				true,
				true);
	}

	@Test
	public void testSingleLocalSinglethreadedEvaluator() throws IOException {
		helper.testEvaluator(
				GendreauEvaluator.ComputationStrategy.SINGLETHREADED,
				SolutionType.MYOPIC,
				false,
				false);
	}

	@Test
	public void testMultipleLocalSinglethreadedEvaluator() throws IOException {
		helper.testEvaluator(
				GendreauEvaluator.ComputationStrategy.SINGLETHREADED,
				SolutionType.MYOPIC,
				true,
				false);
	}

	@Test
	public void testMultipleIdenticalSinglethreadedEvaluator() throws IOException {
		helper.testEvaluator(
				GendreauEvaluator.ComputationStrategy.SINGLETHREADED,
				SolutionType.MYOPIC,
				true,
				true);
	}


}
