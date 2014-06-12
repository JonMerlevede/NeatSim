package neatsim.core.evaluators.gendreau.simulators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import neatsim.util.AssertionHelper;

import org.jppf.JPPFException;
import org.jppf.client.JPPFClient;
import org.jppf.client.JPPFJob;
import org.jppf.server.protocol.JPPFTask;
import org.jppf.task.storage.DataProvider;

import rinde.evo4mas.common.ResultDTO;

public class DistributedJPPFSimulator implements Simulator {
	protected int numberOfCalls = 0;
	protected DataProvider dataProvider;
	protected JPPFClient jppfClient;

	public DistributedJPPFSimulator(final JPPFClient jppfClient) {
		this.jppfClient = jppfClient;
	}

	@Override
	public Collection<ResultDTO> process(
			final Collection<GendreauSimulationTask> tasks) {
		assert AssertionHelper.isEffectiveCollection(tasks);
		assert dataProvider != null; // users need to call setDataProvider first

		assert jppfClient != null;
		//assert jppfClient.getClientConnection() != null;

		// JPPF also sets the dataProvider of all tasks in the job
		final JPPFJob job = new JPPFJob(dataProvider);
//		final String s = JPPFConfiguration.getProperties().getString("jppf.execution.policy");
//		ExecutionPolicy policy = null;
//      if (s != null) {
//        try {
//      	  PolicyParser.validatePolicy(s);
//      	  policy = PolicyParser.parsePolicy(s);
//        } catch (final JPPFException e) { // this happens if policy is invalid
//      	  e.printStackTrace(); assert false;
//        } catch (final Exception e) { // this should really never happen
//      	  e.printStackTrace(); assert false;
//        }
//      }
		job.setBlocking(true);
//		job.getSLA().setExecutionPolicy(policy); // notice that policy may be null
		// determine whether an execution policy should be used


		//job.getSLA().setSuspended(true);
		job.setName("call number " + numberOfCalls++);
		System.out.println("Adding tasks to job");
		for (final JPPFTask task : tasks) {
			try {
				job.addTask(task);
				//job.addTask(task);
				//job.addTask(new TestTask());
			} catch (final JPPFException e) { e.printStackTrace(); assert false; }
		}
		System.out.println("Submitting jobs to JPPFClient");
		try {
			final List<JPPFTask> jppfOutput = jppfClient.submit(job);
			assert jppfOutput != null;
			System.out.println("Received results from JPPFClient");
			final List<ResultDTO> resultDTOs = new ArrayList<>();
			for (final JPPFTask t : jppfOutput) {
				if (t.getException() != null) {
					System.out.println("Exception occured :(");
					throw t.getException();
				}
				assert t instanceof GendreauSimulationTask;
				resultDTOs.add(((GendreauSimulationTask) t).getComputationResult());
			}
			return resultDTOs;
		} catch (final Exception e) {
			e.printStackTrace();	assert false; return null;
		}
	}

	@Override
	public void setDataProvider(final DataProvider dataProvider) {
		assert dataProvider != null;
		this.dataProvider = dataProvider;
	}
}