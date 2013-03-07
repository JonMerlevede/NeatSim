package neatsim.core.evaluators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import neatsim.core.BlackBox;
import neatsim.core.BlackBoxHeuristic;
import neatsim.core.evaluators.simulators.DistributedJPPFSimulator;
import neatsim.core.evaluators.simulators.GendreauSimulationTask;
import neatsim.core.evaluators.simulators.LocalMultithreadedSimulator;
import neatsim.core.evaluators.simulators.LocalSinglethreadedSimulator;
import neatsim.core.evaluators.simulators.Simulator;
import neatsim.core.stopconditions.Stopcondition;
import neatsim.server.thrift.CFastCyclicNetwork;
import neatsim.server.thrift.CFitnessInfo;
import neatsim.server.thrift.CPopulationInfo;
import neatsim.server.thriftadapters.FastCyclicNeuralNetwork;
import neatsim.server.thriftadapters.FitnessInfo;
import neatsim.util.AssertionHelper;
import neatsim.util.FitnessTransformer;

import org.javatuples.Pair;
import org.jppf.client.JPPFClient;
import org.jppf.task.storage.DataProvider;
import org.jppf.task.storage.MemoryMapDataProvider;

import rinde.evo4mas.common.ResultDTO;
import rinde.evo4mas.gendreau06.GSimulationTask.SolutionType;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;


/*
 * This class is structured as follows.
 *
 * When a user calls evaluatePopulation, first, one job is created for each
 * simulation. There are numberOfScenariosPerGeneration number of simulations
 * per individual in the population.
 *
 * Jobs are collected into an unordered collection and dispatched to an internal
 * evaluator. This internal evaluator is either a LocalEvaluator or a
 * JPPFEvaluator, depending on the SolutionType the user provided when creating
 * this population evaluator.
 *
 * The DistributedEvaluator uses the JPPF framework to distribute jobs over a
 * grid of computers.
 *
 * The LocalEvaluators process all jobs on the local computer.
 *
 * After all jobs have finished, the internal evaluator returns a (unordered)
 * collection of ResultDTO objects its result. This result is then processed
 * using 'processResults'.
 *
 * To see to which result belongs to which individual in the population,
 * processResults compares the id of the internal heuristic in the ResultDTO
 * objects. Note that this means that different individuals that have the same
 * heuristic and are in the same population always have the same fitness values;
 * their fitness values are averaged over more instances than is the case for
 * heuristics that are not shared over multiple individuals.
 *
 * After determining the fitness of each individual, ranking can be applied (see
 * processReulsts).
 *
 * After processing, the result is returned.
 */
/**
 * This class represents a population evaluator for the PDP problem by performing
 * several RinSim simulations.
 *
 * @author Jonathan Merlevede
 *
 */
public class GendreauEvaluator {

	public enum ComputationStrategy {
		SINGLETHREADED,MULTITHREADED,DISTRIBUTED;
	}

	public static int RANKING_PARAMETER = 2;
	public static double STOP_FITNESS = 100000;
	private int numberOfScenariosPerGeneration;
	private SolutionType solutionType;
	private final JPPFClient jppfClient;
	private final Stopcondition stopCondition;
	private List<String> trainingScenarioNames;
	private List<String> trainingScenarios;
	private final Simulator evaluator;


	// Provided lists should be O(1) for .get()
	public GendreauEvaluator(
			// We need the name of the scenario files to make Gendreau06Scenario instances
			final List<String> trainingScenarioNames,
			final List<String> trainingScenarios,
			final int numberOfScenariosPerGeneration,
			final SolutionType solutionType,
			final ComputationStrategy computationStrategy,
			final Stopcondition stopCondition) {
		// Assert basic validity of function arguments
		assert stopCondition != null;
		this.stopCondition = stopCondition;

		if (solutionType != SolutionType.MYOPIC)
			throw new UnsupportedOperationException("Currently, only myopic vehicles are supported");

		setNumberOfScenariosPerGeneration(numberOfScenariosPerGeneration);
		setTrainingScenarios(trainingScenarioNames, trainingScenarios);
		setSolutionType(solutionType);
		final Pair<Simulator,JPPFClient> t = createSimulator(computationStrategy);
		evaluator = t.getValue0();
		jppfClient = t.getValue1();
	}

