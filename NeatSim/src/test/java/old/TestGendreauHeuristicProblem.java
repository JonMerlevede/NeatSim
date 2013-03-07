package old;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import neatsim.core.BlackBoxHeuristic;
import neatsim.core.FastCyclicNeuralNetwork;
import neatsim.sim.GendreauHeuristicProblem;
import neatsim.sim.neuralnets.NeuralNetworkFactory;

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
			final BufferedReader bfr = new BufferedReader(new FileReader(SCENARIO_NAME));
			scenario = Gendreau06Parser.parse(bfr,FILE_NAME, NUMBER_OF_VEHICLES);
		} catch (final IOException e) {
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
		return (new NeuralNetworkFactory()).createDist();
	}

	/**
	 * Test case for {@link GendreauHeuristicProblem#simulate()}.
	 */
	@Test
	public void testSimulate() {
		final BlackBoxHeuristic bbh = new BlackBoxHeuristic(fcnn);
		final GendreauHeuristicProblem ghp = GendreauHeuristicProblem.create(scenario, bbh, false);
		ghp.simulate(); // TODO decide what to test for ; currently we just see if there occurs an exception
		System.out.println(ghp.getStatistics());
	}
}
