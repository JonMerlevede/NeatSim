package neatsim.core.evaluators.gendreau;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import neatsim.core.FitnessInfo;
import neatsim.core.blackbox.BlackBox;
import neatsim.core.blackbox.BlackBoxGendreauHeuristic;
import neatsim.core.blackbox.neural.NeuralNetwork;
import neatsim.core.evaluators.PopulationEvaluator;
import neatsim.core.evaluators.gendreau.simulators.DistributedJPPFSimulator;
import neatsim.core.evaluators.gendreau.simulators.GendreauSimulationTask;
import neatsim.core.evaluators.gendreau.simulators.GendreauSimulationTaskSelfsufficient;
import neatsim.core.evaluators.gendreau.simulators.LocalMultithreadedSimulator;
import neatsim.core.evaluators.gendreau.simulators.LocalSinglethreadedSimulator;
import neatsim.core.evaluators.gendreau.simulators.Simulator;
import neatsim.core.fitnesstransformers.FitnessTransformer;
import neatsim.core.fitnesstransformers.ToppedAbsolute;
import neatsim.core.stopconditions.SimpleStopcondition;
import neatsim.core.stopconditions.Stopcondition;
import neatsim.server.thrift.CFastCyclicNetwork;
import neatsim.server.thrift.CFitnessInfo;
import neatsim.server.thrift.CPopulationFitness;
import neatsim.server.thrift.CPopulationInfo;
import neatsim.util.AssertionHelper;

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
public class GendreauEvaluator implements PopulationEvaluator {

	public enum InternalEvaluator {
		SINGLETHREADED,MULTITHREADED,DISTRIBUTED;
	}

	public static final int DEFAULT_NUMBER_OF_SCENARIOS_PER_GENERATION = 5;

	public static final int RANKING_PARAMETER = 2;
	public static final double STOP_FITNESS = 100000;
	private final SolutionType solutionType;
	private final JPPFClient jppfClient;
	private final Stopcondition stopcondition;
	private final FitnessTransformer fitnessTransformer;
	private final List<GendreauScenario> gendreauScenarios;
	private final Simulator evaluator;
	private final int numberOfScenariosPerGeneration;
	//private final int lowestFreshScenarioNumber = 0;

	public GendreauEvaluator(
			final List<GendreauScenario> gendreauScenarios,
			final SolutionType solutionType,
			final InternalEvaluator internalEvaluator) {
		this(gendreauScenarios,
				solutionType,
				internalEvaluator,
				new SimpleStopcondition(),
//				new GenerationsStopCondition(),
//				new Invert());
				new ToppedAbsolute(4000),
				DEFAULT_NUMBER_OF_SCENARIOS_PER_GENERATION);
//				new LinearRanking(RANKING_PARAMETER));
	}


	// Provided lists should be O(1) for .get()
	public GendreauEvaluator(
			// We need the name of the scenario files to make Gendreau06Scenario instances
			final List<GendreauScenario> gendreauScenarios,
			final SolutionType solutionType,
			final InternalEvaluator internalEvaluator,
			final Stopcondition stopcondition,
			final FitnessTransformer fitnessTransformer,
			final int numberOfScenariosPerGeneration) {

		assert AssertionHelper.isEffectiveCollection(gendreauScenarios);
		assert gendreauScenarios.size() > 0;
		this.gendreauScenarios = gendreauScenarios;

		if (solutionType != SolutionType.MYOPIC)
			throw new UnsupportedOperationException("Currently, only myopic vehicles are supported");
		assert(solutionType != null);
		this.solutionType = solutionType;

		assert(internalEvaluator != null);
		final Pair<Simulator,JPPFClient> t = createSimulator(internalEvaluator);
		evaluator = t.getValue0();
		jppfClient = t.getValue1();

		assert stopcondition != null;
		this.stopcondition = stopcondition;

		assert(fitnessTransformer != null);
		this.fitnessTransformer = fitnessTransformer;

		assert(numberOfScenariosPerGeneration > 0);
		this.numberOfScenariosPerGeneration = numberOfScenariosPerGeneration;
	}

