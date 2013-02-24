package neatsim.evaluators;

import java.util.Collection;

public class AssertionHelper {
	public static <E> boolean isEffectiveCollection(final Collection<E> coll) {
		if (coll == null)
			return false;
		for (final E elem : coll)
			if (elem == null) { return false; }
		return true;
	}
}
