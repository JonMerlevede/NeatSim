package neatsim.localtests;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import neatsim.core.blackbox.neural.NeuralNetwork;
import neatsim.core.evaluators.gendreau.GendreauEvaluator;
import neatsim.core.evaluators.gendreau.GendreauScenario;
import neatsim.core.fitnesstransformers.Invert;
import neatsim.core.stopconditions.SimpleStopcondition;
import neatsim.util.FilenameReader;
import neatsim.util.NeuralNetworkReader;

import org.junit.Test;

import rinde.evo4mas.common.ResultDTO;
import rinde.evo4mas.gendreau06.GSimulationTask.SolutionType;

public class TestSimulationEvaluatorLocal {
	protected static final String SCENARIO_FOLDER_PATH = "src/test/resources/scenarios/";
	protected static final String SCENARIO_FILE_SUFFIX = "240_24";
	protected static final int SCENARIO_NUMBER_MATCHING = 5;
	protected static final String GENOME_FOLDER_PATH = "src/test/resources/testrun/";
	protected static final String GENOME_FILE_PREFIX = "xml";
	protected static final String GENOME_FILE_SUFFIX = "testrun";
	protected static final int GENOME_NUMBER = 15;

	protected static final int NUMBER_OF_EVALUATIONS_NONFINAL_SCENARIO = 2;
	protected static final int NUMBER_OF_EVALUATIONS_FINAL_SCENARIO = 3;
	protected static final int NUMBER_OF_TIMESTEPS_PER_ACTIVATION = 3;
	protected static final GendreauEvaluator.InternalEvaluator COMPUTATION_STRATEGY = GendreauEvaluator.InternalEvaluator.MULTITHREADED;

	protected TestSimulationEvaluatorHelper helper;

	public TestSimulationEvaluatorLocal() {
		helper = new TestSimulationEvaluatorHelper();
	}

	@Test
	public void testEvaluateGenomes() throws IOException {
		// Load scenario's from hard disk
		final List<GendreauScenario> scenarios = GendreauScenario.load(
				SCENARIO_FOLDER_PATH, SCENARIO_FILE_SUFFIX);
		assertEquals(SCENARIO_NUMBER_MATCHING, scenarios.size());
		// Create neural network XML reader
		final NeuralNetworkReader reader = new NeuralNetworkReader(
				NUMBER_OF_TIMESTEPS_PER_ACTIVATION);
		// Create evaluator
		final GendreauEvaluator evaluator = new GendreauEvaluator(scenarios,
				SolutionType.MYOPIC,
				COMPUTATION_STRATEGY,
				new SimpleStopcondition(),
				new Invert(),
				GendreauEvaluator.DEFAULT_NUMBER_OF_SCENARIOS_PER_GENERATION);
		// Load paths to genome files from hard disk
		final List<String> genomePaths = (new FilenameReader())
				.getFilePathsFromDirectory(
						GENOME_FOLDER_PATH,
						GENOME_FILE_PREFIX,
						GENOME_FILE_SUFFIX);
		assertEquals(GENOME_NUMBER, genomePaths.size());
		// Read genome files from disk to ANNs using XML reader
		final List<NeuralNetwork> genomes = new ArrayList<NeuralNetwork>(
				genomePaths.size());
		for (final String genomePath : genomePaths) {
			final List<NeuralNetwork> population = reader.readFile(genomePath).neuralNetworks;
			assertEquals(population.size(), 1);
			genomes.add(population.get(0));
		}
		// Evaluate all genomes
		final ResultDTO[][] fitnesses = evaluator.evaluateGenomes(genomes,
				NUMBER_OF_EVALUATIONS_NONFINAL_SCENARIO,
				NUMBER_OF_EVALUATIONS_FINAL_SCENARIO, true);
		assertEquals(genomes.size(), fitnesses.length);
		// Assert that we get the correct number of fitnesses
		for (int i=0; i < fitnesses.length -1; i++) {
			assertEquals(NUMBER_OF_EVALUATIONS_NONFINAL_SCENARIO, fitnesses[i].length);
		}
		assertEquals(NUMBER_OF_EVALUATIONS_FINAL_SCENARIO, fitnesses[fitnesses.length-1].length);

		// Print resulting fitnesses to screen
//		System.out.println("Fitnesses: (number of genomes = " + fitnesses.length + ")");
//		final String[] pfitnesses = new String[fitnesses.length];
//		for (int i=0; i < fitnesses.length; i++) {
//			pfitnesses[i] = Arrays.toString(fitnesses[i]);
//		}
//		System.out.println(Arrays.toString(pfitnesses));
	}


	@Test
	public void testSingleLocalMultithreadedEvaluator() throws IOException {
		helper.testEvaluator(
				GendreauEvaluator.InternalEvaluator.MULTITHREADED,
				SolutionType.MYOPIC,
				false,
				false);
	}

	@Test
	public void testMultipleDifferentLocalMultithreadedEvaluator() throws IOException {
		helper.testEvaluator(
				GendreauEvaluator.InternalEvaluator.MULTITHREADED,
				SolutionType.MYOPIC,
				true,
				false);
	}

	@Test
	public void testMultipleIdenticalLocalMultithreadedEvaluator() throws IOException {
		helper.testEvaluator(
				GendreauEvaluator.InternalEvaluator.MULTITHREADED,
				SolutionType.MYOPIC,
				true,
				true);
	}

	@Test
	public void testSingleLocalSinglethreadedEvaluator() throws IOException {
		helper.testEvaluator(
				GendreauEvaluator.InternalEvaluator.SINGLETHREADED,
				SolutionType.MYOPIC,
				false,
				false);
	}

	@Test
	public void testMultipleLocalSinglethreadedEvaluator() throws IOException {
		helper.testEvaluator(
				GendreauEvaluator.InternalEvaluator.SINGLETHREADED,
				SolutionType.MYOPIC,
				true,
				false);
	}

	@Test
	public void testMultipleIdenticalSinglethreadedEvaluator() throws IOException {
		helper.testEvaluator(
				GendreauEvaluator.InternalEvaluator.SINGLETHREADED,
				SolutionType.MYOPIC,
				true,
				true);
	}


}
