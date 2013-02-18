import static junit.framework.Assert.assertEquals;
import junit.framework.Assert;
import neatsim.core.FastCyclicNeuralNetwork;
import neatsim.sim.neuralnets.NeuralNetworkFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runners.JUnit4;


public class TestNeuralNetworks {
	private NeuralNetworkFactory factory;
	private FastCyclicNeuralNetwork dist;
	private FastCyclicNeuralNetwork dist2;
	
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
		dist.setInput(0, 10);
		assertEquals(10,dist.getInput(0));
		dist.setInput(1, 10);
		assertEquals(10, dist.getInput(0));
		assertEquals(10, dist.getInput(1));
		dist.setInput(2, 5);
		assertEquals(10, dist.getInput(0));
		assertEquals(10, dist.getInput(1));
		assertEquals(5, dist.getInput(2));
		dist.reset();
		assertEquals(0, dist.getInput(0));
		assertEquals(0, dist.getInput(1));
		assertEquals(0, dist.getInput(2));
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
