package neatsim.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FilenameReader {
	public List<String> getFilePathsFromDirectory(final String path, final String filePrefix, final String fileSuffix) {
		assert path != null;
		assert filePrefix != null;
		assert fileSuffix != null;

		final File dir = new File(path);
		final ArrayList<String> paths = new ArrayList<>(dir.listFiles().length);
		for (final File file : dir.listFiles())
			paths.add(file.getAbsolutePath());
		Collections.sort(paths);
		return Collections.unmodifiableList(paths);
	}

	public List<String> getFileNamesFromDirectory(final String path, final String filePrefix, final String fileSuffix) {
		assert path != null;
		assert filePrefix != null;
		assert fileSuffix != null;

		final File dir = new File(path);
		final ArrayList<String> names = new ArrayList<>(dir.listFiles().length);
		for (final File file : dir.listFiles())
			names.add(file.getName());
		Collections.sort(names);
		return Collections.unmodifiableList(names);

//		List<String> tnames = ExperimentUtil.getFilesFromDir(path, fileSuffix);
//		tnames = removeDirPrefix(tnames);
//		if (filePrefix != "") {
//			final Iterator<String> namesIterator = tnames.iterator();
//			while (namesIterator.hasNext()) {
//				if (!namesIterator.next().startsWith(filePrefix))
//					namesIterator.remove();
//			}
//		}
//		return Collections.unmodifiableList(tnames);
	}

//	private List<String> removeDirPrefix(final List<String> files) {
//		final List<String> names = newArrayList();
//		for (final String f : files) {
//			names.add(f.substring(f.lastIndexOf('/') + 1));
//		}
//		return names;
//	}
}
