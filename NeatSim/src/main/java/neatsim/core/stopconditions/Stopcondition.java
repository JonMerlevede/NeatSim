package neatsim.core.stopconditions;

public interface Stopcondition {
	/*
	 * Generation is a nonnegative number (>=0).
	 */
	public boolean isSatistified(int generation, double fitness);
}
