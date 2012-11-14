package neatsim.core;

import java.util.List;

public class FunctionLibrary {
	public static final FunctionLibrary INSTANCE = new FunctionLibrary();
	
	private FunctionLibrary() {
		
	}
	
	private double steepenedSigmoid(double x) {
		return 1.0/(1.0 + Math.exp(-4.9*x));
	}
	
	public double evaluate(String functionName, double inputValue, List<Double> aux) {
		switch (functionName) {
			case "SteepenedSigmoid":
				return steepenedSigmoid(inputValue);
			default:
				throw new UnsupportedOperationException("Not implemented yet");
		}
	}
}
