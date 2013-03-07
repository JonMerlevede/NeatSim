package neatsim.core.stopconditions;

public abstract class Stopcondition {
	/*
	 * Generation is a nonnegative number (>=0).
	 */
	public abstract boolean isSatistified(int generation, double fitness);
}
