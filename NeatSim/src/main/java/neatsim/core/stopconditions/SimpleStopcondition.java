package neatsim.core.stopconditions;

public class SimpleStopcondition extends Stopcondition {

	@Override
	public boolean isSatistified(final int generation, final double fitness) {
		return false;
	}

}
