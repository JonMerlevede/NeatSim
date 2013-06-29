package neatsim.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Config {
	private final Properties config;
	protected final Logger logger = LoggerFactory.getLogger(Config.class);

	public static class MissingConfigurationException extends RuntimeException {
		private static final long serialVersionUID = 7658107087636985589L;

		public MissingConfigurationException(final String config) {
			super("Invalid configuration: property " + config + " is missing from properties file.");
		}
	}
	public static class InvalidConfigurationException extends RuntimeException {
		private static final long serialVersionUID = -7807365937463686946L;

		public InvalidConfigurationException(final String config) {
			super("Invalid configuration: " + config + " has an invalid value.");
		}

		public InvalidConfigurationException(final String config, final String val) {
			super("Invalid configuration: " + config + " has an invalid value (" + val + ").");
		}
	}

	protected Config() {
		logger.debug("Creating Config");
		final Properties prop = new Properties();
		final File propFile = new File("neatsim.properties");
		try {
			prop.load(new BufferedReader(new FileReader(propFile)));
		} catch (final IOException e) {
			throw new RuntimeException("Cannot find specified properties file :(");
		}
		config = prop;
	}

	protected Config(final Config config) {
		this.config = config.config;
	}

	protected String getProperty(final String p) {
		if (!config.containsKey(p))
			throw new MissingConfigurationException(p);
		return config.getProperty(p).trim();
	}

	protected String getProperty(final String p, final String d) {
		if (!config.containsKey(p))
			return d;
		return config.getProperty(p).trim();
	}

	protected int getPropertyAsInteger(final String p) {
		if (!config.containsKey(p))
			throw new MissingConfigurationException(p);
		try {
			return Integer.parseInt(config.getProperty(p).trim());
		} catch (final NumberFormatException e) {
			throw new InvalidConfigurationException(p);
		}
	}

	protected int getPropertyAsInteger(final String p, final int d) {
		if (!config.containsKey(p))
			return d;
		return getPropertyAsInteger(p);
	}

	protected boolean getPropertyAsBoolean(final String p) {
		if (!config.containsKey(p))
			throw new MissingConfigurationException(p);
		switch (getProperty(p).toUpperCase()){
		case "TRUE": return true;
		case "FALSE": return false;
		default: throw new InvalidConfigurationException(p);
		}
	}

	protected boolean getPropertyAsBoolean(final String p, final boolean d) {
		if (!config.containsKey(p))
			return d;
		else
			return getPropertyAsBoolean(p);
	}

	protected <T extends Enum<T>> T enumToProperty(final Class<T> e, final String s) {
		final String v = getProperty(s);
		for (final T t : e.getEnumConstants()) {
			if (t.name().equalsIgnoreCase(v))
				return t;
		}
		throw new InvalidConfigurationException(s);
	}

	protected Properties getConfig() {
		assert config != null;
		return config;
	}
}
