namespace csharp NeatSim.Thrift
namespace java neatsim.comm.thrift

struct CConnection {
	10: required i32 fromNeuronId
	20: required i32 toNeuronId
	30: required double weight
}

struct CFastCyclicNetwork {
	10: required list<CConnection> connections
	20: required list<string> activationFunctions
	30: required list<list<double>> neuronAuxArgs
	40: required i32 neuronCount;
	50: required i32 inputNeuronCount
	60: required i32 outputNeuronCount
	70: required i32 timestepsPerActivation
}

struct CAuxFitnessInfo {
	10: required string name
	20: required double value
}

struct CFitnessInfo {
	10: required list<CAuxFitnessInfo> auxFitness
	20: required double fitness
	30: required bool stopConditionSatisfied = false;
}

struct CPopulationInfo {
	10: required list<CFastCyclicNetwork> phenomes
}

struct CPopulationFitness {
	10: list<CFitnessInfo> fitnessInfos
	20: i32 evaluationCount
}

service CFitnessEvaluatorService {
	CPopulationFitness calculateXorPopulationFitness (
		1:CPopulationInfo populationInfo
	)
	CFitnessInfo calculateXorPhenotypeFitness (
		1:CFastCyclicNetwork	ann
	)
}