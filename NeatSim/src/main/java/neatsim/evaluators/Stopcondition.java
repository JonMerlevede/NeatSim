package neatsim.evaluators;

public abstract class Stopcondition {
	abstract boolean isSatistified(int generation, double fitness);
}
