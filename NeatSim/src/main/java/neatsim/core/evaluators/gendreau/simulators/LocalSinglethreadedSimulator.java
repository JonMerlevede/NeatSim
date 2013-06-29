package neatsim.core.evaluators.gendreau.simulators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


import neatsim.util.AssertionHelper;

import org.jppf.task.storage.DataProvider;

import rinde.evo4mas.common.ResultDTO;

public class LocalSinglethreadedSimulator implements Simulator {
	protected DataProvider dataProvider;

	@Override
	public Collection<ResultDTO> process(
			final Collection<GendreauSimulationTask> tasks) {
		assert AssertionHelper.isEffectiveCollection(tasks);
		assert dataProvider != null; // users need to call setDataProvider first

		final List<ResultDTO> fitnessInfos = new ArrayList<>(tasks.size());
		for (final GendreauSimulationTask task : tasks) {
			task.setDataProvider(dataProvider);
			task.run();
			fitnessInfos.add(task.getComputationResult());
		}
		return fitnessInfos;
	}

	@Override
	public void setDataProvider(final DataProvider dataProvider) {
		assert dataProvider != null;
		this.dataProvider = dataProvider;
	}
}
