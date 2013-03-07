package neatsim;

import java.util.List;

import junit.framework.Assert;

import org.jppf.client.JPPFClient;
import org.jppf.client.JPPFJob;
import org.jppf.server.protocol.JPPFTask;
import org.junit.Test;

public class TestJPPF {
	public static class TestJPPFTask extends JPPFTask {
		/**
		 *
		 */
		private static final long serialVersionUID = 1091551606992497371L;

		@Override
		public void run() {
			setException(new Exception("Moooo"));
			setResult("Moomoo");
		}
	}
	@Test
	public void createTask() throws Exception {
		final JPPFClient client = new JPPFClient();
		final JPPFJob job = new JPPFJob();
		for (int i = 1; i < 10; i++) {
			job.addTask(new TestJPPFTask());
		}
		job.setBlocking(true);
		final List<JPPFTask> tasks =  client.submit(job);
		for (final JPPFTask task : tasks) {
			Assert.assertNotNull(task.getException());
		}
	}
}
