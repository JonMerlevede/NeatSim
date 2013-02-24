package neatsim.evaluators;
import java.io.IOException;
import java.util.List;

import neatsim.thrift.CFastCyclicNetwork;
import neatsim.thrift.CFitnessEvaluatorService;
import neatsim.thrift.CFitnessInfo;
import neatsim.thrift.CPopulationFitness;
import neatsim.thrift.CPopulationInfo;

import org.apache.thrift.TException;
import org.javatuples.Pair;

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
	protected final SimulationEvaluator simEvaluator;

	/**
	 * Creates a new fitness evaluator.
	 */
	public FitnessEvaluator() {
		xorEvaluator = new XorEvaluator();
		final SimulationEvaluatorHelper seh = new SimulationEvaluatorHelper();
		Pair<List<String>,List<String>> tp = null;
		try {
			tp = seh.readScenariosFromDirectory("data/", "_240_24");
		} catch (final IOException e) { e.printStackTrace(); }
		final List<String> scenarioNames = tp.getValue0();
		final List<String> scenarioContents = tp.getValue1();

		simEvaluator = new SimulationEvaluator(
				scenarioNames,
				scenarioContents,
				1, // number of scenarios per generation
				SolutionType.MYOPIC,
				SimulationEvaluator.ComputationStrategy.MULTITHREADED,
				new SimpleStopcondition());
		System.out.println("System evaluator intialised!");
	}

	@Override
	public CFitnessInfo calculateXorPhenotypeFitness(final CFastCyclicNetwork ann)
			throws TException {
		return xorEvaluator.evaluatePhenotype(ann);
	}

	@Override
	public CPopulationFitness calculateXorPopulationFitness(
			final CPopulationInfo populationInfo) throws TException {
		System.out.println("calculateXorPopulationFitness called.");
		return xorEvaluator.evaluatePopulation(populationInfo);
	}

	@Override
	public CPopulationFitness calculateSimPopulationFitness(
			final CPopulationInfo populationInfo) throws TException {
		System.out.println("calculateSimPopulationFitness called.");
		final List<CFitnessInfo> fitnessInfos = simEvaluator
				.evaluatePopulation(populationInfo);
		return new CPopulationFitness(fitnessInfos, fitnessInfos.size());
		// return simEvaluator.parallelEvaluatePopulation(populationInfo);
	}
}