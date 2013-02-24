import static junit.framework.Assert.assertEquals;
import neatsim.core.FastCyclicNeuralNetwork;
import neatsim.sim.neuralnets.NeuralNetworkFactory;

import org.junit.Before;
import org.junit.Test;


public class TestNeuralNetworks {
	private final NeuralNetworkFactory factory;
	private final FastCyclicNeuralNetwork dist;
	private final FastCyclicNeuralNetwork dist2;

	public TestNeuralNetworks() {
		factory = new NeuralNetworkFactory();
		dist = factory.createDist();
		dist2 = factory.createDist2();
	}

	@Before
	public void setUp() {

	}

	@Test
	public void testSetInputReset() {
		dist.reset();
		dist.setInput(0, 10d);
		assertEquals(10d,dist.getInput(0));
		dist.setInput(1, 10d);
		assertEquals(10d, dist.getInput(0));
		assertEquals(10d, dist.getInput(1));
		dist.setInput(2, 5d);
		assertEquals(10d, dist.getInput(0));
		assertEquals(10d, dist.getInput(1));
		assertEquals(5d, dist.getInput(2));
		dist.reset();
		assertEquals(0d, dist.getInput(0));
		assertEquals(0d, dist.getInput(1));
		assertEquals(0d, dist.getInput(2));
	}

	@Test
	public void testDistance() {
		dist.reset();
		assertEquals(0d, dist.getOutput(0));
		dist.setInput(7, 10);
		assertEquals(10d, dist.getInput(7));
		assertEquals(0d, dist.getOutput(0));
		dist.activate();
		assertEquals(10d, dist.getInput(7));
		assertEquals(10d, dist.getOutput(0));
	}

	@Test
	public void testXor() {

	}

}
