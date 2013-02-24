package neatsim.evaluators;

public abstract class Stopcondition {
	/*
	 * Generation is a nonnegative number (>=0).
	 */
	abstract boolean isSatistified(int generation, double fitness);
}
