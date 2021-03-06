package neatsim.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import neatsim.core.blackbox.neural.NeuralNetwork;
import neatsim.server.thrift.CConnection;
import neatsim.server.thrift.CFastCyclicNetwork;

public class NeuralNetworkFactory {
	// See "BlackBoxHeuristic"
	public static final int ID_WAITERS = 1;
	public static final int ID_CARGO_SIZE = 2;
	public static final int ID_IS_IN_CARGO = 3;
	public static final int ID_TIME_UNTIL_AVAILABLE = 4;
	public static final int ID_ADO = 5;
	public static final int ID_MIDO = 6;
	public static final int ID_MADO = 7;
	public static final int ID_DIST = 8;
	public static final int ID_URGE = 9;
	public static final int ID_EST = 10;
	public static final int ID_TTL = 11;
	public static final int ID_ADC = 12;
	public static final int ID_MIDC = 13;
	public static final int ID_MADC = 14;
	public static final int NUMBER_OF_INPUTS = 14;
	public static final int NUMBER_OF_INPUTS_INCLUDING_BIAS_NODE = NUMBER_OF_INPUTS + 1;
	public static final int ID_OUTPUT = NUMBER_OF_INPUTS_INCLUDING_BIAS_NODE;


	public NeuralNetworkFactory() {

	}

	private NeuralNetwork createNetwork(final List<CConnection> connections, final int numberOfHiddenNodes) {
		final List<String> activationFunctions = new ArrayList<>();
		final String steepenedSigmoid = "Identity";
		for (int i = 0; i < NUMBER_OF_INPUTS_INCLUDING_BIAS_NODE + 1 + numberOfHiddenNodes; i++)
			activationFunctions.add(steepenedSigmoid);

		final List<List<Double>> auxiliaryArguments = new ArrayList<>();
		for (int i = 0; i < NUMBER_OF_INPUTS_INCLUDING_BIAS_NODE + 1 + numberOfHiddenNodes; i++)
			auxiliaryArguments.add(new ArrayList<Double>());

		final CFastCyclicNetwork cfcn = new CFastCyclicNetwork(
				connections, // connections in the neural net
				activationFunctions, // list of activation functions
				auxiliaryArguments, // list of auxiliary arguments
				NUMBER_OF_INPUTS_INCLUDING_BIAS_NODE + 1 + numberOfHiddenNodes, // total neuron count; this includes the bias node
				NUMBER_OF_INPUTS, // number of inputs
				1, // number of outputs
				3 //timesteps per activation
				);
		return new NeuralNetwork(cfcn);
	}

	private int idOfHiddenNode(final int nr) {
		// Start numbering at 0, bias node + number of inputs + output node + nr
		return NUMBER_OF_INPUTS + 2 + nr;
	}

	public NeuralNetwork createDist() {
		final List<CConnection> connections = Arrays.asList(
				new CConnection(ID_DIST, ID_OUTPUT, 1),
				new CConnection(ID_TIME_UNTIL_AVAILABLE, ID_OUTPUT, 100000)
				);
		//System.out.println(connections);
		return createNetwork(connections, 0);
	}

	public NeuralNetwork createDist2() {
		final List<CConnection> connections = Arrays.asList(
				new CConnection(ID_DIST, idOfHiddenNode(0), 1),
				new CConnection(idOfHiddenNode(0), ID_OUTPUT, 1),
				new CConnection(ID_TIME_UNTIL_AVAILABLE, ID_OUTPUT, 100000)
				);
		return createNetwork(connections, 1);
	}

	public NeuralNetwork createClosest() {
		final List<CConnection> connections = Arrays.asList(
				new CConnection(ID_DIST, idOfHiddenNode(0), 1),
				new CConnection(ID_MIDC, idOfHiddenNode(0), -1),
				new CConnection(idOfHiddenNode(0), ID_OUTPUT, 100000),
				new CConnection(ID_TIME_UNTIL_AVAILABLE, ID_OUTPUT, Double.MAX_VALUE),
				new CConnection(ID_DIST, ID_OUTPUT, 1)
				);
		return createNetwork(connections, 1);
	}
}
