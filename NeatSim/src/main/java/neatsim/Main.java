package neatsim;


/**
 * Main class that {@link Server#start() starts} a {@link Server server} serving
 * the Thrift FitnessEvaluatorService.
 * 
 * @author Jonathan Merlevede
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Server server = new Server();
		server.start();
	}

}
