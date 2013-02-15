import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import neatsim.comm.thrift.CConnection;
import neatsim.comm.thrift.CFastCyclicNetwork;
import neatsim.core.BlackBoxHeuristic;
import neatsim.core.FastCyclicNeuralNetwork;
import neatsim.experiments.sim.GendreauHeuristicProblem;

import org.junit.Before;
import org.junit.Test;

import rinde.sim.problem.gendreau06.Gendreau06Parser;
import rinde.sim.problem.gendreau06.Gendreau06Scenario;


public class TestGendreauHeuristicProblem {
	public static final String SCENARIO_NAME = "data/req_rapide_1_240_24";
	public static final int NUMBER_OF_VEHICLES = 10;
	public static final String FILE_NAME = new File(SCENARIO_NAME).getName();

	FastCyclicNeuralNetwork fcnn;
	Gendreau06Scenario scenario;
	
	
	@Before
	public void setUp() {
		fcnn = makeNeuralNetwork();

		try {
			BufferedReader bfr = new BufferedReader(new FileReader(SCENARIO_NAME));
			scenario = Gendreau06Parser.parse(bfr,FILE_NAME, NUMBER_OF_VEHICLES);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates a hard-coded fast cyclic neural network that contains no hidden
	 * nodes and connects only the distance node and the output node with a
	 * connection that has weight one.
	 * 
	 * @return The created fast cyclic neural network.
	 */
	public FastCyclicNeuralNetwork makeNeuralNetwork() {
		int identifierOfDistanceNode = 8;
		int numberOfInputs = 14;
		int identifierOfOutputNode = numberOfInputs + 1;
		
		List<CConnection> connections = Arrays.asList(
				new CConnection(identifierOfDistanceNode, identifierOfOutputNode, 1)
				);
		
		List<String> activationFunctions = new ArrayList<>();
		String steepenedSigmoid = "SteepenedSigmoid";
		for (int i = 0; i < numberOfInputs + 1; i++)
			activationFunctions.add(steepenedSigmoid);
		
		List<List<Double>> auxiliaryArguments = new ArrayList<>();
		for (int i = 0; i < numberOfInputs; i++)
			auxiliaryArguments.add(new ArrayList<Double>());
		
		CFastCyclicNetwork cfcn = new CFastCyclicNetwork(
				connections, // connections in the neural net
				activationFunctions, // list of activation functions
				auxiliaryArguments, // list of auxiliary arguments
				numberOfInputs + 1, // total neuron count
				numberOfInputs, // number of inputs
				1, // number of outputs
				3 //timesteps per activation
				);
		
		return new FastCyclicNeuralNetwork(cfcn);
	}
	
	/**
	 * Test case for {@link GendreauHeuristicProblem#simulate()}.
	 */
	@Test
	public void testSimulate() {
		GendreauHeuristicProblem ghp = GendreauHeuristicProblem.create(scenario, fcnn);
		ghp.simulate(); // TODO decide what to test for :P
	}
}
