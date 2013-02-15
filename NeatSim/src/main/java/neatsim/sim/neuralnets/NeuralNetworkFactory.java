package neatsim.sim.neuralnets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import neatsim.core.FastCyclicNeuralNetwork;
import neatsim.thrift.CConnection;
import neatsim.thrift.CFastCyclicNetwork;

public class NeuralNetworkFactory {
	public NeuralNetworkFactory() {
		
	}
	public FastCyclicNeuralNetwork createDist() {
		int identifierOfDistanceNode = 8;
		int numberOfInputs = 14;
		int numberOfInputsIncludingBiasNode = numberOfInputs + 1;
		int identifierOfOutputNode = numberOfInputs + 1;
		
		List<CConnection> connections = Arrays.asList(
				new CConnection(identifierOfDistanceNode, identifierOfOutputNode, 1)
				);
		
		List<String> activationFunctions = new ArrayList<>();
		String steepenedSigmoid = "SteepenedSigmoid";
		for (int i = 0; i < numberOfInputsIncludingBiasNode + 1; i++)
			activationFunctions.add(steepenedSigmoid);
		
		List<List<Double>> auxiliaryArguments = new ArrayList<>();
		for (int i = 0; i < numberOfInputsIncludingBiasNode + 1; i++)
			auxiliaryArguments.add(new ArrayList<Double>());
		
		CFastCyclicNetwork cfcn = new CFastCyclicNetwork(
				connections, // connections in the neural net
				activationFunctions, // list of activation functions
				auxiliaryArguments, // list of auxiliary arguments
				numberOfInputsIncludingBiasNode + 1, // total neuron count; this includes the bias node
				numberOfInputs, // number of inputs
				1, // number of outputs
				3 //timesteps per activation
				);
		
		return new FastCyclicNeuralNetwork(cfcn);
	}
}
