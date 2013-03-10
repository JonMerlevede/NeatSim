package neatsim.core.evaluators;

import neatsim.server.thrift.CPopulationFitness;
import neatsim.server.thrift.CPopulationInfo;

public interface PopulationEvaluator {
	public CPopulationFitness evaluatePopulation(final CPopulationInfo populationInfo);
}
