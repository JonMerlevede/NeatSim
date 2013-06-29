package neatsim.localtests;

import static junit.framework.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import neatsim.core.blackbox.neural.NeuralNetwork;
import neatsim.server.thrift.CConnection;
import neatsim.server.thrift.CFastCyclicNetwork;
import neatsim.util.NeuralNetworkReader;

import org.junit.Test;

public class TestXor {
	private final NeuralNetworkReader reader = new NeuralNetworkReader(3);

	/*
	 * Evolved using my implementation in SharpNEAT
	 */
	@Test
	public void evolvedXor1() throws IOException {
		final URL testNetwork = getClass().getResource("/xorSolution1.gnm.xml");
		final List<NeuralNetwork> networks = reader.readFile(testNetwork).neuralNetworks;
		assertEquals(1, networks.size());

		final NeuralNetwork nn = networks.get(0);
		testNeuralNetworkImprecise(nn);
	}

	/*
	 * Evolved using SharpNEAT implementation
	 */
	@Test
	public void evolvedXor2() throws IOException {
		final URL testNetwork = getClass().getResource("/xorSolution2.gnm.xml");
		final List<NeuralNetwork> networks = reader.readFile(testNetwork).neuralNetworks;
		assertEquals(1, networks.size());

		final NeuralNetwork nn = networks.get(0);
		testNeuralNetworkImprecise(nn);
	}

	/*
	 * Evolved using SharpNEAT implementation
	 */
	@Test
	public void evolvedXor3() throws IOException {
		final URL testNetwork = getClass().getResource("/xorSolution3.gnm.xml");
		final List<NeuralNetwork> networks = reader.readFile(testNetwork).neuralNetworks;
		assertEquals(1, networks.size());

		final NeuralNetwork nn = networks.get(0);
		nn.setInput(0, 0);
		nn.setInput(0, 0);
		nn.activate();
		assertEquals(true, nn.getOutput(0) <= 0.5);
		nn.reset();

		nn.setInput(0, 1);
		nn.setInput(1, 0);
		nn.activate();
		assertEquals(true, nn.getOutput(0) > 0.5);
		nn.reset();

		nn.setInput(0, 1);
		nn.setInput(1, 1);
		nn.activate();
		assertEquals(true, nn.getOutput(0) <= 0.5);
		nn.reset();

		nn.setInput(0, 0);
		nn.setInput(1, 1);
		nn.activate();
		assertEquals(true, nn.getOutput(0) > 0.5);
		nn.reset();
	}


	public static void testNeuralNetworkImprecise(final NeuralNetwork nn) {
		nn.resetAll();

		nn.activate();
		assertEquals(true, nn.getOutput(0) <= 0.5);
		nn.reset();

		nn.setInput(0, 1);
		nn.activate();
		assertEquals(true, nn.getOutput(0) > 0.5);
		nn.reset();

		nn.setInput(1, 1);
		nn.activate();
		assertEquals(true, nn.getOutput(0) <= 0.5);
		nn.reset();

		nn.setInput(0, 0);
		nn.setInput(1, 1);
		nn.activate();
		assertEquals(true, nn.getOutput(0) > 0.5);
		nn.resetAll();
	}

	@Test
	public void workingXor() {
		final List<List<Double>> auxiliaryArguments = new ArrayList<>();
		for (int i = 0; i < 7; i++)
			auxiliaryArguments.add(new ArrayList<Double>());

		final CFastCyclicNetwork cfcn = new CFastCyclicNetwork(
				Arrays.asList(
						new CConnection(1, 4, 1),
						new CConnection(1, 5, -1),
						new CConnection(2, 4, -1),
						new CConnection(2, 5, 1),
						new CConnection(4, 3, 1),
						new CConnection(5, 3, 1)),
				Arrays.asList(
						"Error",
						"Error",
						"Error",
						"ThreshOne",
						"ThreshOne",
						"ThreshOne",
						"ThreshOne"),
				Arrays.asList(
						(List<Double>)(new ArrayList<Double>()),
						(List<Double>)(new ArrayList<Double>()),
						(List<Double>)(new ArrayList<Double>()),
						(List<Double>)(new ArrayList<Double>()),
						(List<Double>)(new ArrayList<Double>()),
						(List<Double>)(new ArrayList<Double>()),
						(List<Double>)(new ArrayList<Double>())),
				7,
				2,
				1,
				3);
		final NeuralNetwork nn = new NeuralNetwork(cfcn);
		nn.activate();
		assertEquals(0d, nn.getOutput(0));
		nn.reset();

		nn.setInput(0, 1);
		nn.activate();
		assertEquals(1d, nn.getOutput(0));
		nn.reset();

		nn.setInput(1, 1);
		nn.activate();
		assertEquals(0d, nn.getOutput(0));
		nn.reset();

		nn.setInput(0, 0);
		nn.setInput(1, 1);
		nn.activate();
		assertEquals(1d, nn.getOutput(0));
		nn.resetAll();
	}
}