	private void addTasks(
			final List<GendreauSimulationTask> tasks,
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
				final GendreauSimulationTask task = new GendreauSimulationTask(
						scenarioName,
						new BlackBoxHeuristic(ann), // also 'clones' the ann
						numVehicles,
						-1,
						solutionType);
				tasks.add(task);
			} catch (final Exception e) {
				e.printStackTrace();
				assert false;
			}
		}
	}

	private void addTasks(
			final List<GendreauSimulationTask> tasks,
			final DataProvider dataprovider,
			final int generation,
			final List<? extends BlackBox> anns) {
		for (final BlackBox bb : anns) {
			addTasks(tasks,dataprovider,generation,bb);
		}
	}

	public void close() {
		jppfClient.close();
	}

	private Pair<Simulator,JPPFClient> createSimulator(
			final ComputationStrategy computationStrategy) {
		assert computationStrategy != null;
		switch (computationStrategy) {
		case SINGLETHREADED:
			return new Pair<Simulator, JPPFClient>(
					new LocalSinglethreadedSimulator(), null);
		case MULTITHREADED:
			return new Pair<Simulator, JPPFClient>(
					new LocalMultithreadedSimulator(), null);
		case DISTRIBUTED:
			final JPPFClient jppfClient = new JPPFClient();
			return new Pair<Simulator, JPPFClient>(
					new DistributedJPPFSimulator(jppfClient), jppfClient);
		}
		// This can never happen
		assert false;
		return null;
	}

	public List<CFitnessInfo> evaluatePopulation(final CPopulationInfo populationInfo) {
		assert populationInfo != null;

		final LinkedList<BlackBox> anns = new LinkedList<>();
		for (final CFastCyclicNetwork cfcn : populationInfo.getPhenomes()) {
			anns.add(new FastCyclicNeuralNetwork(cfcn));
		}
		final int generation = populationInfo.getGeneration();
		return evaluatePopulation(anns,generation);
	}

	/*
	 * The generation number is necessary for the following reasons.
	 *
	 * 1. For determining what scenarios to use.
	 *
	 * I would have done this differently (by keeping a counter in this
	 * evaluator), but it is important that this implementation behaves identical
	 * to Rinde's implementation.
	 *
	 * 2. For determining the stop condition (not very important)
	 */
	public List<CFitnessInfo> evaluatePopulation(
			final List<? extends BlackBox> anns, final int generation) {
		assert AssertionHelper.isEffectiveCollection(anns);
		assert generation >= 0;

		final ArrayList<GendreauSimulationTask> tasks = new ArrayList<>();
		final DataProvider dataProvider = new MemoryMapDataProvider();
		addTasks(tasks, dataProvider, generation, anns);
		assert tasks.size() == anns.size() * numberOfScenariosPerGeneration;
		evaluator.setDataProvider(dataProvider);
		final Collection<ResultDTO> unprocessedResults = evaluator.process(tasks);
		assert unprocessedResults.size() == tasks.size();
		final List<CFitnessInfo> results =
				processResults(unprocessedResults, anns, generation);
		assert results.size() == anns.size();
		return results;
	}

	private int[] getCurrentScenarioNumbers(final int generation) {
		final int[] result = new int[numberOfScenariosPerGeneration];
		final int base = generation*numberOfScenariosPerGeneration;
		for (int i=0; i < numberOfScenariosPerGeneration; i++)
			result[i] = (base + i) % getNumberOfTrainingScenarios();
		return result;
	}

	private int getNumberOfTrainingScenarios() {
		return trainingScenarioNames.size();
	}

	/*
	 * processResults requires the list of heuristics to determine the order in
	 * which the fitness information needs to be returned, and the generation
	 * number to set the stop condition property of fitness information
	 */
	private List<CFitnessInfo> processResults(
			final Collection<ResultDTO> results,
			final List<? extends BlackBox> heuristics,
			final int generation) {
		// Group fitness values that belong to the same heuristics
		final Multimap<String, ResultDTO> gatheredFitnessValues = HashMultimap.create();
		for (final ResultDTO res : results) {
			final String programString = res.getTaskDataId();// res.getComputationJob().((J)
			gatheredFitnessValues.put(programString, res);
		}
		// Calculate average fitness values
		final Map<String, Float> fitnessMap = new HashMap<>();
		for (final Entry<String, Collection<ResultDTO>> entry : gatheredFitnessValues.asMap().entrySet()) {
			final String heuristicId = entry.getKey();
			final Collection<ResultDTO> heuristicFitnesses = entry.getValue();
			// This is assertion is not true;
			// entry.getValue().size() is equal to the number of scenarios per
			// generation, multiplied by the number of individuals that share
			// a black box that has the exact same id.
			// assert entry.getValue().size() == numberOfScenariosPerGeneration;
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
		// Write average fitness values and satisfaction of stop condition to fitness info objects
		final List<CFitnessInfo> fitnessInfos = new LinkedList<>();
		for (final BlackBox heuristic : heuristics) {
			assert fitnessMap.containsKey(heuristic.getId());
			final double fitness = fitnessMap.get(heuristic.getId());
			final CFitnessInfo info = new FitnessInfo(
					fitness,
					stopCondition.isSatistified(generation, fitness));
			fitnessInfos.add(info);
		}
		// Perform ranking
		final FitnessTransformer ranker = new FitnessTransformer();
		ranker.linearRankingOfCosts(fitnessInfos, RANKING_PARAMETER);
		// Done!
		return fitnessInfos;
	}

	// This could be made public (i.e. the evaluator will still work correctly after changing)
	private void setNumberOfScenariosPerGeneration(final int numberOfScenariosPerGeneration) {
		assert numberOfScenariosPerGeneration > 0;
		this.numberOfScenariosPerGeneration = numberOfScenariosPerGeneration;
	}


	// This could be made public (I think)
	private void setSolutionType(final SolutionType solutionType) {
		assert solutionType != null;
		this.solutionType = solutionType;
	}

	// This could be made public (i.e. the evaluator will still work correctly after changing)
	private void setTrainingScenarios(
			final List<String> trainingScenarioNames,
			final List<String> trainingScenarios) {
		assert AssertionHelper.isEffectiveCollection(trainingScenarioNames);
		assert AssertionHelper.isEffectiveCollection(trainingScenarios);
		assert trainingScenarioNames.size() > 0;
		assert trainingScenarioNames.size() == trainingScenarios.size();
		this.trainingScenarioNames = trainingScenarioNames;
		this.trainingScenarios = trainingScenarios;
	}
}