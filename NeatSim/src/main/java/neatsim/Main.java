package neatsim;

import neatsim.comm.Server;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Server server = new Server();
		server.start();
	}

}
