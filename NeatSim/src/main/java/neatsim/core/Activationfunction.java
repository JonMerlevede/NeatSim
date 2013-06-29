package neatsim.core;

import java.util.List;

/**
 * Enumeration of different activation functions of nodes in a neural network.
 *
 * @author Jonathan Merlevede
 */
public enum Activationfunction {
	/**
	 * Steepened sigmoid function.
	 * Requires no auxiliary arguments.
	 */
	STEEPENED_SIGMOID(new FunctionImplementation() {
		@Override
		public double calculate(final double arg, final List<Double> auxArgs) {
			return 1.0/(1.0 + Math.exp(-4.9*arg));
		}
	}, false),
	/**
	 * Identity function.
	 * Requires no auxiliary arguments.
	 */
	IDENTITY(new FunctionImplementation() {
		@Override
		public double calculate(final double arg, final List<Double> auxArgs) {
			return arg;
		}
	}, false),
	THRESH_ONE(new FunctionImplementation() {
		@Override
		public double calculate(final double arg, final List<Double> auxArgs) {
			return arg >= 1 ? 1 : 0;
		}
	}, false),
	ERROR(new FunctionImplementation() {
		@Override
		public double calculate(final double arg, final List<Double> auxArgs) {
			throw new IllegalStateException("A node with this activation function should never be activated.");
		}
	}, false);

	private FunctionImplementation implementation;
	private boolean requiresAuxiliaryArguments;

	private interface FunctionImplementation {
		public double calculate(final double arg, final List<Double> auxArgs);
	}

	private Activationfunction(final FunctionImplementation implementation,
			final boolean requiresAuxiliaryArguments) {
		this.implementation = implementation;
		this.requiresAuxiliaryArguments = requiresAuxiliaryArguments;
	}

	public double calculate(final double arg, final List<Double> auxArgs) {
		return implementation.calculate(arg, auxArgs);
	}

	public double calculate(final double arg) {
		if (requiresAuxiliaryArguments)
			throw new IllegalAccessError("This function requires auxiliary arguments");
		return implementation.calculate(arg, null);
	}

	public static Activationfunction fromString(final String functionName) {
		switch(functionName) {
		case("SteepenedSigmoid"): return STEEPENED_SIGMOID;
		case("Identity"): return IDENTITY;
		case("ThreshOne"): return THRESH_ONE;
		case("Error"): return ERROR;
		}
		assert false; throw new RuntimeException();
	}
}
