package neatsim.comm;
import neatsim.comm.thrift.CFastCyclicNetwork;
import neatsim.comm.thrift.CFitnessEvaluatorService;
import neatsim.comm.thrift.CFitnessInfo;
import neatsim.comm.thrift.CPopulationFitness;
import neatsim.comm.thrift.CPopulationInfo;
import neatsim.experiments.xor.XorEvaluator;

import org.apache.thrift.TException;

public class FitnessEvaluatorServiceImplementation implements CFitnessEvaluatorService.Iface {
	public final XorEvaluator xorEvaluator;
	
	public FitnessEvaluatorServiceImplementation() {
		xorEvaluator = new XorEvaluator();
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
}