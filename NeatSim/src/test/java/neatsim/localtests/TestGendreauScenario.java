package neatsim.localtests;

import java.io.IOException;
import java.util.List;

import neatsim.core.evaluators.gendreau.GendreauScenario;

import org.junit.Assert;
import org.junit.Test;

import rinde.evo4mas.common.ExperimentUtil;

public class TestGendreauScenario {
	@Test
	public void testOrder() {
		final List<GendreauScenario> s1 = GendreauScenario.load("data/", "_450_24");
		final List<GendreauScenario> s2 = GendreauScenario.load("data/", "_450_24");
		Assert.assertArrayEquals(s1.toArray(), s2.toArray());
		Math.random();
		Math.random();
		final List<GendreauScenario> s3 = GendreauScenario.load("data/", "_450_24");
		Assert.assertArrayEquals(s1.toArray(), s3.toArray());
	}

	@Test
	public void testContents() throws IOException {
		final List<GendreauScenario> s1 = GendreauScenario.load("data/", "_450_24");
		Assert.assertEquals(5, s1.size());
		for (int i = 1; i <= s1.size(); i++) {
			Assert.assertEquals(s1.get(i-1).getScenario(),
					ExperimentUtil.textFileToString("data/req_rapide_"+i+"_450_24"));
		}

	}
}
