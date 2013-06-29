package neatsim;

import java.io.FileNotFoundException;
import java.io.IOException;

import neatsim.server.Server;
import neatsim.server.ServerConfig;

public class Main {
	public enum Mode {
		SERVER,EVALUATOR
	}
	public static void main(final String[] args) throws FileNotFoundException, IOException {
		final MainConfig conf = new MainConfig();
		switch(conf.getMode()) {
		case SERVER:
			System.out.println("Starting NeatSim in server mode.");
			final ServerConfig serverConf = new ServerConfig();
			final Server server = new Server(serverConf.getPort());
			server.start();
		case EVALUATOR:
			System.out.println("Starting NeatSim in evaluator mode.");
			new Evaluator();
		}
	}
}
