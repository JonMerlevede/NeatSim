package neatsim;

import static junit.framework.Assert.assertEquals;

import java.net.URL;
import java.util.List;

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
		final URL testNetwork = getClass().getResource("/closest.xml");
		final List<NeuralNetwork> networks = reader.readFile(testNetwork).neuralNetworks;
		assertEquals(1,networks.size());
		final NeuralNetwork nn = networks.get(0);
		final BlackBoxGendreauHeuristic nnh = new BlackBoxGendreauHeuristic(nn);

		final GendreauSimulationTaskVisible task = new GendreauSimulationTaskVisible(
				"req_rapide_1_240_24", nnh, 10, -1, SolutionType.MYOPIC);
		final DataProvider dp = new MemoryMapDataProvider();
		dp.setValue(s.getName(),s.getScenario());
		task.setDataProvider(dp);
		task.run();
	}
}
