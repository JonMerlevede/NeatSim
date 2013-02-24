package neatsim.evaluators;

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.javatuples.Pair;

import rinde.evo4mas.common.ExperimentUtil;

public class SimulationEvaluatorHelper {
	// Taken from rinde
	private List<String> removeDirPrefix(final List<String> files) {
		final List<String> names = newArrayList();
		for (final String f : files) {
			names.add(f.substring(f.lastIndexOf('/') + 1));
		}
		return names;
	}

	public Pair<List<String>,List<String>> readScenariosFromDirectory(
			final String path,
			final String suffix) throws IOException {
		// getFilesFromDir gives us a list where .get() is O(1)
		final List<String> tnames = ExperimentUtil.getFilesFromDir(path, suffix);
		final List<String> names = Collections.unmodifiableList(removeDirPrefix(tnames));

		final List<String> tcontents = new ArrayList<>();
		for (final String name : tnames) {
			tcontents.add(ExperimentUtil.textFileToString(name));
		}

		final List<String> contents = Collections.unmodifiableList(tcontents);
		return new Pair<List<String>,List<String>>(names, contents);
	}
}
