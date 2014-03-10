package neatsim.localtests;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import neatsim.core.blackbox.BlackBox;
import neatsim.core.blackbox.neural.NeuralNetwork;
import neatsim.core.evaluators.gendreau.GendreauEvaluator;
import neatsim.core.evaluators.gendreau.GendreauScenario;
import neatsim.core.fitnesstransformers.Absolute;
import neatsim.core.stopconditions.SimpleStopcondition;
import neatsim.server.thrift.CPopulationFitness;
import neatsim.util.NeuralNetworkFactory;
import neatsim.util.NeuralNetworkReader;

import org.junit.Assert;
import org.junit.Test;

import rinde.evo4mas.gendreau06.GSimulationTask.SolutionType;

public class TestShortScenario {
//test_req_rapide_001_450_24
	protected NeuralNetworkFactory nnf;

	public TestShortScenario() {
		nnf = new NeuralNetworkFactory();
	}

	@Test
	public void testShortScenario() throws IOException {
//		final GendreauEvaluatorFactory seh = GendreauEvaluatorFactory.newInstance();
//		final Pair<List<String>,List<String>> tp = null;
//		try {
//			tp = seh.readScenariosFromDirectory("data/", "_240_24");
//		} catch (final IOException e) { e.printStackTrace(); }
//		final List<String> scenarioNames = tp.getValue0();
//		final List<String> scenarioContents = tp.getValue1();

		final List<GendreauScenario> s = GendreauScenario.load("data/", "240_24");
		//final List<GendreauScenario> s = GendreauScenario.load("data/", ".xml");
//		s.addAll(GendreauScenario.load("mydata/", "test_req_rapide_002_450_24"));
//		s.addAll(GendreauScenario.load("mydata/", "test_req_rapide_003_450_24"));
//		s.addAll(GendreauScenario.load("mydata/", "test_req_rapide_004_450_24"));
//		s.addAll(GendreauScenario.load("mydata/", "test_req_rapide_005_450_24"));

		final GendreauEvaluator se = new GendreauEvaluator(
				s,
				SolutionType.MYOPIC,
				GendreauEvaluator.InternalEvaluator.SINGLETHREADED,
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


		final NeuralNetworkReader reader = new NeuralNetworkReader(3);
		final URL testNetwork = getClass().getResource("/closest.xml");
		System.out.println(testNetwork);
		final List<NeuralNetwork> networks = reader.readFile(testNetwork).neuralNetworks;
//		assertEquals(1, networks.size());
		final NeuralNetwork closest = networks.get(0);
//		final BlackBox closest = new BlackBox() {
//			private double closestVehicle = 0;
//			private double dist = 0;
//			private double tua = 0;
//			@Override
//			public void setInput(final int no, final double val) {
//				if (no == NeuralNetworkFactory.ID_MIDC)
//					closestVehicle = val;
//				if (no == NeuralNetworkFactory.ID_DIST)
//					dist = val;
//				if (no == NeuralNetworkFactory.ID_TIME_UNTIL_AVAILABLE)
//					tua = val;
//			}
//
//			@Override
//			public double getOutput(final int no) {
//				if (Math.abs(dist - closestVehicle) < 1E-4) {
//					if (NeuralNetworkFactory.ID_TIME_UNTIL_AVAILABLE)
//					return dist;
//				} else {
//					return 1E30;
//				}
//			}
//
//			@Override
//			public int getNumberOfInputs() {
//				return NeuralNetworkFactory.NUMBER_OF_INPUTS;
//			}
//
//			@Override
//			public int getNumberOfOutputs() {
//				return 1;
//			}
//
//			@Override
//			public void activate() {
//				// do nothing
//			}
//
//			@Override
//			public void reset() {
//				closestVehicle = 0;
//				dist = 0;
//				tua = 0;
//			}
//
//			@Override
//			public boolean isValid() {
//				return true;
//			}
//
//			@Override
//			public String getId() {
//				return hashCode()+"jfkjerk;lqjekl;rj";
//			}
//		};


		final List<BlackBox> fcnns = new ArrayList<BlackBox>();
		fcnns.add(nnf.createDist());
		fcnns.add(nnf.createClosest());
		fcnns.add(closest);
		final CPopulationFitness popfit = se.evaluatePopulation(fcnns, 1, s.size());
		System.out.println("Number of scenarios: " + s.size());
		System.out.println("Cost by 'distance' ANN: " + popfit.fitnessInfos.get(0).getAuxFitness().get(0).value);
		System.out.println("Cost by 'closest' ANN: " + popfit.fitnessInfos.get(1).getAuxFitness().get(0).value);
		System.out.println("Cost by 'closest' ANN as read from file (faulty): " + popfit.fitnessInfos.get(2).getAuxFitness().get(0).value);

		Assert.assertEquals(false, popfit.fitnessInfos.get(0).getAuxFitness().get(0).value == Float.MAX_VALUE);
		Assert.assertEquals(false, popfit.fitnessInfos.get(1).getAuxFitness().get(0).value == Float.MAX_VALUE);
		Assert.assertEquals(false, popfit.fitnessInfos.get(2).getAuxFitness().get(0).value == Float.MAX_VALUE);
		System.out.println(Float.MAX_VALUE);
	}
}
