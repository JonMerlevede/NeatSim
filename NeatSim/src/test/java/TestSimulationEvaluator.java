import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import neatsim.core.FastCyclicNeuralNetwork;
import neatsim.evaluators.SimpleStopcondition;
import neatsim.evaluators.SimulationEvaluator;
import neatsim.evaluators.SimulationEvaluatorHelper;
import neatsim.sim.neuralnets.NeuralNetworkFactory;

import org.javatuples.Pair;
import org.junit.Before;
import org.junit.Test;

import rinde.evo4mas.gendreau06.GSimulationTask.SolutionType;

public class TestSimulationEvaluator {
	private NeuralNetworkFactory nnf;

	@Before
	public void InitializeSimulationEvaluator() {
		nnf = new NeuralNetworkFactory();
	}

	@Test
	public void testSingleLocalMultithreadedEvaluator() throws IOException {
		_testEvaluator(SimulationEvaluator.ComputationStrategy.MULTITHREADED,
				SolutionType.MYOPIC,
				false,
				false);
	}

	@Test
	public void testMultipleDifferentLocalMultithreadedEvaluator() throws IOException {
		_testEvaluator(SimulationEvaluator.ComputationStrategy.MULTITHREADED,
				SolutionType.MYOPIC,
				true,
				false);
	}

	@Test
	public void testMultipleIdenticalLocalMultithreadedEvaluator() throws IOException {
		_testEvaluator(SimulationEvaluator.ComputationStrategy.MULTITHREADED,
				SolutionType.MYOPIC,
				true,
				true);
	}

	@Test
	public void testSingleLocalSinglethreadedEvaluator() throws IOException {
		_testEvaluator(SimulationEvaluator.ComputationStrategy.SINGLETHREADED,
				SolutionType.MYOPIC,
				false,
				false);
	}

	@Test
	public void testMultipleLocalSinglethreadedEvaluator() throws IOException {
		_testEvaluator(SimulationEvaluator.ComputationStrategy.SINGLETHREADED,
				SolutionType.MYOPIC,
				true,
				false);
	}

	@Test
	public void testMultipleIdenticalSinglethreadedEvaluator() throws IOException {
		_testEvaluator(SimulationEvaluator.ComputationStrategy.SINGLETHREADED,
				SolutionType.MYOPIC,
				true,
				true);
	}

	public void _testEvaluator(
			final SimulationEvaluator.ComputationStrategy strategy,
			final SolutionType solutionType,
			final boolean multiple,
			final boolean identical) {
		final SimulationEvaluatorHelper seh = new SimulationEvaluatorHelper();
		Pair<List<String>,List<String>> tp = null;
		try {
			tp = seh.readScenariosFromDirectory("data/", "_240_24");
		} catch (final IOException e) { e.printStackTrace(); }
		final List<String> scenarioNames = tp.getValue0();
		final List<String> scenarioContents = tp.getValue1();

		final SimulationEvaluator se = new SimulationEvaluator(
				scenarioNames,
				scenarioContents,
				1, // number of scenarios per generation
				solutionType,
				strategy,
				new SimpleStopcondition());

		final List<FastCyclicNeuralNetwork> fcnns = new ArrayList<FastCyclicNeuralNetwork>();
		fcnns.add(nnf.createDist());
		if (multiple) {
			// There are now multiple anns in the list, but all anns are different
			fcnns.add(nnf.createClosest());
			fcnns.add(nnf.createDist2());
			if (identical) {
				// There are now multiple anns in the list, including identical ones
				fcnns.add(nnf.createDist());
			}
		}

		se.evaluatePopulation(fcnns, 1);
	}
}
