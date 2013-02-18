import neatsim.core.FastCyclicNeuralNetwork;
import neatsim.sim.neuralnets.NeuralNetworkFactory;


public class Test {
	public static void main(String[] args) {
		NeuralNetworkFactory nnf = new NeuralNetworkFactory();
		FastCyclicNeuralNetwork fcnn = nnf.createDist();
		fcnn.setInput(7, 10);
		fcnn.activate();
		System.out.println(fcnn.getOutput(0));
	}
}
