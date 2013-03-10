package neatsim.core.evaluators;

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import neatsim.util.AssertionHelper;

import org.javatuples.Pair;

import rinde.evo4mas.common.ExperimentUtil;

public class GendreauScenario {
	private final String name;
	private final String scenario;

	public String getNames() {
		return name;
	}
	public String getScenarios() {
		return scenario;
	}

	public GendreauScenario(
			final String name,
			final String scenario) {
		this.name = name;
		this.scenario = scenario;
	}

	public static List<GendreauScenario> load(
			String dataDirectory,
			final String fileSuffix) {
		assert dataDirectory != null;
		assert fileSuffix != null;

		Pair<List<String>,List<String>> tp = null;
		if (!dataDirectory.endsWith("/"))
			dataDirectory = dataDirectory + "/";
		try {
			tp = readScenariosFromDirectory(dataDirectory, fileSuffix);
		} catch (final IOException e) { e.printStackTrace(); }

		final List<String> scenarioNames = tp.getValue0();
		final List<String> scenarios = tp.getValue1();

		assert scenarioNames.size() > 0;
		assert scenarioNames.size() == scenarios.size();
		return create(scenarioNames, scenarios);
	}

	public static List<GendreauScenario> create(
			final List<String> scenarioNames,
			final List<String> scenarios) {
		assert AssertionHelper.isEffectiveCollection(scenarioNames);
		assert AssertionHelper.isEffectiveCollection(scenarios);
		assert scenarioNames.size() > 0;
		assert scenarioNames.size() == scenarios.size();

		final List<GendreauScenario> gendreauScenarios = new ArrayList<>(scenarios.size());
		final Iterator<String> namesIterator = scenarioNames.iterator();
		final Iterator<String> scenarioIterator = scenarios.iterator();
		while (namesIterator.hasNext()) {
			gendreauScenarios.add(new GendreauScenario(
					namesIterator.next(),
					scenarioIterator.next()));
		}
		return gendreauScenarios;
	}

	// Taken from rinde
	private static List<String> removeDirPrefix(final List<String> files) {
		final List<String> names = newArrayList();
		for (final String f : files) {
			names.add(f.substring(f.lastIndexOf('/') + 1));
		}
		return names;
	}

	private static Pair<List<String>,List<String>> readScenariosFromDirectory(
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
