package neatsim.core;

import java.util.List;

/**
 * The instance of this singleton class represents a function library.
 * 
 * 
 * @author Jonathan Merlevede
 *
 */
public class FunctionLibrary {
	/**
	 * A handle to the unique instance of this class.
	 */
	public static final FunctionLibrary INSTANCE = new FunctionLibrary();
	
	/*
	 * Private constructor: singleton class!
	 */
	private FunctionLibrary() {
		
	}
	
	private double steepenedSigmoid(double x) {
		return 1.0/(1.0 + Math.exp(-4.9*x));
	}
	
	/**
	 * Returns whether the given function name exists in this function library.
	 * 
	 * @param functionName The given function name.
	 * @return True if the given function name exists in this function library
	 *         and false otherwise.
	 */
	public boolean exists(String functionName) {
		return functionName.equals("SteepenedSigmoid") || functionName.equals("Identity");
	}
	
	/**
	 * Evaluates the function of the given name using the given input value and
	 * the given auxiliary arguments.
	 * 
	 * @param functionName The given function name.
	 * @param inputValue The input value.
	 * @param aux The auxiliary arguments.
	 * @pre The given function name exists in the function library.
	 * 	| exists(functionName)
	 * @return The value returned by the function of the given name using the
	 *         given input value and the given auxiliary arguments.
	 */
	public double evaluate(String functionName, double inputValue, List<Double> aux) {
		assert exists(functionName);
		switch (functionName) {
			case "SteepenedSigmoid":
				return steepenedSigmoid(inputValue);
			case "Identity":
				return inputValue;
			default:
			// Note that source code (exists) should be kept up to date so that
			// this default action is NEVER actually reached.
				throw new UnsupportedOperationException("Not implemented yet");
		}
	}
}
