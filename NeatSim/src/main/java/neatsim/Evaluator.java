package neatsim;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import neatsim.core.blackbox.BlackBox;
import neatsim.core.evaluators.gendreau.GendreauEvaluator;
import neatsim.core.fitnesstransformers.Absolute;
import neatsim.core.stopconditions.SimpleStopcondition;
import rinde.evo4mas.common.ResultDTO;
import rinde.evo4mas.gendreau06.GSimulationTask.SolutionType;
import rinde.sim.problem.common.StatsTracker.StatisticsDTO;
//import rinde.evo4mas.gendreau06.GSimulationTask.SolutionType;

public class Evaluator {
	public enum Type { GENDREAU }
	private final EvaluatorConfig config;

	public Evaluator() throws IOException {
		config = new EvaluatorConfig();
		switch (config.getType()) {
		case GENDREAU:
			evaluateGendreau();
			return;
		}
		throw new RuntimeException();
	}

	private void evaluateGendreau() throws IOException {
		final EvaluatorGendreauConfig gconfig = new EvaluatorGendreauConfig(config);
		final GendreauEvaluator evaluator = new GendreauEvaluator(
				gconfig.getScenarios(),
				SolutionType.MYOPIC,
				gconfig.getInternalEvaluator(),
				new SimpleStopcondition(), // this does not matter
				new Absolute(), // this does not matter
				GendreauEvaluator.DEFAULT_NUMBER_OF_SCENARIOS_PER_GENERATION);
		config.getOutputFile().delete();
		if (config.isDry())
			return;

		final BufferedWriter writer = new BufferedWriter(new FileWriter(config.getOutputFile()));
		if (gconfig.getBatch()) { // batch mode
			System.out.println("Batch mode");
			System.out.println("Using data provider: " + gconfig.isUseDataprovider());
			final ResultDTO[][] results = evaluator.evaluateGenomes(
					config.getGenomes(),
					gconfig.getNumberOfScenariosInNonfinalGenerations(),
					gconfig.getNumberOfScenariosInFinalGeneration(),
					gconfig.isUseDataprovider());
			System.out.println("Results received, writing file");
			for (int m=0; m < results.length; m++) {
				for (int n=0; n < results[m].length; n++) {
					writer.append(resultDTOToString(results[m][n]));
					if (n + 1 < results[m].length)
						writer.append(",");
				}
				if (m + 1 < results.length)
					writer.newLine();
			}
			System.out.println("Done!");
		} else { // not in batch mode
			System.out.println("Not in batch mode");
			System.out.println("Using data provider: " + gconfig.isUseDataprovider());
			// We do not evaluate all genomes before writing; just in case something goes wrong...
			final ArrayList<BlackBox> genomeList = new ArrayList<>();
			final Iterator<BlackBox> it = config.getGenomes().iterator();
			while (it.hasNext()) {
				final BlackBox genome = it.next();
				genomeList.clear(); genomeList.add(genome);
				final ResultDTO[][] results = evaluator.evaluateGenomes(
						genomeList,
						gconfig.getNumberOfScenariosInNonfinalGenerations(),
						gconfig.getNumberOfScenariosInFinalGeneration(),
						gconfig.isUseDataprovider());
				assert results.length == 1;
				for (int i=0; i < results[0].length; i++) {
					writer.append(resultDTOToString(results[0][i]));
					if (i + 1 < results[0].length)
						writer.append(",");
				}
				if (it.hasNext())
					writer.newLine();
			}
		}
		writer.flush();
		writer.close();
		System.out.println("File closed");
	}

	private final Gendreau06ObjectiveFunction obj = new Gendreau06ObjectiveFunction();

	private String resultDTOToString(final ResultDTO results) {
		//final StatisticsDTO stats = results.s;
		final StatisticsDTO stats = results.stats;
		return obj.travelTime(stats) + ";"
				+ obj.tardiness(stats) + ";"
				+ obj.overTime(stats) + ";"
				+ obj.computeCost(stats);
	}
}
