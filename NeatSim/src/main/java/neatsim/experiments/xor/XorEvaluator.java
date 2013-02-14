package neatsim.experiments.xor;

import java.util.ArrayList;
import java.util.List;

import neatsim.comm.thrift.CFastCyclicNetwork;
import neatsim.comm.thrift.CFitnessInfo;
import neatsim.comm.thrift.CPopulationFitness;
import neatsim.comm.thrift.CPopulationInfo;
import neatsim.core.BlackBox;
import neatsim.core.FastCyclicNetwork;
import neatsim.core.FitnessInfo;

/**
 * Instances of this class are XOR evaluators. XOR evaluates are capable of
 * assessing the quality of a black box as an approximation to the XOR function.
 * Functions that more closely resemble the XOR function are given a higher
 * fitness.
 * 
 * In addition, instances of this class provide methods that allow them to
 * directly evaluate {@see CPopulationInfo} and {@see CFastCyclicNetwork} Thrift
 * objects.
 * 
 * @author Jonathan Merlevede
 */
public class XorEvaluator {
	public static final double STOP_FITNESS = 10.0;

	/**
	 * Creates a new XOR evaluator.
	 */
	public XorEvaluator() {
		// Empty
	}
	
	/**
	 * Evaluates the given population by evaluating each fast cyclic network
	 * inside using {@see #evaluatePhenotype(CFastCyclicNetwork)}. The resulting
	 * collection of {@see CFitnessInfo} objects are returned as a {@see
	 * CPopulationFitness} object that can be used Thrift communication.
	 * 
	 * @param pi Information on the population to be evaluated.
	 * @pre The given population information is effective.
	 * 	| pi != null
	 * @return The population fitness information.
	 */
	public CPopulationFitness evaluatePopulation(CPopulationInfo pi) {
		assert pi != null;
		
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
	
	/**
	 * Evaluates the given fast cyclic neural network (FCNN).
	 * @param ann The Thrift FCNN to evaluate.
	 * @pre The given Thrift FCNN is effective.
	 * 
	 * 	| ann != null
	 * @return The fitness of the given FCNN.
	 */
	public CFitnessInfo evaluatePhenotype(CFastCyclicNetwork ann) {
		assert ann != null;
		
		FastCyclicNetwork fcn = new FastCyclicNetwork(ann);
		return evaluatePhenotype(fcn);
	}
	
	/**
	 * Returns whether this evaluator can evaluate the given black box.
	 * 
	 * @return True if the given black box is effective and the number of inputs
	 *         is equal to two and the number of outputs is equal to 1.
	 * 	
	 * 	| result == box != null
	 *		|	&& box.getNumberOfInputs() == 2
	 *		|	&& box.getNumberOfOutputs() == 1
	 */
	public boolean isValidBlackBox(BlackBox box) {
		return box != null
				&& box.getNumberOfInputs() == 2
				&& box.getNumberOfOutputs() == 1;
	}
	
	/**
	 * Evaluates the given black box.
	 * 
	 * @param box The black box to evaluate.
	 * @pre The given black box is a valid black box.
	 * 
	 * 	| isValidBlackBox(box)
	 * @return The fitness of the given black box.
	 */
	public CFitnessInfo evaluatePhenotype(BlackBox box) {
		assert box != null;
		assert box.getNumberOfInputs() == 2;
		assert box.getNumberOfOutputs() == 1;
		
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
