package neatsim.core.stopconditions;

public class GenerationsStopCondition implements Stopcondition {
	protected int nGenerations;

	public GenerationsStopCondition(final int nGenerations) {
		this.nGenerations = nGenerations;
	}

	@Override
	public boolean isSatistified(final int generation, final double fitness) {
		return generation >= nGenerations;
	}


}
