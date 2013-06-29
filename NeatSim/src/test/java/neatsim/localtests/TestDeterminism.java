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

public class TestDeterminism {
	protected NeuralNetworkFactory nnf;

	public TestDeterminism() {
		nnf = new NeuralNetworkFactory();
	}

//	@Test
//	public void testDifferentDistributedEvaluators() {
//		testDifferentEvaluators(GendreauEvaluator.ComputationStrategy.DISTRIBUTED);
//	}

//	@Test
//	public void testIdenticalDistributedEvaluators() {
//		testIdenticalEvaluators(GendreauEvaluator.ComputationStrategy.DISTRIBUTED);
//	}

	@Test
	public void testDifferentSinglethreadedEvaluators() {
		testDifferentEvaluators(GendreauEvaluator.InternalEvaluator.SINGLETHREADED);
	}

	@Test
	public void testIdenticalSinglethreadedEvaluators() {
		testIdenticalEvaluators(GendreauEvaluator.InternalEvaluator.SINGLETHREADED);
	}

	@Test
	public void testDifferentMultithreadedEvaluators() {
		testDifferentEvaluators(GendreauEvaluator.InternalEvaluator.MULTITHREADED);
	}

	@Test
	public void testIdenticalMultithreadedEvaluators() {
		testIdenticalEvaluators(GendreauEvaluator.InternalEvaluator.MULTITHREADED);
	}

	private void testDifferentEvaluators(final GendreauEvaluator.InternalEvaluator computationStrategy) {
		final List<GendreauScenario> s = GendreauScenario.load("data/", "req_rapide_1_240_33");
		s.addAll(GendreauScenario.load("data/", "req_rapide_2_240_33"));

		final GendreauEvaluator se1 = new GendreauEvaluator(
				s,
				SolutionType.MYOPIC,
				computationStrategy,
				new SimpleStopcondition(),
				new Absolute(),
				GendreauEvaluator.DEFAULT_NUMBER_OF_SCENARIOS_PER_GENERATION);
		final GendreauEvaluator se2 = new GendreauEvaluator(
				s,
				SolutionType.MYOPIC,
				computationStrategy,
				new SimpleStopcondition(),
				new Absolute(),
				GendreauEvaluator.DEFAULT_NUMBER_OF_SCENARIOS_PER_GENERATION);

		testEvaluators(s.size(), se1, se2);
	}

	private void testIdenticalEvaluators(final GendreauEvaluator.InternalEvaluator computationStrategy) {
		final List<GendreauScenario> s = GendreauScenario.load("data/", "req_rapide_1_240_33");
		s.addAll(GendreauScenario.load("data/", "req_rapide_2_240_33"));

		final GendreauEvaluator se1 = new GendreauEvaluator(
				s,
				SolutionType.MYOPIC,
				computationStrategy,
				new SimpleStopcondition(),
				new Absolute(),
				GendreauEvaluator.DEFAULT_NUMBER_OF_SCENARIOS_PER_GENERATION);

		testEvaluators(s.size(), se1, se1);
	}

	private void testEvaluators(final int nScenarios, final GendreauEvaluator se1, final GendreauEvaluator se2) {
		final List<NeuralNetwork> fcnns1 = new ArrayList<NeuralNetwork>();
		fcnns1.add(nnf.createDist());
		fcnns1.add(nnf.createClosest());
		final List<NeuralNetwork> fcnns2 = new ArrayList<NeuralNetwork>();
		fcnns2.add(nnf.createDist());
		fcnns2.add(nnf.createClosest());
		final CPopulationFitness popfit1 = se1.evaluatePopulation(fcnns1, 1, nScenarios);
		final CPopulationFitness popfit2 = se2.evaluatePopulation(fcnns2, 1, nScenarios);

		System.out.println("Number of scenarios: " + nScenarios);
		System.out.println("Dist: " + popfit1.fitnessInfos.get(0).getAuxFitness().get(0).value);
		System.out.println("Closest: " + popfit1.fitnessInfos.get(1).getAuxFitness().get(0).value);
		Assert.assertEquals(popfit1.fitnessInfos.get(0).fitness, popfit2.fitnessInfos.get(0).fitness, 0);
		Assert.assertEquals(popfit1.fitnessInfos.get(0).getAuxFitness().get(0).value, popfit2.fitnessInfos.get(0).getAuxFitness().get(0).value, 0);
		Assert.assertEquals(popfit1.fitnessInfos.get(1).fitness, popfit2.fitnessInfos.get(1).fitness, 0);
		Assert.assertEquals(popfit1.fitnessInfos.get(1).getAuxFitness().get(0).value, popfit2.fitnessInfos.get(1).getAuxFitness().get(0).value, 0);
		System.out.println(Float.MAX_VALUE);
	}
}
