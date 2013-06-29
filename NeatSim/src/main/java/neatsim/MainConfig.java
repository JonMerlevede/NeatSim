package neatsim;

import neatsim.util.Config;

public class MainConfig extends Config {
	private final Main.Mode mode;

	private static final String MODE = "neatsim.mode";

	public Main.Mode getMode() {
		return mode;
	}

	public MainConfig() {
		mode = enumToProperty(Main.Mode.class, MODE);
	}
}
