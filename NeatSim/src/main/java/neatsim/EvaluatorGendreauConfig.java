package neatsim;

import java.io.File;
import java.io.IOException;
import java.util.List;

import neatsim.core.evaluators.gendreau.GendreauEvaluator;
import neatsim.core.evaluators.gendreau.GendreauScenario;
import neatsim.util.Config;

public class EvaluatorGendreauConfig extends Config {

	private static final String DIR = "neatsim.evaluator.gendreau.data.directory";
	private static final String PREFIX = "neatsim.evaluator.gendreau.data.prefix";
	private static final String SUFFIX = "neatsim.evaluator.gendreau.data.suffix";
	private static final String NUM_NONFINAL = "neatsim.evaluator.gendreau.scenarios.number.nonfinal";
	private static final String NUM_FINAL = "neatsim.evaluator.gendreau.scenarios.number.final";
	private static final String INTERNAL_EVALUATOR = "neatsim.evaluator.gendreau.evaluator";
	private static final String BATCH = "neatsim.evaluator.batch";
	private static final String USE_DATAPROVIDER = "neatsim.evaluator.usedataprovider";




	private final List<GendreauScenario> scenarios;
	private final GendreauEvaluator.InternalEvaluator internalEvaluator;
	private final int numberOfNonfinalGenerations;
	private final int numberOfFinalGenerations;
	private final boolean batch;
	private final boolean usedataprovider;

	public int getNumberOfScenariosInNonfinalGenerations() {
		return numberOfNonfinalGenerations;
	}


	public boolean getBatch() {
		return batch;
	}

	public int getNumberOfScenariosInFinalGeneration() {
		return numberOfFinalGenerations;
	}

	public List<GendreauScenario> getScenarios() {
		return scenarios;
	}

	public boolean isUseDataprovider() {
		return usedataprovider;
	}

	public GendreauEvaluator.InternalEvaluator getInternalEvaluator() {
		return internalEvaluator;
	}




	public EvaluatorGendreauConfig(final Config config) throws IOException {
		super(config);
		logger.debug("Creating EvaluatorGendreauConfig");
		numberOfNonfinalGenerations = getPropertyAsInteger(NUM_NONFINAL);
		if (numberOfNonfinalGenerations < 1)
			throw new InvalidConfigurationException(NUM_NONFINAL);
		numberOfFinalGenerations = getPropertyAsInteger(NUM_FINAL);
		if (numberOfFinalGenerations < 1)
			throw new InvalidConfigurationException(NUM_FINAL);
		scenarios = propToScenarios();
		GendreauEvaluator.InternalEvaluator t;;
		try {
			t = enumToProperty(GendreauEvaluator.InternalEvaluator.class, INTERNAL_EVALUATOR);
		} catch (final MissingConfigurationException e) {
			t = GendreauEvaluator.InternalEvaluator.DISTRIBUTED;
		}
		assert t != null;
		internalEvaluator = t;
		batch = getPropertyAsBoolean(BATCH, false);
		usedataprovider = getPropertyAsBoolean(USE_DATAPROVIDER, true);
	}

	private List<GendreauScenario> propToScenarios() throws IOException {
		final String directory = (new File(getProperty(DIR,""))).getCanonicalPath();
		String prefix;
		try {
			prefix = getProperty(PREFIX);
		} catch (final MissingConfigurationException e) {
			prefix = "";
		}
		assert prefix != null;
		String suffix;
		try {
			suffix = getProperty(SUFFIX);
		} catch (final MissingConfigurationException e) {
			suffix = "";
		}
		assert suffix != null;
		final List<GendreauScenario> scenarios = GendreauScenario.load(directory, prefix, suffix);
		printScenarioOrder(scenarios);
		return scenarios;
	}

	private void printScenarioOrder(final List<GendreauScenario> scenarios) {
		logger.info("Evaluating scenarios in the following order: ");
		for(final GendreauScenario scenario : scenarios) {
			logger.info("\t{}",scenario.getName());
		}
	}
}
