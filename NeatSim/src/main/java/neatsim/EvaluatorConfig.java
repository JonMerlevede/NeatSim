package neatsim;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import neatsim.core.blackbox.BlackBox;
import neatsim.core.blackbox.neural.NeuralNetwork;
import neatsim.util.Config;
import neatsim.util.NaturalOrderComparator;
import neatsim.util.NeuralNetworkReader;
import neatsim.util.PrefixSuffixFilter;

public class EvaluatorConfig extends Config {
	private final Evaluator.Type type;
	private final List<BlackBox> genomes;
	private final File outputPath;
	private final boolean dry;

	private static final String DRY = "neatsim.evaluator.dryrun";
	private static final String TYPE = "neatsim.evaluator.type";
	private static final String DATADIR = "neatsim.evaluator.data.directory";
	private static final String PREFIX = "neatsim.evaluator.data.prefix";
	private static final String SUFFIX = "neatsim.evaluator.data.suffix";
	private static final String EVALUTATIONSTEPS = "neatsim.evaluator.evaluationsteps";
	private static final String OUTPUTPATH = "neatsim.evaluator.output.path";
	private static final String SINGLETON = "neatsim.evaluator.singletongenome";
	public static final int DEFAULT_NUMBER_OF_TIMESTEPS_PER_ACTIVATION = 3;

	public Evaluator.Type getType() {
		return type;
	}

	public List<BlackBox> getGenomes() {
		return genomes;
	}

	public File getOutputFile() {
		return outputPath;
	}

	public boolean isDry() {
		return dry;
	}


	public EvaluatorConfig() throws IOException {
		super();
		logger.debug("Creating EvaluatorConfig");
		type = enumToProperty(Evaluator.Type.class, TYPE);
		genomes = propToBlackboxes();
		final String t1 = getProperty(OUTPUTPATH,"output.csv");
		final File t2 = (new File(t1)).getCanonicalFile();
		if (t2.isDirectory())
			throw new InvalidConfigurationException(OUTPUTPATH, "specified path is a directory");
		if (!(t2.exists() && t2.canWrite()) && !t2.createNewFile())
			throw new InvalidConfigurationException(OUTPUTPATH, "specified path is not writable");
		outputPath = t2;
		dry = getPropertyAsBoolean(DRY,false);
	}


	private int propToTimesteps() {
		try {
			return getPropertyAsInteger(EVALUTATIONSTEPS);
		} catch (final MissingConfigurationException e) {
			return DEFAULT_NUMBER_OF_TIMESTEPS_PER_ACTIVATION;
		}
		//throw new InvalidConfigurationException(EVALUTATIONSTEPS);
	}

	// TODO change this so that this uses NeuralNetworkReader.readDirectory
	public List<BlackBox> propToBlackboxes() throws IOException {
	// Read data directory setting
			String dataDirectory = (new File(getProperty(DATADIR, ""))).getCanonicalPath();
			// Read prefix setting
			final String prefix = getProperty(PREFIX, "");
			// Read suffix setting
			final String suffix = getProperty(SUFFIX, "");
			// Read timesteps setting
			final int timesteps = propToTimesteps();

			if (!dataDirectory.endsWith(File.separator))
				dataDirectory = dataDirectory + File.separator;

			final boolean singletonGenome = getPropertyAsBoolean(SINGLETON, true);
			final File folder = new File(dataDirectory);
			final FilenameFilter filenameFilter = new PrefixSuffixFilter(prefix, suffix);
			final List<String> sgenomes = new ArrayList<String>();
			for (final File file : folder.listFiles(filenameFilter))
				sgenomes.add(dataDirectory + file.getName());
			Collections.sort(sgenomes, NaturalOrderComparator.CASEINSENSITIVE_NUMERICAL_ORDER);
			printEvaluationOrder(sgenomes);
			//# Convert the genome path names to ANNs
			final List<BlackBox> genomes = new ArrayList<BlackBox>(sgenomes.size());
			final NeuralNetworkReader reader = new NeuralNetworkReader(timesteps);
			if (singletonGenome) {
				for (final String sgenome : sgenomes) {
					final List<NeuralNetwork> t = reader.readFile(sgenome).neuralNetworks;
					if (t.size() != 1) {
						logger.warn("File {} contains more than one genome.",sgenome);
						throw new RuntimeException("File " + sgenome + " contains more than one genome.");
					}
					genomes.add(t.get(0));
				}
			} else {
				for (final String sgenome : sgenomes) {
					final List<NeuralNetwork> t = reader.readFile(sgenome).neuralNetworks;
					for (final BlackBox bb : t)
						genomes.add(bb);
				}
			}
			logger.info("Genomes were read from file correctly");
			return genomes;
	}

	private void printEvaluationOrder(final List<String> sgenomes) {
		logger.info("Evaluating genomes in the following order: ");
		for(final String genome : sgenomes) {
			logger.info("\t{}",genome);
		}
	}
}