	/**
	 * Adds tasks to the given list of tasks that evaluate the given genome for
	 * the scenarios corresponding to the given list of scenario numbers. Updates
	 * the given data provider so that it contains all necessary scenario
	 * information.
	 *
	 * @param tasks
	 *        The list of tasks to add tasks to.
	 * @param dataProvider
	 *        The given data provider to add necessary information to.
	 * @param scenarioNumbers
	 *        The scenario numbers to evaluate the genome for.
	 * @param genome
	 *        The genome to evaluate.
	 */
	private void addTasks(
			final List<GendreauSimulationTask> tasks,
			final DataProvider dataProvider,
			final int[] scenarioNumbers,
			final BlackBox genome) {
		// Get the scenarios to evaluate this heuristic for
		for (final int i : scenarioNumbers) {
			try {
				final String scenarioName = gendreauScenarios.get(i).getName();
				dataProvider.setValue(
						gendreauScenarios.get(i).getName(),
						gendreauScenarios.get(i).getScenario());
				// TODO make this a parameter
				final int numVehicles = scenarioName.contains("_450") ? 20 : 10;
				final GendreauSimulationTask task = new GendreauSimulationTask(
						scenarioName,
						new BlackBoxGendreauHeuristic(genome), // also 'clones' the ann
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

	private void addSelfsufficientTasks(
			final List<GendreauSimulationTask> tasks,
			final DataProvider dataProvider,
			final int[] scenarioNumbers,
			final BlackBox genome) {
		// Get the scenarios to evaluate this heuristic for
		for (final int i : scenarioNumbers) {
			try {
				final String scenarioName = gendreauScenarios.get(i).getName();
				// TODO make this nicer
				final int numVehicles = scenarioName.contains("_450") ? 20 : 10;
				final GendreauSimulationTask task = new GendreauSimulationTaskSelfsufficient(
						scenarioName,
						new BlackBoxGendreauHeuristic(genome), // also 'clones' the ann
						numVehicles,
						-1,
						solutionType,
						gendreauScenarios.get(i).getScenario());
				tasks.add(task);
			} catch (final Exception e) {
				e.printStackTrace();
				assert false;
			}
		}
	}

	private void addSelfsufficientTasks(
			final List<GendreauSimulationTask> tasks,
			final DataProvider dataProvider,
			final int[] scenarioNumbers,
			final List<? extends BlackBox> anns) {
		for (final BlackBox bb : anns) {
			addSelfsufficientTasks(tasks,dataProvider,scenarioNumbers,bb);
		}
	}

	private void addTasks(
			final List<GendreauSimulationTask> tasks,
			final DataProvider dataProvider,
			final int[] scenarioNumbers,
			final List<? extends BlackBox> anns) {
		for (final BlackBox bb : anns) {
			addTasks(tasks,dataProvider,scenarioNumbers,bb);
		}
	}

	public void close() {
		jppfClient.close();
	}

	private Pair<Simulator,JPPFClient> createSimulator(
			final InternalEvaluator computationStrategy) {
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

	/**
	 * Like {@see #evaluatePopulation(CPopulationInfo, int)}, using {@see
	 * #DEFAULT_NUMBER_OF_SCENARIOS_PER_GENERATION} for the number of scenarios
	 * per generation.
	 */
	@Override
	public CPopulationFitness evaluatePopulation(final CPopulationInfo populationInfo) {
		return evaluatePopulation(populationInfo, numberOfScenariosPerGeneration);
	}

	/**
	 * Evaluates the described population using the given number of scenarios per
	 * genome.
	 *
	 * @param populationInfo
	 *        Description of the population to evaluate
	 * @param numberOfScenariosPerGeneration
	 *        Number of scenarios to use for the evaluation of a genome
	 * @return Fitness of the described population.
	 */
	public CPopulationFitness evaluatePopulation(
			final CPopulationInfo populationInfo,
			final int numberOfScenariosPerGeneration) {
		assert populationInfo != null;

		final LinkedList<BlackBox> anns = new LinkedList<>();
		for (final CFastCyclicNetwork cfcn : populationInfo.getPhenomes()) {
			anns.add(new NeuralNetwork(cfcn));
		}
		final int generation = populationInfo.getGeneration();
		return evaluatePopulation(anns,generation,numberOfScenariosPerGeneration);
	}

	/**
	 * Like {@see #evaluatePopulation(List, int, int)}, using {@see
	 * #DEFAULT_NUMBER_OF_SCENARIOS_PER_GENERATION} for the number of scenarios
	 * per generation.
	 */
	public CPopulationFitness evaluatePopulation(
			final List<? extends BlackBox> anns,
			final int generation) {
		return evaluatePopulation(anns, generation, numberOfScenariosPerGeneration);
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
	public CPopulationFitness evaluatePopulation(
			final List<? extends BlackBox> anns,
			final int generation,
			final int numberOfScenariosPerGeneration) {
		assert AssertionHelper.isEffectiveCollection(anns);
		assert generation >= 0;
		assert numberOfScenariosPerGeneration >= 1;

		final ArrayList<GendreauSimulationTask> tasks = new ArrayList<>();
		final DataProvider dataProvider = new MemoryMapDataProvider();
		addTasks(tasks, dataProvider, getCurrentScenarioNumbers(generation,numberOfScenariosPerGeneration), anns);
		//addSelfsufficientTasks(tasks, dataProvider, getCurrentScenarioNumbers(generation,numberOfScenariosPerGeneration), anns);
		assert tasks.size() == anns.size() * numberOfScenariosPerGeneration;
		evaluator.setDataProvider(dataProvider);
		final Collection<ResultDTO> unprocessedResults = evaluator.process(tasks);
		assert unprocessedResults.size() == tasks.size();
		final List<CFitnessInfo> results =
				processResults(unprocessedResults, anns, generation, numberOfScenariosPerGeneration);
		assert results.size() == anns.size();
		final CPopulationFitness populationFitness = new CPopulationFitness(
				results,
				tasks.size());
		return populationFitness;
	}

	private int[] getCurrentScenarioNumbers(final int generation, final int numberOfScenariosPerGeneration) {
		final int[] result = new int[numberOfScenariosPerGeneration];
		final int base = (generation % getNumberOfScenarios())* numberOfScenariosPerGeneration % getNumberOfScenarios();
		//final int base = generation*numberOfScenariosPerGeneration;
		for (int i=0; i < numberOfScenariosPerGeneration; i++)
			result[i] = (base + i) % getNumberOfScenarios();
		return result;
	}

	private int getNumberOfScenarios() {
		return gendreauScenarios.size();
	}

	private void evaluateGenomesAddTasks(
			final List<GendreauSimulationTask> tasks,
			final DataProvider dataProvider,
			final List<? extends BlackBox> genomes,
			final int numberOfScenariosPerNonfinalGeneration,
			final int numberOfScenariosPerFinalGeneration,
			final boolean useDataProvider) {
		final Iterator<? extends BlackBox> genomeIt = genomes.iterator();
		int baseScenarioNumber = 0;
		while (genomeIt.hasNext()) {
			final BlackBox genome = genomeIt.next();
			final int numberOfScenarios = genomeIt.hasNext()
					? numberOfScenariosPerNonfinalGeneration
					: numberOfScenariosPerFinalGeneration;
			final int[] scenarioNumbers = new int[numberOfScenarios];
			for (int scenarioNumberOffset=0; scenarioNumberOffset < numberOfScenarios; scenarioNumberOffset++) {
				scenarioNumbers[scenarioNumberOffset] = (baseScenarioNumber+scenarioNumberOffset) % getNumberOfScenarios();
			}
			baseScenarioNumber += numberOfScenarios;
			if (useDataProvider)
				addTasks(tasks, dataProvider, scenarioNumbers, genome);
			else
				addSelfsufficientTasks(tasks, dataProvider, scenarioNumbers, genome);
		}
	}

	/**
	 * Groups together results with the same task data id (in our case: genome
	 * id's) into a multimap.
	 */
	private Multimap<String, ResultDTO> groupResults(final Collection<ResultDTO> results) {
		final Multimap<String, ResultDTO> gatheredFitnessValues = HashMultimap.create();
		for (final ResultDTO res : results) {
			final String scenarioName = res.getTaskDataId();// res.getComputationJob().((J)
			gatheredFitnessValues.put(scenarioName, res);
		}
		return gatheredFitnessValues;
	}

	public ResultDTO[][] evaluateGenomes(
			final List<? extends BlackBox> genomes,
			final int numberOfScenariosPerNonfinalGeneration,
			final int numberOfScenariosPerFinalGeneration,
			final boolean useDataProvider) {
		assert AssertionHelper.isEffectiveCollection(genomes);
		assert numberOfScenariosPerFinalGeneration > 0;
		assert numberOfScenariosPerNonfinalGeneration > 0;

		// Construct list of tasks
		final int expectedNumberOfTasks =
				(genomes.size()-1)*numberOfScenariosPerNonfinalGeneration +
				numberOfScenariosPerFinalGeneration;
		final List<GendreauSimulationTask> tasks = new ArrayList<GendreauSimulationTask>(expectedNumberOfTasks);
		final DataProvider dataProvider = new MemoryMapDataProvider();
		evaluateGenomesAddTasks(
				tasks,
				dataProvider,
				genomes,
				numberOfScenariosPerNonfinalGeneration,
				numberOfScenariosPerFinalGeneration,
				useDataProvider);
		assert tasks.size() == expectedNumberOfTasks;
		// Set dataprovider
		evaluator.setDataProvider(dataProvider);
		// Process tasks
		final Collection<ResultDTO> results = evaluator.process(tasks);
		// Group fitness values that belong to the same heuristics
		final Multimap<String, ResultDTO> gatheredFitnessValues = groupResults(results);
		assert gatheredFitnessValues.keySet().size() == genomes.size(); // one key for every genome
		assert gatheredFitnessValues.size() == expectedNumberOfTasks; // one result for every task
		// Put results together in a two-dimensional array of doubles
		int genomeCounter = 0;
		final ResultDTO[][] fitnesses = new ResultDTO[genomes.size()][];
		for (final BlackBox genome : genomes) {
			final Collection<ResultDTO> unorderedGenomeResults = gatheredFitnessValues.get(genome.getId());
			assert unorderedGenomeResults.size() == numberOfScenariosPerNonfinalGeneration ||
					 unorderedGenomeResults.size() == numberOfScenariosPerFinalGeneration;
			final List<ResultDTO> orderedGenomeResults = orderGenomeResults(unorderedGenomeResults);
			fitnesses[genomeCounter] = new ResultDTO[orderedGenomeResults.size()];
			int genomeResultCounter = 0;
			for (final ResultDTO genomeResult : orderedGenomeResults) {
				fitnesses[genomeCounter][genomeResultCounter] = genomeResult;
				genomeResultCounter++;
			}
			genomeCounter++;
		}
		// Return the constructed array
		return fitnesses;
	}

	private List<ResultDTO> orderGenomeResults(final Collection<ResultDTO> col) {
		final List<ResultDTO> list = Arrays.asList(col.toArray(new ResultDTO[0]));
		Collections.sort(list, new Comparator<ResultDTO>() {
			@Override
			public int compare(final ResultDTO o1, final ResultDTO o2) {
				return o1.scenarioKey.compareToIgnoreCase(o2.scenarioKey);
			}
		});
		return list;
	}

	/*
	 * processResults requires the list of heuristics to determine the order in
	 * which the fitness information needs to be returned, and the generation
	 * number to set the stop condition property of fitness information
	 */
	private List<CFitnessInfo> processResults(
			final Collection<ResultDTO> results,
			final List<? extends BlackBox> heuristics,
			final int generation,
			final int numberOfScenariosPerGeneration) {
		// Group fitness values that belong to the same heuristics
		final Multimap<String, ResultDTO> gatheredFitnessValues = groupResults(results);
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
					stopcondition.isSatistified(generation, fitness));
			fitnessInfos.add(info);
		}
		// Perform ranking
		//final FitnessTransformer transformer = new FitnessTransformer();
		//transformer.linearRankingOfCosts(fitnessInfos, RANKING_PARAMETER);
		//transformer.costToAbsoluteFitness(fitnessInfos);
		fitnessTransformer.transform(fitnessInfos);
		// Done!
		return fitnessInfos;
	}

//	public void setNumberOfScenariosPerGeneration(final int numberOfScenariosPerGeneration) {
//		assert numberOfScenariosPerGeneration > 0;
//		this.numberOfScenariosPerGeneration = numberOfScenariosPerGeneration;
//	}
//
//
//	// This could be made public (I think)
//	private void setSolutionType(final SolutionType solutionType) {
//		assert solutionType != null;
//		this.solutionType = solutionType;
//	}
}