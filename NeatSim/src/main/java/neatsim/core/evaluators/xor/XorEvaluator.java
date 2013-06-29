package neatsim.core.evaluators.xor;

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
public class XorEvaluator implements PopulationEvaluator {
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

	/**
	 * Evaluates the given fast cyclic neural network (FCNN).
	 * @param ann The Thrift FCNN to evaluate.
	 * @pre The given Thrift FCNN is effective.
	 *
	 * 	| ann != null
	 * @return The fitness of the given FCNN.
	 */
	public CFitnessInfo evaluatePhenotype(final CFastCyclicNetwork ann) {
		assert ann != null;

		final NeuralNetwork fcn = new NeuralNetwork(ann);
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
	public boolean isValidBlackBox(final BlackBox box) {
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
	public CFitnessInfo evaluatePhenotype(final BlackBox box) {
		assert box != null;
		assert box.getNumberOfInputs() == 2;
		assert box.getNumberOfOutputs() == 1;

		double fitness = 0;
		boolean pass = true;

		// Test inputs 0,0
		box.reset();
		box.setInput(0, 0);
		box.setInput(1, 0);
		box.activate();
		if (!box.isValid()) return FitnessInfo.ZERO;
		fitness += 1.0d - (box.getOutput(0)*box.getOutput(0));
		if (box.getOutput(0) > 0.5d)
			pass = false;

		// Test inputs 1,1
		box.reset();
		box.setInput(0, 1);
		box.setInput(1, 1);
		box.activate();
		if (!box.isValid()) return FitnessInfo.ZERO;
		fitness += 1.0d - (box.getOutput(0)*box.getOutput(0));
		if (box.getOutput(0) > 0.5d)
			pass = false;

		// Test inputs 0,1
		box.reset();
		box.setInput(0, 0);
		box.setInput(1, 1);
		box.activate();
		if (!box.isValid()) return FitnessInfo.ZERO;
		fitness += 1.0 - ((1-box.getOutput(0))*(1-box.getOutput(0)));
		if (box.getOutput(0) <= 0.5d)
			pass = false;

		// Test inputs 1,0
		box.reset();
		box.setInput(0, 1);
		box.setInput(1, 0);
		box.activate();
		if (!box.isValid())
			return FitnessInfo.ZERO;
		fitness += 1.0 - ((1-box.getOutput(0))*(1-box.getOutput(0)));
		if (box.getOutput(0) <= 0.5d)
			pass = false;


		if (pass)
			fitness += 10.0d;
		final FitnessInfo fi = new FitnessInfo(fitness);
		fi.setStopConditionSatisfied(fitness >= STOP_FITNESS);
		assert fi.stopConditionSatisfied == pass;

		if (fi.stopConditionSatisfied) {
			for (int i = 1; i <= 100; i++)
				assert isNeuralNetworkImprecise(box);
		}
		return fi;
	}

	private boolean isNeuralNetworkImprecise(final BlackBox nn) {
		nn.reset();
		nn.setInput(0, 0);
		nn.setInput(1, 0);
		nn.activate();
		System.out.println(nn.getOutput(0));
		if (nn.getOutput(0) > 0.5d) {
			System.out.println("Invalid: 0,0");
			return false;
		}


		nn.reset();
		nn.setInput(0, 0);
		nn.setInput(1, 1);
		nn.activate();
		System.out.println(nn.getOutput(0));
		if (nn.getOutput(0) <= 0.5d) {
			System.out.println("Invalid: 0,1");
			return false;
		}

		nn.reset();
		nn.setInput(0, 1);
		nn.setInput(1, 0);
		nn.activate();
		System.out.println(nn.getOutput(0));
		if (nn.getOutput(0) <= 0.5d) {
			System.out.println("Invalid: 1,0");
			return false;
		}

		nn.reset();
		nn.setInput(0, 1);
		nn.setInput(1, 1);
		nn.activate();
		System.out.println(nn.getOutput(0));
		if (nn.getOutput(0) > 0.5d) {
			System.out.println("Invalid: 1,1");
			return false;
		}

		return true;
	}

}
