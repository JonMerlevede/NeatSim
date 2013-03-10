package neatsim.core;

import java.util.List;

public enum Function {
	/**
	 * Stepened sigmoid function.
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
	}, false);

	private FunctionImplementation implementation;
	private boolean requiresAuxiliaryArguments;

	private interface FunctionImplementation {
		public double calculate(final double arg, final List<Double> auxArgs);
	}

	private Function(final FunctionImplementation implementation,
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

	public static Function fromString(final String functionName) {
		switch(functionName) {
		case("SteepenedSigmoid"): return STEEPENED_SIGMOID;
		case("Identity"): return IDENTITY;
		}
		assert false; throw new RuntimeException();
	}
}
