package neatsim;

import neatsim.evaluators.FitnessEvaluator;
import neatsim.thrift.CFitnessEvaluatorService;

import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;

/**
 * Class that implements a server that provides the Thrift
 * FitnessEvaluatorService using the Thrift binary protocol. After creation, the
 * server needs to be started.
 * 
 * The Thrift server is currently started on a fixed port (7913), and will fail
 * if this port is not available.
 * 
 * @author Jonathan Merlevede
 */
public class Server {
	/**
	 * Creates a new instance of this class and starts it.
	 * 
	 * @param args Arguments. These are ignored.
	 */
	public static void main(String[] args) {
		Server server = new Server();
		server.start();
	}
	
	/**
	 * Starts the server. This blocks the calling thread.
	 * 
	 * Returns only if an exception occurs.
	 */
	public void start() {
		try {
//			TNonblockingServerTransport serverTransport
//				= new TNonblockingServerSocket(7911);
			// Reserve a socket for Thrift communication
			TServerTransport serverTransport = new TServerSocket(7913);
			// Create a new fitness evaluator.
			// This evaluator provides an implementation of the Thrift service.
			CFitnessEvaluatorService.Iface impl = new FitnessEvaluator();
			// Attach the fitness evaluator to a Thrift processor
			CFitnessEvaluatorService.Processor<CFitnessEvaluatorService.Iface>
				processor = new CFitnessEvaluatorService.Processor<CFitnessEvaluatorService.Iface>(impl);
			// We now have to attach the processor to a Thrift server.
			/*
			 * There are multiple Thrift servers available. We use the
			 * TSimpleServer, which is the simplest Thrift server. It is single
			 * threaded. In my opinion this makes it easy to understand and debug.
			 * The 'better' Thrift servers are multithreaded and offer very high
			 * performance. We gain performance when using a multithreaded Thrift
			 * server, as we know that we have only a single client.
			 */
			TServer.Args args = new TSimpleServer.Args(serverTransport);
			TServer server = new TSimpleServer(args.processor(processor));
			// Uncomment two of the following lines and comment the previous lines
			// of code to switch to a high-perforamance, multi-threaded server
			// implementation.
			// See
			//		https://github.com/m1ch1/mapkeeper/wiki/Thrift-Java-Servers-Compared
			// for a comparison of the performance of the different Thrift servers.
//			TNonblockingServer.Args args = new TNonblockingServer.Args(serverTransport);
//			TServer server = new TNonblockingServer(args.processor(processor));
//			TThreadPoolServer.Args args= new TThreadPoolServer.Args(serverTransport);
//			TThreadPoolServer server = new TThreadPoolServer(args.processor(processor));
			System.out.println("Starting server on port 7913...");
			// Start the server.
			// When using the TSimpleServer, this captures the running thread.
			server.serve();
		} catch (TTransportException e) {
			e.printStackTrace();
		}
	}
}