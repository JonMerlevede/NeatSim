package neatsim.server;

import java.io.FileNotFoundException;
import java.io.IOException;

import neatsim.server.thrift.CFitnessEvaluatorService;

import org.apache.thrift.TProcessor;
import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransport;
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
	private final int port;

	/**
	 * Creates a new instance of this class and starts it.
	 *
	 * @param args Arguments. These are ignored.
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static void main(final String[] args) throws FileNotFoundException, IOException {
		final Server server = new Server(7913);
		server.start();
	}

	public Server(final int port) {
		this.port = port;
	}

	/**
	 * Starts the server. This blocks the calling thread.
	 *
	 * Returns only if an exception occurs.
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public void start() throws FileNotFoundException, IOException {
		try {
//			TNonblockingServerTransport serverTransport
//				= new TNonblockingServerSocket(7911);
			// Reserve a socket for Thrift communication
			final TServerTransport serverTransport = new TServerSocket(port);
			// Create a new fitness evaluator.
//			// This evaluator provides an implementation of the Thrift service.
//			final CFitnessEvaluatorService.Iface impl = new FitnessEvaluator();
//			// Attach the fitness evaluator to a Thrift processor
//			final CFitnessEvaluatorService.Processor<CFitnessEvaluatorService.Iface>
//				processor = new CFitnessEvaluatorService.Processor<CFitnessEvaluatorService.Iface>(impl);
			// We now have to attach the processor to a Thrift server.
			/*
			 * There are multiple Thrift servers available. We use the
			 * TSimpleServer, which is the simplest Thrift server. It is single
			 * threaded. In my opinion this makes it easy to understand and debug.
			 * The 'better' Thrift servers are multithreaded and offer very high
			 * performance. We gain performance when using a multithreaded Thrift
			 * server, as we know that we have only a single client.
			 */
//			final TServer.Args args = new TSimpleServer.Args(serverTransport);
			//final TServer server = new TSimpleServer(args.processor(processor));
			// Uncomment two of the following lines and comment the previous lines
			// of code to switch to a high-perforamance, multi-threaded server
			// implementation.
			// See
			//		https://github.com/m1ch1/mapkeeper/wiki/Thrift-Java-Servers-Compared
			// for a comparison of the performance of the different Thrift servers.
//			TNonblockingServer.Args args = new TNonblockingServer.Args(serverTransport);
//			TServer server = new TNonblockingServer(args.processor(processor));
			final TThreadPoolServer.Args args= new TThreadPoolServer.Args(serverTransport);
			final TThreadPoolServer server = new TThreadPoolServer(args.processorFactory(new ProcessorFactory()));
			System.out.println("Starting server on port 7913...");
			// Start the server.
			// When using the TSimpleServer, this captures the running thread.
			server.serve();
		} catch (final TTransportException e) {
			e.printStackTrace();
		}
	}

	static class ProcessorFactory extends TProcessorFactory {

		public ProcessorFactory() {
			super(null);
		}

		@Override
		public TProcessor getProcessor(final TTransport trans) {
			CFitnessEvaluatorService.Iface impl;
			try {
				impl = new FitnessEvaluator();
			} catch (final IOException e) {
				throw new IllegalStateException(e);
			}
			// Attach the fitness evaluator to a Thrift processor
			final CFitnessEvaluatorService.Processor<CFitnessEvaluatorService.Iface>
				processor = new CFitnessEvaluatorService.Processor<CFitnessEvaluatorService.Iface>(impl);
			return processor;
		  }

	}
}