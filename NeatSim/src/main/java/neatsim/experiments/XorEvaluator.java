package neatsim.experiments;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import neatsim.comm.thrift.CFastCyclicNetwork;
import neatsim.comm.thrift.CFitnessInfo;
import neatsim.comm.thrift.CPopulationFitness;
import neatsim.comm.thrift.CPopulationInfo;
import neatsim.core.BlackBox;
import neatsim.core.FastCyclicNetwork;
import neatsim.core.FitnessInfo;

public class XorEvaluator {
	public static final double STOP_FITNESS = 10.0;

	public XorEvaluator() {
		// Empty
	}
	
	public CPopulationFitness evaluatePopulation(CPopulationInfo pi) {
		int evaluationCount = 0;
		int n = pi.getPhenomes().size();
		List<CFitnessInfo> fitnessInfos = new ArrayList<CFitnessInfo>(n);
		for (int i = 0; i < pi.getPhenomes().size(); i++) {
			evaluationCount++;
			CFitnessInfo fi = evaluatePhenotype(pi.getPhenomes().get(i));
			fitnessInfos.add(i, fi);
			if (fi.stopConditionSatisfied)
				break;
		}
		CPopulationFitness pf = new CPopulationFitness();
		pf.setFitnessInfos(fitnessInfos);
		pf.setEvaluationCount(evaluationCount);
		return pf;
	}
	
	public CFitnessInfo evaluatePhenotype(CFastCyclicNetwork ann) {
		FastCyclicNetwork fcn = new FastCyclicNetwork(ann);
		return evaluatePhenotype(fcn);
	}
	
	public CFitnessInfo evaluatePhenotype(BlackBox box) {
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
