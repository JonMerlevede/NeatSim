package neatsim.localtests;

import java.util.ArrayList;
import java.util.List;

import neatsim.core.blackbox.neural.NeuralNetwork;
import neatsim.core.evaluators.gendreau.GendreauEvaluator;
import neatsim.core.evaluators.gendreau.GendreauScenario;
import neatsim.core.fitnesstransformers.Absolute;
import neatsim.core.stopconditions.SimpleStopcondition;
import neatsim.server.thrift.CPopulationFitness;
import neatsim.util.NeuralNetworkFactory;

import org.junit.Assert;
import org.junit.Test;

import rinde.evo4mas.gendreau06.GSimulationTask.SolutionType;

public class TestLongScenario {
//test_req_rapide_001_450_24
	protected NeuralNetworkFactory nnf;

	public TestLongScenario() {
		nnf = new NeuralNetworkFactory();
	}

	@Test
	public void testLongScenario() {
//		final GendreauEvaluatorFactory seh = GendreauEvaluatorFactory.newInstance();
//		final Pair<List<String>,List<String>> tp = null;
//		try {
//			tp = seh.readScenariosFromDirectory("data/", "_240_24");
//		} catch (final IOException e) { e.printStackTrace(); }
//		final List<String> scenarioNames = tp.getValue0();
//		final List<String> scenarioContents = tp.getValue1();

		final List<GendreauScenario> s = GendreauScenario.load("data/", "_450_24");
		s.addAll(GendreauScenario.load("mydata/", "test_req_rapide_002_450_24"));
//		s.addAll(GendreauScenario.load("mydata/", "test_req_rapide_003_450_24"));
//		s.addAll(GendreauScenario.load("mydata/", "test_req_rapide_004_450_24"));
//		s.addAll(GendreauScenario.load("mydata/", "test_req_rapide_005_450_24"));

		final GendreauEvaluator se = new GendreauEvaluator(
				s,
				SolutionType.MYOPIC,
				GendreauEvaluator.InternalEvaluator.MULTITHREADED,
				new SimpleStopcondition(),
				new Absolute(),
				GendreauEvaluator.DEFAULT_NUMBER_OF_SCENARIOS_PER_GENERATION);
//				new GendreauEvaluator(
//				scenarioNames,
//				scenarioContents,
//				1, // number of scenarios per generation
//				solutionType,
//				strategy,
//				new SimpleStopcondition());

		final List<NeuralNetwork> fcnns = new ArrayList<NeuralNetwork>();
		fcnns.add(nnf.createDist());
		fcnns.add(nnf.createClosest());
		final CPopulationFitness popfit = se.evaluatePopulation(fcnns, 1, s.size());
		System.out.println("Number of scenarios: " + s.size());
		System.out.println("Distance: " + popfit.fitnessInfos.get(0).getAuxFitness().get(0).value);
		System.out.println("Closest: " + popfit.fitnessInfos.get(1).getAuxFitness().get(0).value);
		Assert.assertEquals(false, popfit.fitnessInfos.get(0).getAuxFitness().get(0).value == Float.MAX_VALUE);
		Assert.assertEquals(false, popfit.fitnessInfos.get(1).getAuxFitness().get(0).value == Float.MAX_VALUE);
		System.out.println(Float.MAX_VALUE);
	}
}
