package neatsim.stats;
//
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//import neatsim.core.NeuralNetwork;
//import neatsim.core.evaluators.gendreau.GendreauEvaluator;
//import neatsim.core.evaluators.gendreau.GendreauScenario;
//import neatsim.core.fitnesstransformers.Invert;
//import neatsim.core.stopconditions.SimpleStopcondition;
//import neatsim.util.FilenameReader;
//import neatsim.util.NeuralNetworkReader;
//import rinde.evo4mas.gendreau06.GSimulationTask.SolutionType;
//
//
//
public class FitnessCalculator {
//	protected static final String SCENARIO_FOLDER_PATH = "src/test/resources/scenarios/";
//	protected static final String SCENARIO_FILE_SUFFIX = "240_24";
//	protected static final String GENOME_FOLDER_PATH = "src/test/resources/testrun/";
//	protected static final String GENOME_FILE_PREFIX = "xml";
//	protected static final String GENOME_FILE_SUFFIX = "testrun";
//	protected static final String OUT_FILE = "/C:\\Users\\Jonathan\\Dropbox\\univ\\ma2\\thesis\\testoutput.csv";
//
//
//	protected static final int NUMBER_OF_EVALUATIONS_NONFINAL_SCENARIO = 2;
//	protected static final int NUMBER_OF_EVALUATIONS_FINAL_SCENARIO = 3;
//	protected static final int NUMBER_OF_TIMESTEPS_PER_ACTIVATION = 3;
//	protected static final GendreauEvaluator.InternalEvaluator COMPUTATION_STRATEGY = GendreauEvaluator.InternalEvaluator.MULTITHREADED;
//
//	public static void main(final String[] args) throws IOException {
//		new FitnessCalculator();
//	}
//
//	public FitnessCalculator() throws IOException {
//		final double[][] individualsFitnesses = calculateIndividualsFitnesses();
//		writeFitnesses(individualsFitnesses);
//	}
//
//	private void writeFitnesses(final double[][] isFs) throws IOException {
//		final File file = new File(OUT_FILE);
//		final FileWriter rawWriter = new FileWriter(file, false);
//		final BufferedWriter writer = new BufferedWriter(rawWriter);
//
//		for (int i = 0; i < isFs.length; i++) {
//			final double[] iFs = isFs[i];
//			for (int j = 0; j < iFs.length - 1; j++) {
//				writer.write(iFs[j]+",");
//			}
//			writer.write(iFs[iFs.length -1]+"");
//			if (i < isFs.length - 1)
//				writer.write("\n");
//		}
//
//		writer.close();
//		rawWriter.close();
//	}
//
//	private double[][] calculateIndividualsFitnesses() throws IOException {
//		// Load scenario's from hard disk
//		final List<GendreauScenario> scenarios = GendreauScenario.load(
//				SCENARIO_FOLDER_PATH, SCENARIO_FILE_SUFFIX);
//		// Create neural network XML reader
//		final NeuralNetworkReader reader = new NeuralNetworkReader(
//				NUMBER_OF_TIMESTEPS_PER_ACTIVATION);
//		// Create evaluator
//		final GendreauEvaluator evaluator = new GendreauEvaluator(scenarios,
//				SolutionType.MYOPIC,
//				COMPUTATION_STRATEGY,
//				new SimpleStopcondition(),
//				new Invert(),
//				GendreauEvaluator.DEFAULT_NUMBER_OF_SCENARIOS_PER_GENERATION);
//		// Load paths to genomes from hard disk
//		final List<String> genomePaths = (new FilenameReader())
//				.getFilePathsFromDirectory(
//						GENOME_FOLDER_PATH,
//						GENOME_FILE_PREFIX,
//						GENOME_FILE_SUFFIX);
//		// Generate phenomes (ANNs) from genomes using XML reader
//		final List<NeuralNetwork> genomes = new ArrayList<NeuralNetwork>(
//				genomePaths.size());
//		for (final String genomePath : genomePaths) {
//			final List<NeuralNetwork> population = reader.readFile(genomePath);
//			assert population.size() == 1;
//			genomes.add(population.get(0));
//		}
//		// Evaluate all individuals
//		final double[][] fitnesses = evaluator.evaluateGenomes(genomes,
//				NUMBER_OF_EVALUATIONS_NONFINAL_SCENARIO,
//				NUMBER_OF_EVALUATIONS_FINAL_SCENARIO, true);
//		assert genomes.size() == fitnesses.length;
//		assert assertCorrectNumber(fitnesses);
//		// Return calculated fitness values
//		return fitnesses;
//	}
//
//	private boolean assertCorrectNumber(final double[][] fitnesses) {
//		// Assert that we get the correct number of fitnesses
//		for (int i=0; i < fitnesses.length -1; i++) {
//			assert NUMBER_OF_EVALUATIONS_NONFINAL_SCENARIO == fitnesses[i].length;
//		}
//		assert NUMBER_OF_EVALUATIONS_FINAL_SCENARIO == fitnesses[fitnesses.length-1].length;
//		return true;
//	}
}
