package neatsim.experiments;
import org.apache.thrift.TException;

import neatsim.comm.thrift.*;
import neatsim.core.BlackBox;
import neatsim.core.FastCyclicNetwork;
import neatsim.core.FitnessInfo;

public class XorServiceImpl implements CFitnessCalculatorService.Iface {
	
	public static final double STOP_FITNESS = 10.0;
	
//	private int numberOfTimes = 1;
	@Override
	public CFitnessInfo calculateFitness(CFastCyclicNetwork ann)
			throws TException {
//		System.out.println("Function called " + numberOfTimes++ + " times."); 
		FastCyclicNetwork fcn = new FastCyclicNetwork(ann);
		CFitnessInfo fi = evaluate(fcn);
//		System.out.println("Fitness: " + fi.fitness);
		return fi;
	}
	
	private CFitnessInfo evaluate(BlackBox box) {
		double fitness = 0;
		double pass = 1.0;
		double output;
		
		box.reset();
		
		// Test inputs 0,0
		box.setInput(0, 0);
		box.setInput(1, 0);
		box.activate();
		if (!box.isValid()) {
			return FitnessInfo.ZERO;
		}
		output = box.getOutput(0);
		fitness +=1.0 - (output*output);
		if (output > 0.5)
			pass = 0.0;
		
		// Test inputs 1,1
		box.setInput(0, 1);
		box.setInput(1, 1);
		box.activate();
		if (!box.isValid()) {
			return FitnessInfo.ZERO;
		}
		output = box.getOutput(0);
		fitness +=1.0 - (output*output);
		if (output > 0.5)
			pass = 0.0;
		
		// Test inputs 0,1
		box.setInput(0, 0);
		box.setInput(1, 1);
		box.activate();
		if (!box.isValid()) {
			return FitnessInfo.ZERO;
		}
		output = box.getOutput(0);
		fitness +=1.0 - ((1-output)*(1-output));
		if (output <= 0.5)
			pass = 0.0;
		
		// Test inputs 1,0
		box.setInput(0, 1);
		box.setInput(1, 0);
		box.activate();
		if (!box.isValid()) {
			return FitnessInfo.ZERO;
		}
		output = box.getOutput(0);
		fitness +=1.0 - ((1-output)*(1-output));
		if (output <= 0.5)
			pass = 0.0;
		
		
		fitness += pass * 10.0;
		FitnessInfo fi = new FitnessInfo(fitness, fitness);
		fi.setStopConditionSatisfied(fitness >= STOP_FITNESS);
		return fi;
	}
}