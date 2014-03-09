package neatsim;

import java.util.List;

import neatsim.core.blackbox.BlackBox;
import neatsim.core.blackbox.BlackBoxGendreauHeuristic;
import neatsim.core.blackbox.neural.NeuralNetwork;
import neatsim.core.evaluators.gendreau.GendreauScenario;
import neatsim.core.evaluators.gendreau.simulators.GendreauSimulationTaskVisible;
import neatsim.util.NeuralNetworkReader;

import org.jppf.task.storage.DataProvider;
import org.jppf.task.storage.MemoryMapDataProvider;
import org.junit.Test;

import rinde.evo4mas.gendreau06.GSimulationTask.SolutionType;

public class ViewGenome {
	@Test
	public void viewGenome() throws Exception {
		final NeuralNetworkReader reader = new NeuralNetworkReader(3);
		final List<GendreauScenario> ss = GendreauScenario.load("data/", "req_rapide_1_240_24");
		final GendreauScenario s = ss.get(0);
		//final URL testNetwork = getClass().getResource("/dist.xml");
		final List<NeuralNetwork> networks = reader.readFile("mydata/dist.xml").neuralNetworks;
		//assertEquals(1,networks.size());
		final NeuralNetwork nn = networks.get(0);
		System.out.println(nn);

		//final BlackBoxGendreauHeuristic nnh = new BlackBoxGendreauHeuristic(nn);

		final BlackBox box = new BlackBox() {
			private double newval = 0;
			private double val = 0;
			@Override
			public void setInput(final int no, final double val) {
//				if (no==8) newval=val;
				if (no==8) newval=val;
			}

			@Override
			public void reset() {
				val=0; newval = 0;
			}

			@Override
			public boolean isValid() {
				return true;
			}

			@Override
			public double getOutput(final int no) {
				return val;
			}

			@Override
			public int getNumberOfOutputs() {
				return 1;
			}

			@Override
			public int getNumberOfInputs() {
				return 14;
			}

			@Override
			public String getId() {
				return "moomoo";
			}

			@Override
			public void activate() {
				val = newval;
			}
		};

		final BlackBoxGendreauHeuristic nnh = new BlackBoxGendreauHeuristic(nn);

		final GendreauSimulationTaskVisible task = new GendreauSimulationTaskVisible(
				"req_rapide_1_240_24", nnh, 10, -1, SolutionType.MYOPIC);
		final DataProvider dp = new MemoryMapDataProvider();
		dp.setValue(s.getName(),s.getScenario());
		task.setDataProvider(dp);
		task.run();
	}
}
