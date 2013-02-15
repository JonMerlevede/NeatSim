package neatsim.evaluators;
import neatsim.thrift.CFastCyclicNetwork;
import neatsim.thrift.CFitnessEvaluatorService;
import neatsim.thrift.CFitnessInfo;
import neatsim.thrift.CPopulationFitness;
import neatsim.thrift.CPopulationInfo;

import org.apache.thrift.TException;

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
	protected final LocalSimulationEvaluator simEvaluator;
	
	/**
	 * Creates a new fitness evaluator.
	 */
	public FitnessEvaluator() {
		xorEvaluator = new XorEvaluator();
		simEvaluator = new LocalSimulationEvaluator();
	}
	
	@Override
	public CFitnessInfo calculateXorPhenotypeFitness(CFastCyclicNetwork ann)
			throws TException { 
		return xorEvaluator.evaluatePhenotype(ann);
	}

	@Override
	public CPopulationFitness calculateXorPopulationFitness(
			CPopulationInfo populationInfo) throws TException {
		return xorEvaluator.evaluatePopulation(populationInfo);
	}
	
	@Override
	public CPopulationFitness calculateSimPopulationFitness(
			CPopulationInfo populationInfo) throws TException {
		return simEvaluator.evaluatePopulation(populationInfo);
//		return simEvaluator.parallelEvaluatePopulation(populationInfo);
	}
}