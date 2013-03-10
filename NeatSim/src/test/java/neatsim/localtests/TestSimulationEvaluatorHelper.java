package neatsim.localtests;
import java.util.ArrayList;
import java.util.List;

import neatsim.core.NeuralNetwork;
import neatsim.core.evaluators.GendreauEvaluator;
import neatsim.core.evaluators.GendreauScenario;
import neatsim.util.NeuralNetworkFactory;
import rinde.evo4mas.gendreau06.GSimulationTask.SolutionType;


public class TestSimulationEvaluatorHelper {
	protected NeuralNetworkFactory nnf;

	public TestSimulationEvaluatorHelper() {
		nnf = new NeuralNetworkFactory();
	}

	public void testEvaluator(
			final GendreauEvaluator.ComputationStrategy strategy,
			final SolutionType solutionType,
			final boolean multiple,
			final boolean identical) {
//		final GendreauEvaluatorFactory seh = GendreauEvaluatorFactory.newInstance();
//		final Pair<List<String>,List<String>> tp = null;
//		try {
//			tp = seh.readScenariosFromDirectory("data/", "_240_24");
//		} catch (final IOException e) { e.printStackTrace(); }
//		final List<String> scenarioNames = tp.getValue0();
//		final List<String> scenarioContents = tp.getValue1();

		final GendreauEvaluator se = new GendreauEvaluator(
				GendreauScenario.load("data/", "_240_24"),
				solutionType,
				strategy);
//				new GendreauEvaluator(
//				scenarioNames,
//				scenarioContents,
//				1, // number of scenarios per generation
//				solutionType,
//				strategy,
//				new SimpleStopcondition());

		final List<NeuralNetwork> fcnns = new ArrayList<NeuralNetwork>();
		fcnns.add(nnf.createDist());
		if (multiple) {
			// There are now multiple anns in the list, but all anns are different
			fcnns.add(nnf.createClosest());
			fcnns.add(nnf.createDist2());
			if (identical) {
				// There are now multiple anns in the list, including identical ones
				fcnns.add(nnf.createDist());
			}
		}

		se.evaluatePopulation(fcnns, 1);
	}
}
