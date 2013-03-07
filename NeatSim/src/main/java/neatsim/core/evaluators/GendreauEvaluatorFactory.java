package neatsim.core.evaluators;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import neatsim.core.stopconditions.SimpleStopcondition;
import neatsim.core.stopconditions.Stopcondition;

import org.javatuples.Pair;

import rinde.evo4mas.common.ExperimentUtil;
import rinde.evo4mas.gendreau06.GSimulationTask.SolutionType;

/**
 *
 * @author Jonathan Merlevede
 *
 */
public class GendreauEvaluatorFactory {

	public GendreauEvaluator create(
			final String dataDirectory,
			final String fileSuffix,
			final SolutionType solutionType,
			final GendreauEvaluator.ComputationStrategy computationStrategy) {
		return create(
				dataDirectory,
				fileSuffix,
				solutionType,
				computationStrategy,
				new SimpleStopcondition());
	}

	public GendreauEvaluator create(
			String dataDirectory,
			final String fileSuffix,
			final SolutionType solutionType,
			final GendreauEvaluator.ComputationStrategy computationStrategy,
			final Stopcondition stopcondition) {
		assert dataDirectory != null;
		assert fileSuffix != null;
		assert solutionType != null;
		assert computationStrategy != null;
		assert stopcondition != null;
		assert new File(dataDirectory).exists();

		Pair<List<String>,List<String>> tp = null;
		if (!dataDirectory.endsWith("/"))
			dataDirectory = dataDirectory + "/";
		try {
			tp = readScenariosFromDirectory(dataDirectory, fileSuffix);
		} catch (final IOException e) { e.printStackTrace(); }
		final List<String> scenarioNames = tp.getValue0();
		final List<String> scenarioContents = tp.getValue1();

		final GendreauEvaluator simEvaluator = new GendreauEvaluator(
				scenarioNames,
				scenarioContents,
				1, // number of scenarios per generation
				solutionType,
				computationStrategy,
				stopcondition);

		return simEvaluator;
	}

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
