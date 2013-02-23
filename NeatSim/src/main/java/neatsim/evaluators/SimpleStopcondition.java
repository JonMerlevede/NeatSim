package neatsim.evaluators;

public class SimpleStopcondition extends Stopcondition {

	@Override
	boolean isSatistified(final int generation, final double fitness) {
		return false;
	}

}
