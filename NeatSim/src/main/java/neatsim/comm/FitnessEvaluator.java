package neatsim.comm;
import neatsim.comm.thrift.CFastCyclicNetwork;
import neatsim.comm.thrift.CFitnessEvaluatorService;
import neatsim.comm.thrift.CFitnessInfo;
import neatsim.comm.thrift.CPopulationFitness;
import neatsim.comm.thrift.CPopulationInfo;
import neatsim.experiments.sim.SimEvaluator;
import neatsim.experiments.xor.XorEvaluator;

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
	protected final SimEvaluator simEvaluator;
	
	/**
	 * Creates a new fitness evaluator.
	 */
	public FitnessEvaluator() {
		xorEvaluator = new XorEvaluator();
		simEvaluator = new SimEvaluator();
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