package neatsim.server;
import neatsim.core.evaluators.PopulationEvaluator;
import neatsim.core.evaluators.GendreauEvaluator;
import neatsim.core.evaluators.GendreauScenario;
import neatsim.core.evaluators.XorEvaluator;
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
	protected final PopulationEvaluator gendreauEvaluator;

	/**
	 * Creates a new fitness evaluator.
	 */
	public FitnessEvaluator() {
		xorEvaluator = new XorEvaluator();
//		final GendreauEvaluatorFactory seh = GendreauEvaluatorFactory.newInstance();
		gendreauEvaluator = new GendreauEvaluator(
				GendreauScenario.load("data", "_240_24"),
				SolutionType.MYOPIC,
				GendreauEvaluator.ComputationStrategy.DISTRIBUTED);
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
//		final List<CFitnessInfo> fitnessInfos = simEvaluator
//				.evaluatePopulation(populationInfo);
		return gendreauEvaluator.evaluatePopulation(populationInfo);
		// return simEvaluator.parallelEvaluatePopulation(populationInfo);
	}
}