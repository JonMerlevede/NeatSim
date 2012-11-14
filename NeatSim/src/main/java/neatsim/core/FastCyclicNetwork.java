package neatsim.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import neatsim.comm.thrift.CConnection;
import neatsim.comm.thrift.CFastCyclicNetwork;

public class FastCyclicNetwork implements BlackBox {
	private static final FunctionLibrary FL = FunctionLibrary.INSTANCE;
	
	private final List<CConnection> connectionArray;
	private final List<String> neuronActivationFnArray;
	private final List<List<Double>>neuronAuxArgsArray;
	
//	private final List<Double> inputSignalArray;
//	private final List<Double> outputSignalArray;
	
	private final List<Double> preActivationArray;
	private final List<Double> postActivationArray;
	
	private final int inputNeuronCount;
	private final int outputNeuronCount;
	private final int inputAndBiasNeuronCount;
	private final int timestepsPerActivation;
	
	public FastCyclicNetwork(CFastCyclicNetwork cfcn) { 
		// The bias node is already present in the given cfcn network.
		
		connectionArray = cfcn.getConnections();
		neuronActivationFnArray = cfcn.getActivationFunctions();
		neuronAuxArgsArray = cfcn.getNeuronAuxArgs();
		
		preActivationArray = new ArrayList<Double>(Collections.nCopies(cfcn.getNeuronCount(), 0.0));
		postActivationArray = new ArrayList<Double>(Collections.nCopies(cfcn.getNeuronCount(), 0.0));
//		System.out.println("Teh neuron count: " + cfcn.getNeuronCount());
//		System.out.println("Teh sizze: " + preActivationArray.size());
//		System.out.println("Teh sizze2: " + postActivationArray.size());
		
		inputNeuronCount = cfcn.getInputNeuronCount();
		inputAndBiasNeuronCount = inputNeuronCount + 1;
		outputNeuronCount = cfcn.getOutputNeuronCount();
		timestepsPerActivation = cfcn.getTimestepsPerActivation();
		postActivationArray.set(0, 1.0);
	}
	
	
	public boolean isValidInputNumber(int no) {
		return !(no < 0 || no >= inputNeuronCount);
	}
	public double getInput(int no) {
		if (!isValidInputNumber(no))
			throw new IllegalArgumentException("Index out of bounds.");
		return postActivationArray.get(1+no);
	}
	public void setInput(int no, double val) {
		if (!isValidInputNumber(no))
			throw new IllegalArgumentException("Index out of bounds.");
		postActivationArray.set(1+no,val);
	}
	
	public boolean isValidOutputNumber(int no) {
		return !(no < 0 || no >= outputNeuronCount);
	}
	public double getOutput(int no) {
		if (!isValidOutputNumber(no))
			throw new IllegalArgumentException("Index out of bounds.");
		return postActivationArray.get(1+inputNeuronCount);
	}
	public void setOutput(int no, double val) {
		if (no < 0 || no >= outputNeuronCount)
			throw new IllegalArgumentException("Index out of bounds.");
		postActivationArray.set(1+inputNeuronCount, val);
	}
	
	public void activate() {
		for (int i = 0; i < timestepsPerActivation; i++) {
			// Loop connections
			for (CConnection con : connectionArray) {
				int to = con.getToNeuronId();
				int from = con.getFromNeuronId();
				double fromVal = postActivationArray.get(from);
				double toVal = postActivationArray.get(to);
				toVal += fromVal * con.getWeight();
				preActivationArray.set(to, toVal);
			}
			// Loop neurons
			for (int j = inputAndBiasNeuronCount; j < preActivationArray.size(); j++) {
				String functionName = neuronActivationFnArray.get(j); 
				double output = FL.evaluate(functionName, preActivationArray.get(j), neuronAuxArgsArray.get(j));
				postActivationArray.set(j, output);
				preActivationArray.set(j, 0.0); // TODO I do not see why this is necessary. Try removing.
			}
		}
	}
	
	public void reset() {
		for (int i = 0; i < preActivationArray.size(); i++) {
			preActivationArray.set(i, 0.0); // TODO I do not see why this is necessary. Try removing.
			postActivationArray.set(i, 0.0);
		}
		//preActivationArray.clear();
		//postActivationArray.clear();
	}
	
	public boolean isValid() {
		return true;
	}
}
