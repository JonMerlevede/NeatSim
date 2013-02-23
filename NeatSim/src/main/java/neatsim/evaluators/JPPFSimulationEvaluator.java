package neatsim.evaluators;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import neatsim.core.BlackBox;
import neatsim.core.BlackBoxHeuristic;
import neatsim.core.FastCyclicNeuralNetwork;
import neatsim.core.FitnessInfo;
import neatsim.thrift.CFastCyclicNetwork;
import neatsim.thrift.CFitnessInfo;
import neatsim.thrift.CPopulationInfo;

import org.javatuples.Pair;
import org.jppf.client.JPPFClient;
import org.jppf.task.storage.DataProvider;
import org.jppf.task.storage.MemoryMapDataProvider;

import rinde.ecj.GPEvaluator.ComputationStrategy;
import rinde.evo4mas.common.ResultDTO;
import rinde.evo4mas.gendreau06.GSimulationTask;
import rinde.evo4mas.gendreau06.GSimulationTask.SolutionType;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class JPPFSimulationEvaluator {
	private static final long serialVersionUID = 4489397479555560638L;
	public static double STOP_FITNESS = 100000;

	private File trainingSetDirectory;
	private int numberOfScenariosPerGeneration;
	private int numberOfVehicles;
	private SolutionType solutionType;
	private final JPPFClient jppfClient;
	private final Stopcondition stopCondition;

	private ArrayList<String> trainingScenarioNames;
	private ArrayList<String> trainingScenarios;

	private interface Evaluator {
		Collection<ResultDTO> process(Collection<GSimulationTask> tasks);
	}

	private class LocalEvaluator implements Evaluator {
		@Override
		public Collection<ResultDTO> process(final Collection<GSimulationTask> tasks) {
			// TODO
			return null;
		}
	}

	private class JPPEEvaluator implements Evaluator {
		@Override
		public Collection<ResultDTO> process(final Collection<GSimulationTask> tasks) {
			// TODO
			return null;
		}
	}

	private final Evaluator evaluator;



	public JPPFSimulationEvaluator(
			final ArrayList<String> trainingScenarioNames,
			final ArrayList<String> trainingScenarios, // we do not ask simply for the contents of the scenario files
			final int numberOfScenariosPerGeneration,  // because we need the name of the scenario files to
			final int numberOfVehicles,					 // make Gendreau06Scenario instances
			final String scenarioSuffix,
			final SolutionType solutionType,
			final ComputationStrategy computationStrategy,
			final Stopcondition stopCondition) {
		// Assert basic validity of function arguments
		assert stopCondition != null;
		this.stopCondition = stopCondition;

		if (solutionType != SolutionType.MYOPIC)
			throw new UnsupportedOperationException("Currently, only myopic vehicles are supported");

		setNumberOfScenariosPerGeneration(numberOfScenariosPerGeneration);
		setNumberOfVehicles(numberOfVehicles);
		setTrainingScenarios(trainingScenarioNames, trainingScenarios);
		setSolutionType(solutionType);
		final Pair<Evaluator,JPPFClient> t = createEvaluator(computationStrategy);
		evaluator = t.getValue0();
		jppfClient = t.getValue1();
	}

	private Pair<Evaluator,JPPFClient> createEvaluator(final ComputationStrategy computationStrategy) {
		assert computationStrategy != null;
		switch (computationStrategy) {
		case LOCAL:
			return new Pair<Evaluator, JPPFClient>(new LocalEvaluator(), null);
		case DISTRIBUTED:
			return new Pair<Evaluator, JPPFClient>(new JPPEEvaluator(), new JPPFClient());
		}
		// This can never happen
		assert false;
		return null;
	}

	// This could be made public (i.e. the evaluator will still work correctly after changing)
	private void setNumberOfScenariosPerGeneration(final int numberOfScenariosPerGeneration) {
		assert numberOfScenariosPerGeneration > 0;
		this.numberOfScenariosPerGeneration = numberOfScenariosPerGeneration;
	}

	private void setNumberOfVehicles(final int numberOfVehicles) {
		assert numberOfVehicles > 0;
		this.numberOfVehicles = numberOfVehicles;
	}

	// This could be made public (i.e. the evaluator will still work correctly after changing)
	private void setTrainingScenarios(
			final ArrayList<String> trainginScenarioNames,
			final ArrayList<String> trainingScenarios) {
		assert trainingScenarioNames != null;
		assert trainingScenarios != null;
		assert trainingScenarioNames.size() > 0;
		assert trainingScenarioNames.size() == trainingScenarios.size();
		this.trainingScenarioNames = trainingScenarioNames;
		this.trainingScenarios = trainingScenarios;
	}

	// This could be made public (I think)
	private void setSolutionType(final SolutionType solutionType) {
		assert solutionType != null;
		this.solutionType = solutionType;
	}

	public List<CFitnessInfo> evaluatePopulation(final CPopulationInfo populationInfo) {
		final LinkedList<BlackBox> anns = new LinkedList<>();
		for (final CFastCyclicNetwork cfcn : populationInfo.getPhenomes()) {
			anns.add(new FastCyclicNeuralNetwork(cfcn));
		}
		final int generation = populationInfo.getGeneration();
		return evaluatePopulation(anns,generation);
	}

	public List<CFitnessInfo> evaluatePopulation(
			final List<BlackBox> anns,
			final int generation) {
		final LinkedList<GSimulationTask> tasks = new LinkedList<>();
		final DataProvider dataProvider = new MemoryMapDataProvider();
		addTasks(tasks, dataProvider, generation, anns);
		final Collection<ResultDTO> results = evaluator.process(tasks);
		return processResults(results, anns, generation);
	}

	private int getNumberOfTrainingExamples() {
		return trainingScenarioNames.size();
	}

	private List<CFitnessInfo> processResults(
			final Collection<ResultDTO> results,
			final List<BlackBox> heuristics,
			final int generation) {
		final Multimap<String, ResultDTO> gatheredFitnessValues = HashMultimap.create();
		for (final ResultDTO res : results) {
			final String programString = res.getTaskDataId();// res.getComputationJob().((J)
			gatheredFitnessValues.put(programString, res);
		}
		final Map<String, Float> fitnessMap = new HashMap<>();
		for (final Entry<String, Collection<ResultDTO>> entry : gatheredFitnessValues.asMap().entrySet()) {
			final String heuristicId = entry.getKey();
			final Collection<ResultDTO> heuristicFitnesses = entry.getValue();
			assert entry.getValue().size() == numberOfScenariosPerGeneration;
			float sumOfFitnesses = 0f;
			for (final ResultDTO result : heuristicFitnesses) {
				if (result.getFitness() == Float.MAX_VALUE) {
					sumOfFitnesses = Float.MAX_VALUE;
					break;
				} else {
					sumOfFitnesses += result.getFitness();
				}
			}
			final float averageFitness = sumOfFitnesses == Float.MAX_VALUE
					? Float.MAX_VALUE
					: sumOfFitnesses / numberOfScenariosPerGeneration;
			fitnessMap.put(heuristicId, averageFitness);
		}
		final List<CFitnessInfo> fitnessInfos = new LinkedList<>();
		for (final BlackBox heuristic : heuristics) {
			final double fitness = fitnessMap.get(heuristic.getId());
			final CFitnessInfo info = new FitnessInfo(
					fitness,
					stopCondition.isSatistified(generation, fitness));
			fitnessInfos.add(info);
		}
		return fitnessInfos;
	}

	private void addTasks(
			final List<GSimulationTask> tasks,
			final DataProvider dataprovider,
			final int generation,
			final List<BlackBox> anns) {
		for (final BlackBox bb : anns) {
			addTasks(tasks,dataprovider,generation,bb);
		}
	}

	private int[] getCurrentScenarioNumbers(final int generation) {
		final int[] result = new int[numberOfScenariosPerGeneration];
		final int base = generation*numberOfScenariosPerGeneration;
		for (int i=0; i < numberOfScenariosPerGeneration; i++)
			result[i] = (base + i) % getNumberOfTrainingExamples();
		return result;
	}


	private void addTasks(
			final List<GSimulationTask> tasks,
			final DataProvider dataProvider,
			final int generation,
			final BlackBox ann) {
		// Get the scenarios to evaluate this heuristic for
		for (final int i : getCurrentScenarioNumbers(generation)) {
			try {
				final String scenarioName = trainingScenarioNames.get(i);
				dataProvider.setValue(
						trainingScenarioNames.get(i),
						trainingScenarios.get(i));
				// TODO make this nicer
				final int numVehicles = scenarioName.contains("_450") ? 20 : 10;
				final GSimulationTask job = new GSimulationTask(
						scenarioName,
						new BlackBoxHeuristic(ann), // also 'clones' the ann
						numVehicles,
						-1,
						solutionType);
				tasks.add(job);
			} catch (final Exception e) {	e.printStackTrace();	}
		}
	}

	public void close() {
		jppfClient.close();
	}
}