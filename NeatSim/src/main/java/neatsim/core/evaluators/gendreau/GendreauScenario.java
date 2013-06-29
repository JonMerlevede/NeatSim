package neatsim.core.evaluators.gendreau;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import neatsim.util.NaturalOrderComparator;
import neatsim.util.PrefixSuffixFilter;
import rinde.evo4mas.common.ExperimentUtil;

public class GendreauScenario implements Comparable<GendreauScenario> {
	private final String name;
	private final String scenario;

	public String getName() {
		return name;
	}
	public String getScenario() {
		return scenario;
	}

	public GendreauScenario(
			final String name,
			final String scenario) {
		this.name = name;
		this.scenario = scenario;
	}

	public static List<GendreauScenario> load(
			final String dataDirectory,
			final String fileSuffix) {
		return load(dataDirectory, "", fileSuffix);
	}

	public static List<GendreauScenario> load(
			String dataDirectory,
			final String filePrefix,
			final String fileSuffix) {
		assert dataDirectory != null;
		assert filePrefix != null;
		assert fileSuffix != null;

		if (!dataDirectory.endsWith(File.separator))
			dataDirectory = dataDirectory + File.separator;

		try {
			return readScenariosFromDirectory(dataDirectory, filePrefix, fileSuffix);
		} catch (final IOException e) { e.printStackTrace(); throw new RuntimeException(); }
	}

//	public static List<GendreauScenario> create(
//			final List<String> scenarioNames,
//			final List<String> scenarios) {
//		assert AssertionHelper.isEffectiveCollection(scenarioNames);
//		assert AssertionHelper.isEffectiveCollection(scenarios);
//		assert scenarioNames.size() > 0;
//		assert scenarioNames.size() == scenarios.size();
//
//		final List<GendreauScenario> gendreauScenarios = new ArrayList<>(scenarios.size());
//		final Iterator<String> namesIterator = scenarioNames.iterator();
//		final Iterator<String> scenarioIterator = scenarios.iterator();
//		while (namesIterator.hasNext()) {
//			gendreauScenarios.add(new GendreauScenario(
//					namesIterator.next(),
//					scenarioIterator.next()));
//		}
//		return gendreauScenarios;
//	}

	// Taken from rinde
//	private static List<String> removeDirPrefix(final List<String> files) {
//		final List<String> names = newArrayList();
//		for (final String f : files) {
//			names.add(f.substring(f.lastIndexOf('/') + 1));
//		}
//		return names;
//	}

//	private static Pair<List<String>,List<String>> readScenariosFromDirectory(
//			final String path,
//			final String suffix) throws IOException {
//		return readScenariosFromDirectory(path, "", suffix);
//	}



	private static List<GendreauScenario> readScenariosFromDirectory(
			final String path,
			final String prefix,
			final String suffix) throws IOException {
		final File folder = new File(path);
		final FilenameFilter filenameFilter = new PrefixSuffixFilter(prefix, suffix);
		final List<GendreauScenario> scenarios = new ArrayList<GendreauScenario>();
//		final List<String> names = new ArrayList<String>();
//		final List<String> contents = new ArrayList<String>();
		for (final File file : folder.listFiles(filenameFilter)) {
			scenarios.add(new GendreauScenario(
					file.getName(),
					ExperimentUtil.textFileToString(file.getAbsolutePath())));
		}

		Collections.sort(scenarios);

		// getFilesFromDir gives us a list where .get() is O(1)
//		final List<String> tnames = ExperimentUtil.getFilesFromDir(path, suffix);
//		final List<String> names = Collections.unmodifiableList(removeDirPrefix(tnames));
//
//		final List<String> tcontents = new ArrayList<>();
//		for (final String name : tnames) {
//			tcontents.add(ExperimentUtil.textFileToString(name));
//		}
//
//		final List<String> contents = Collections.unmodifiableList(tcontents);
		return scenarios;
//		return new Pair<List<String>,List<String>>(names, contents);
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof GendreauScenario))
			return false;
		final GendreauScenario gobj = (GendreauScenario) obj;
		return (name.equals(gobj.getName()) && scenario.equals(gobj.getScenario()));
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public int compareTo(final GendreauScenario o) {
		return NaturalOrderComparator.CASEINSENSITIVE_NUMERICAL_ORDER.compare(this.name, o.name);
		//return this.name.compareToIgnoreCase(o.name);
	}
}