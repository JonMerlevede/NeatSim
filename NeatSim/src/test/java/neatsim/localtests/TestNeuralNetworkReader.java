package neatsim.localtests;

import static junit.framework.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import neatsim.core.Function;
import neatsim.core.NeuralNetwork;
import neatsim.util.NeuralNetworkReader;

import org.junit.Test;

public class TestNeuralNetworkReader {
	public static final double EPS = 10e-3;
	//private final FunctionLibrary library = FunctionLibrary.getInstance();
	private final NeuralNetworkReader reader = new NeuralNetworkReader(3);

	@Test
	public void testSimpleNetwork() throws IOException {
		final URL testNetwork = getClass().getResource("/simpleTestNetwork.xml");
		final List<NeuralNetwork> networks = reader.readFile(testNetwork);
		assertEquals(1, networks.size());
		assertSimpleNetwork(networks.get(0));
	}

	private void assertSimpleNetwork(final NeuralNetwork network) {
		assertEquals(14,network.getNumberOfInputs());
		assertEquals(1,network.getNumberOfOutputs());
		// Input 1
		network.setInput(0, 1);
		network.activate();
		assertEquals(s(0.52701024249111594), network.getOutput(0), EPS);
		// Input 2
		network.reset();
		network.setInput(1, 1);
		network.activate();
		assertEquals(s(0), network.getOutput(0), EPS);
		// Input 3
		network.reset();
		network.setInput(2, 1);
		network.activate();
		assertEquals(s(-0.67608981306330662), network.getOutput(0), EPS);
		// Input 4
		network.reset();
		network.setInput(3, 1);
		network.activate();
		assertEquals(s(-0.24665289383329442), network.getOutput(0), EPS);
		// Input 5
		network.reset();
		network.setInput(4, 1);
		network.activate();
		assertEquals(s(0), network.getOutput(0), EPS);
	}

	@Test
	public void testHardNetwork() throws IOException {
		final URL testNetwork = getClass().getResource("/hardTestNetwork.xml");
		final List<NeuralNetwork> networks = reader.readFile(testNetwork);
		assertEquals(1, networks.size());
		assertHardNetwork(networks.get(0));
	}

	private void assertHardNetwork(final NeuralNetwork network) {
		assertEquals(14,network.getNumberOfInputs());
		assertEquals(1,network.getNumberOfOutputs());
		// Input 1
		network.reset();
		network.setInput(0, 1);
		network.activate();
		assertEquals(s(4.632470804467788 + s(3.7083442555740476)), network.getOutput(0), EPS);
		// Input 2
		network.reset();
		network.activate();
		assertEquals(s(4.632470804467788), network.getOutput(0), EPS);
	}

	@Test
	public void testMultipleNetworks() throws IOException {
		final URL testNetwork = getClass().getResource("/multipleTestNetwork.xml");
		final List<NeuralNetwork> networks = reader.readFile(testNetwork);
		assertEquals(2, networks.size());
		assertSimpleNetwork(networks.get(0));
		assertHardNetwork(networks.get(1));
	}

	private double s(final double input) {
		return Function.STEEPENED_SIGMOID.calculate(input);//library.evaluate("SteepenedSigmoid", input);
	}
}
