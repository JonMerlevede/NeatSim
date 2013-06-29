package neatsim.core.evaluators.gendreau.simulators;

import java.util.Collection;


import org.jppf.task.storage.DataProvider;

import rinde.evo4mas.common.ResultDTO;

public interface Simulator {
	public Collection<ResultDTO> process(Collection<GendreauSimulationTask> tasks);
	public void setDataProvider(DataProvider dataProvider);
}
