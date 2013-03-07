package neatsim.core.evaluators.simulators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


import neatsim.util.AssertionHelper;

import org.jppf.task.storage.DataProvider;

import rinde.evo4mas.common.ResultDTO;

public class LocalMultithreadedSimulator implements Simulator {
	protected DataProvider dataProvider;

	@Override
	public Collection<ResultDTO> process(
			final Collection<GendreauSimulationTask> tasks) {
		assert AssertionHelper.isEffectiveCollection(tasks);
		assert dataProvider != null; // users need to call setDataProvider first

		final int threads = Runtime.getRuntime().availableProcessors();
		final ExecutorService service = Executors.newFixedThreadPool(threads);
		final List<Callable<ResultDTO>> callables = new ArrayList<>(
				tasks.size());

		for (final GendreauSimulationTask task : tasks) {
			final Callable<ResultDTO> callable = new Callable<ResultDTO>() {
				@Override
				public ResultDTO call() throws Exception {
					task.setDataProvider(dataProvider);
					task.run();
					return task.getComputationResult();
				}
			};
			callables.add(callable);
		}
		try {
			final List<Future<ResultDTO>> futures =
					service.invokeAll(callables);
			service.shutdown();
			assert futures.size() == tasks.size();
			final List<ResultDTO> fitnessInfos =
					new ArrayList<>(futures.size());
			for (final Future<ResultDTO> future : futures) {
				fitnessInfos.add(future.get());
			}

			assert tasks.size() == fitnessInfos.size();
			assert AssertionHelper.isEffectiveCollection(fitnessInfos);
			return fitnessInfos;
		} catch (final ExecutionException e) {
			throw new RuntimeException("Exception in execution of tasks: " + e);
		} catch (final InterruptedException e) {
			throw new RuntimeException("Interrupted: " + e);
		}
	}

	@Override
	public void setDataProvider(final DataProvider dataProvider) {
		assert dataProvider != null;
		this.dataProvider = dataProvider;
	}
}