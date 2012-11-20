package neatsim.comm;

import neatsim.comm.thrift.CFitnessEvaluatorService;
import neatsim.experiments.FitnessEvaluatorServiceImplementation;

import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;

public class Server {
	public void start() {
		try {
//			TNonblockingServerTransport serverTransport = new TNonblockingServerSocket(7911);
			TServerTransport serverTransport = new TServerSocket(7911);
			FitnessEvaluatorServiceImplementation impl = new FitnessEvaluatorServiceImplementation();
			CFitnessEvaluatorService.Processor<CFitnessEvaluatorService.Iface> processor
				= new CFitnessEvaluatorService.Processor<CFitnessEvaluatorService.Iface>(impl);
//			TNonblockingServer.Args args = new TNonblockingServer.Args(serverTransport);
//			TServer server = new TNonblockingServer(args.processor(processor));
			TServer.Args args = new TSimpleServer.Args(serverTransport);
			TServer server = new TSimpleServer(args.processor(processor));
//			TThreadPoolServer.Args args= new TThreadPoolServer.Args(serverTransport);
//			TThreadPoolServer server = new TThreadPoolServer(args.processor(processor));
			System.out.println("Starting server on port 7911...");
			server.serve();
		} catch (TTransportException e) {
			e.printStackTrace();
		}
	}
}
