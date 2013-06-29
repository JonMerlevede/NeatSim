package neatsim.server;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import neatsim.core.evaluators.gendreau.GendreauEvaluator.InternalEvaluator;
import neatsim.core.fitnesstransformers.Absolute;
import neatsim.core.fitnesstransformers.FitnessTransformer;
import neatsim.core.fitnesstransformers.Invert;
import neatsim.core.fitnesstransformers.LinearRanking;
import neatsim.core.fitnesstransformers.ToppedAbsolute;
import neatsim.core.stopconditions.GenerationsStopCondition;
import neatsim.core.stopconditions.SimpleStopcondition;
import neatsim.core.stopconditions.Stopcondition;
import neatsim.util.Config;

public class ServerConfig extends Config {

	public static int DEFAULT_PORT = 7913;

	public int getPort() {
		return port;
	}

	public String getDataDirectory() {
		return dataDirectory;
	}

	public String getDataPrefix() {
		return dataPrefix;
	}

	public String getDataSuffix() {
		return dataSuffix;
	}

	public InternalEvaluator getStrategy() {
		return internalEvaluator;
	}

	public Stopcondition getStopcondition() {
		return stopcondition;
	}

	public FitnessTransformer getFitnessTransformer() {
		return fitnessTransformer;
	}

	public int getScenariosPerGeneration() {
		return scenariosPerGeneration;
	}

	private final String dataDirectory;
	private final String dataPrefix;
	private final String dataSuffix;
	private final InternalEvaluator internalEvaluator;
	private final Stopcondition stopcondition;
	private final FitnessTransformer fitnessTransformer;
	private final int scenariosPerGeneration;
	private final int port;

	private static final String PORT = "neatsim.server.port";
	private static final String PER_GENERATION = "neatsim.server.gendreau.data.pergeneration";
	private static final String PREFIX = "neatsim.server.gendreau.data.prefix";
	private static final String SUFFIX = "neatsim.server.gendreau.data.suffix";
	private static final String DIRECTORY = "neatsim.server.gendreau.data.directory";
	private static final String STRATEGY = "neatsim.server.gendreau.evaluator";
	private static final String STOPCONDITION_TYPE = "neatsim.server.gendreau.stopcondition";
	private static final String STOPCONDITION_GENERATIONS_NUMBER = "neatsim.server.gendreau.stopcondition.generations.number";
	private static final String TRANSFORMER_TYPE = "neatsim.server.gendreau.fitnesstransformer.type";
	private static final String TRANSFORMER_LINEAR_PRESSURE = "neatsim.server.gendreau.fitnesstransformer.linear.selectivepressure";
	private static final String TRANSFORMER_ABSOLUTE_MAXFITNESS = "neatsim.server.gendreau.fitnesstransformer.absolute.maxfitness";

	public ServerConfig() throws IOException {
		super();
		final Properties prop = getConfig();
		dataDirectory = (new File(getProperty(DIRECTORY,""))).getCanonicalPath();
		dataPrefix = getProperty(PREFIX,"");
		dataSuffix = getProperty(SUFFIX,"");
		internalEvaluator = enumToProperty(InternalEvaluator.class, STRATEGY);
		stopcondition = propToStopcondition(prop);
		fitnessTransformer = propToFitnessTransformer(prop);
		scenariosPerGeneration = propToScenariosPerGeneration(prop);
		port = getPropertyAsInteger(PORT, DEFAULT_PORT);
	}

	private int propToScenariosPerGeneration(final Properties prop) {
		try {
			final int dataPerGeneration = Integer.parseInt(getProperty(PER_GENERATION).trim());
			return dataPerGeneration;
		} catch (final NumberFormatException e) {
			throw new InvalidConfigurationException(PER_GENERATION);
		}
	}


	private Stopcondition propToStopcondition(final Properties prop) {
		final String type = getProperty(STOPCONDITION_TYPE);
		switch (type.toUpperCase()) {
		case "SIMPLE" : return new SimpleStopcondition();
		case "GENERATIONS" :
			final int nGenerations = getPropertyAsInteger(STOPCONDITION_GENERATIONS_NUMBER);
			return new GenerationsStopCondition(nGenerations);
		default:
			throw new InvalidConfigurationException(STOPCONDITION_TYPE);
		}
	}

	private FitnessTransformer propToFitnessTransformer(final Properties prop) {
		final String type = getProperty(TRANSFORMER_TYPE);

		switch (type.toUpperCase()) {
		case "INVERT": return new Invert();
		case "LINEAR":
			final int selectivePressure = getPropertyAsInteger(TRANSFORMER_LINEAR_PRESSURE);
			return new LinearRanking(selectivePressure);
		case "ABSOLUTE":
			final String maxFitness = getProperty(TRANSFORMER_ABSOLUTE_MAXFITNESS);
			if (maxFitness.equalsIgnoreCase("AUTO"))
				return new Absolute();
			else {
				try {
					final int imaxFitness = Integer.parseInt(maxFitness);
					return new ToppedAbsolute(imaxFitness);
				} catch (final NumberFormatException e) {
					throw new InvalidConfigurationException(TRANSFORMER_ABSOLUTE_MAXFITNESS);
				}
			}
		default:
			throw new InvalidConfigurationException(TRANSFORMER_TYPE);
		}
	}
}
