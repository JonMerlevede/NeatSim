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
	private static final FunctionLibrary INSTANCE = new FunctionLibrary();

	public static FunctionLibrary getInstance() {
		return INSTANCE;
	}

	/*
	 * Private constructor: singleton class!
	 */
	private FunctionLibrary() {

	}

	private double steepenedSigmoid(final double x) {
		return 1.0/(1.0 + Math.exp(-4.9*x));
	}

	/**
	 * Returns whether the given function name exists in this function library.
	 *
	 * @param functionName The given function name.
	 * @return True if the given function name exists in this function library
	 *         and false otherwise.
	 */
	public boolean exists(final String functionName) {
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
	public double evaluate(final String functionName, final double inputValue, final List<Double> aux) {
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

	public double evaluate(final String functionName, final double inputValue) {
		return evaluate(functionName, inputValue, null);
	}
}