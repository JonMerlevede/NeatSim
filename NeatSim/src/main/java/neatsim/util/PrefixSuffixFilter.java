package neatsim.util;

import java.io.File;
import java.io.FilenameFilter;

public class PrefixSuffixFilter implements FilenameFilter {
	private final String prefix;
	private final String suffix;

	public PrefixSuffixFilter(final String prefix, final String suffix) {
		this.prefix = prefix;
		this.suffix = suffix;
	}

	@Override
	public boolean accept(final File dir, final String name) {
		if (!name.startsWith(prefix))
			return false;
		if (!name.endsWith(suffix))
			return false;
		return true;
	}

}
