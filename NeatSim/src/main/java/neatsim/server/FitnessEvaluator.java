package neatsim.server;
import java.io.FileNotFoundException;
import java.io.IOException;

import neatsim.core.blackbox.neural.NeuralNetwork;
import neatsim.core.evaluators.PopulationEvaluator;
import neatsim.core.evaluators.binarysixmultiplexer.BinarySixEvaluator;
import neatsim.core.evaluators.gendreau.GendreauEvaluator;
import neatsim.core.evaluators.gendreau.GendreauScenario;
import neatsim.core.evaluators.xor.XorEvaluator;
import neatsim.server.thrift.CFastCyclicNetwork;
import neatsim.server.thrift.CFitnessEvaluatorService;
import neatsim.server.thrift.CFitnessInfo;
import neatsim.server.thrift.CPopulationFitness;
import neatsim.server.thrift.CPopulationInfo;

import org.apache.thrift.TException;

import rinde.evo4mas.gendreau06.GSimulationTask.SolutionType;

/**
 * Class that implements the Thrift interface of the FitnessEvaluatorService.
 * Method calls over Thrift are redirected to this class.
 *
 * For more information, refer to the Thrift interface file.
 *
 * @author Jonathan Merlevede
 *
 */
public class FitnessEvaluator implements CFitnessEvaluatorService.Iface {
	/**
	 * Reference to the object that provides the XOR evaluation operation.
	 */
	protected final XorEvaluator xorEvaluator;
	/**
	 * Reference to the object that provides the simulation evaluation operation.
	 */
	protected final GendreauEvaluator gendreauEvaluator;

	protected final PopulationEvaluator binarySixEvaluator;

	protected final int numberOfScenariosPerGeneration;


	/**
	 * Creates a new fitness evaluator.
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public FitnessEvaluator() throws IOException {

		xorEvaluator = new XorEvaluator();
//		final GendreauEvaluatorFactory seh = GendreauEvaluatorFactory.newInstance();
		//new GendreauEvaluator(gendreauScenarios, solutionType, computationStrategy, stopcondition, fitnessTransformer)
		final ServerConfig conf = new ServerConfig();

		gendreauEvaluator = new GendreauEvaluator(
				GendreauScenario.load(conf.getDataDirectory(), conf.getDataPrefix(), conf.getDataSuffix()),
				//GendreauScenario.load("mydata", "train", "_450_24"),
				//GendreauScenario.load("data", "req", "_240_24"),
				SolutionType.MYOPIC,
				conf.getStrategy(),
				conf.getStopcondition(),
				conf.getFitnessTransformer(),
				GendreauEvaluator.DEFAULT_NUMBER_OF_SCENARIOS_PER_GENERATION);
				//GendreauEvaluator.ComputationStrategy.DISTRIBUTED);
		binarySixEvaluator = new BinarySixEvaluator();
		numberOfScenariosPerGeneration = conf.getScenariosPerGeneration();

		System.out.println("System evaluator intialised!");
	}

	@Override
	public CFitnessInfo calculateXorPhenotypeFitness(final CFastCyclicNetwork ann)
			throws TException {
		final CFitnessInfo info = xorEvaluator.evaluatePhenotype(ann);
		if (info.isStopConditionSatisfied()) {
			System.out.println("Found XOR! ");
			System.out.println(new NeuralNetwork(ann));
		}
		return info;
	}

	@Override
	public CPopulationFitness calculateXorPopulationFitness(
			final CPopulationInfo populationInfo) throws TException {
		System.out.println("calculateXorPopulationFitness called.");
		return xorEvaluator.evaluatePopulation(populationInfo);
	}

	@Override
	public CPopulationFitness calculateSixMultiplexerPopulationFitness(
			final CPopulationInfo populationInfo) throws TException {
		System.out.println("calculateSixMultiplexerPopulationFitness called.");
		final CPopulationFitness populationFitness = binarySixEvaluator.evaluatePopulation(populationInfo);
		for (final CFitnessInfo info : populationFitness.getFitnessInfos()) {
			if (info.getFitness() >= BinarySixEvaluator.STOP_FITNESS)
				System.out.println("calculateSixMultiplexerPopulationFitness found solution!");
		}
		return populationFitness;
	}

	@Override
	public CPopulationFitness calculateSimPopulationFitness(
			final CPopulationInfo populationInfo) throws TException {
		System.out.println("calculateSimPopulationFitness called (" +
			populationInfo.getPhenomes().size() + " individuals).");
//		final List<CFitnessInfo> fitnessInfos = simEvaluator
//				.evaluatePopulation(populationInfo);
		return gendreauEvaluator.evaluatePopulation(populationInfo, numberOfScenariosPerGeneration);
		// return simEvaluator.parallelEvaluatePopulation(populationInfo);
	}
}