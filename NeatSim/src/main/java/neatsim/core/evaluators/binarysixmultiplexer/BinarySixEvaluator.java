package neatsim.core.evaluators.binarysixmultiplexer;

import java.util.ArrayList;
import java.util.List;

import neatsim.core.FitnessInfo;
import neatsim.core.blackbox.BlackBox;
import neatsim.core.blackbox.neural.NeuralNetwork;
import neatsim.core.evaluators.PopulationEvaluator;
import neatsim.server.thrift.CFastCyclicNetwork;
import neatsim.server.thrift.CFitnessInfo;
import neatsim.server.thrift.CPopulationFitness;
import neatsim.server.thrift.CPopulationInfo;

public class BinarySixEvaluator implements PopulationEvaluator {
	public static final double STOP_FITNESS = 1000d;

	@Override
	public CPopulationFitness evaluatePopulation(final CPopulationInfo pi) {
		assert pi != null;

		int evaluationCount = 0;
		final int n = pi.getPhenomes().size();
		final List<CFitnessInfo> fitnessInfos = new ArrayList<CFitnessInfo>(n);
		for (int i = 0; i < pi.getPhenomes().size(); i++) {
			evaluationCount++;
			final CFitnessInfo fi = evaluatePhenotype(pi.getPhenomes().get(i));
			fitnessInfos.add(i, fi);
			if (fi.stopConditionSatisfied)
				break;
		}
		final CPopulationFitness pf = new CPopulationFitness();
		pf.setFitnessInfos(fitnessInfos);
		pf.setEvaluationCount(evaluationCount);
		return pf;
	}

	public CFitnessInfo evaluatePhenotype(final CFastCyclicNetwork ann) {
		assert ann != null;

		final NeuralNetwork fcn = new NeuralNetwork(ann);
		return evaluatePhenotype(fcn);
	}


	public CFitnessInfo evaluatePhenotype(final BlackBox box) {
		double fitness = 0.0;
      boolean success = true;
      double output;

      for (int i=0; i<64; i++) {
      	box.reset();
      	int tmp = i;
      	for (int j=0; j < 6; j++) {
      		box.setInput(j, tmp&0x1);
      		tmp >>= 1;
      	}
      	// Activate the black box
      	box.activate();
      	if (!box.isValid()) // invalid black boxes get fitness zero
      		return FitnessInfo.ZERO;
      	// Read output signl
      	output = box.getOutput(0);
      	// Determine correct answer using cryptic bit manipulation
      	if (((1<<(2+(i&0x3)))&i) != 0) {
      		// correct answer = true
      		fitness += 1d-((1d-output)*(1d-output));
      		if (output < 0.5)
      			success = false;
      	} else {
      		// correct answer = false
      		fitness += 1d-(output*output);
      		if(output >= 0.5)
      			success = false;
      	}
      }

      if (success) {
      	fitness += 1000d;
      }

      final FitnessInfo fi = new FitnessInfo(fitness);
		fi.setStopConditionSatisfied(fitness >= STOP_FITNESS);
		return fi;
	}

}
