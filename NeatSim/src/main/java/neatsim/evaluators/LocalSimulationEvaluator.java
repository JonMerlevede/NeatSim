package neatsim.evaluators;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import neatsim.core.BlackBox;
import neatsim.core.BlackBoxHeuristic;
import neatsim.core.FastCyclicNeuralNetwork;
import neatsim.core.FitnessInfo;
import neatsim.sim.GendreauHeuristicProblem;
import neatsim.thrift.CFastCyclicNetwork;
import neatsim.thrift.CFitnessInfo;
import neatsim.thrift.CPopulationFitness;
import neatsim.thrift.CPopulationInfo;
import rinde.sim.problem.common.StatsTracker.StatisticsDTO;
import rinde.sim.problem.gendreau06.Gendreau06ObjectiveFunction;
import rinde.sim.problem.gendreau06.Gendreau06Parser;
import rinde.sim.problem.gendreau06.Gendreau06Scenario;

// TODO document thoroughly
public class LocalSimulationEvaluator {
	public static final double STOP_FITNESS = 3000;
	public static final String SCENARIO_NAME = "data/req_rapide_1_240_24";
	public static final int NUMBER_OF_VEHICLES = 10;
	public static final String FILE_NAME = new File(SCENARIO_NAME).getName();
	public static final Gendreau06ObjectiveFunction OBJECTIVE_FUNCTION = new Gendreau06ObjectiveFunction();

	public LocalSimulationEvaluator() {
		// Empty
	}
	
	private class Counter {
		private int counter = 0;
		public int getCount() {
			return counter;
		}
		public void increaseCount() {
			counter++;
		}
		@Override
		public String toString() {
			return "" + counter;
		}
	}
	
	private String getScenarioString() {
		BufferedReader fr = null;
		StringBuilder sb = null;
		try {
			fr = new BufferedReader(new FileReader(SCENARIO_NAME));
			sb = new StringBuilder();
			String line;
			while ((line = fr.readLine()) != null)
				sb.append(line).append("\n");
			fr.close();
		} catch (IOException e) {throw new RuntimeException("IO exception.");}
		return sb.toString();
	}
	
	public CPopulationFitness parallelEvaluatePopulation(CPopulationInfo pi) {
		assert pi != null;
		
		final Counter evaluationCounter = new Counter();
		final String scenarioString = getScenarioString();
		
		int threads = Runtime.getRuntime().availableProcessors();
		//int threads = 1;
	    ExecutorService service = Executors.newFixedThreadPool(threads);
	    List<Callable<CFitnessInfo>> callables = new ArrayList<>(pi.getPhenomes().size());
	    
	    for (final CFastCyclicNetwork input : pi.getPhenomes()) {
	    	Callable<CFitnessInfo> callable = new Callable<CFitnessInfo>() {
				@Override
				public CFitnessInfo call() throws Exception {
					evaluationCounter.increaseCount();
					System.out.println("Evaluation number " + evaluationCounter);
					return evaluatePhenotype(input, new BufferedReader(new StringReader(scenarioString)));
				}
			};
			callables.add(callable);
	    }
	    List<Future<CFitnessInfo>> futures = null;
	    List<CFitnessInfo> fitnessInfos = null;
	    try {
			futures = service.invokeAll(callables);
			service.shutdown();
			//service.awaitTermination(2, TimeUnit.MINUTES);
			fitnessInfos = new ArrayList<>(futures.size());
			for (Future<CFitnessInfo> future : futures) {
				fitnessInfos.add(future.get());
			}
		} catch (ExecutionException e) {
			throw new RuntimeException("Concurrency exception: " + e);
		} catch (InterruptedException e) {
			throw new RuntimeException("Interrupted: " + e);
		}
	    //linearRanking(fitnessInfos, 1.6);
	    absoluteCosts(fitnessInfos);
	    CPopulationFitness pf = new CPopulationFitness();
		pf.setFitnessInfos(fitnessInfos);
		pf.setEvaluationCount(evaluationCounter.getCount());
		return pf;
	}
	
	public void absoluteCosts(List<CFitnessInfo> infos) {
		assert infos != null;
		
		for (CFitnessInfo i : infos) {
			i.setFitness(5000 - i.getFitness());
		}
	}
	
	public void linearRanking(final List<CFitnessInfo> infos, double selectivePressure) {
		assert infos != null;
		assert selectivePressure > 0 && selectivePressure <= 2;
		
		final Integer[] ranksMinusOne = new Integer[infos.size()];
		for (int i = 0; i < infos.size(); i ++) {
			ranksMinusOne[i] = i;
		}
		Arrays.sort(ranksMinusOne, new Comparator<Integer>() {
			@Override
			public int compare(Integer arg0, Integer arg1) {
				if (infos.get(arg0).getFitness() < infos.get(arg1).getFitness())
					return 1;
				if (infos.get(arg0).getFitness() == infos.get(arg1).getFitness())
					return 0;
				return -1;
			}
		});
		// This prints out sorted fitness values
//		for (int i = 0; i < infos.size(); i++)
//			System.out.println(""+infos.get(ranksMinusOne[i]).getFitness());
		int sizeMinusOne = infos.size() -1 ;
		for (int i = 0; i < infos.size(); i++) {
			double rankingFitness =
					2 - selectivePressure +
					2*(selectivePressure - 1)*(i)/sizeMinusOne; 
			rankingFitness = rankingFitness*rankingFitness + 1;
			infos.get(ranksMinusOne[i]).setFitness(rankingFitness);
			// This prints out sorted distance (descending) and fitness values (ascending)
//			System.out.println("Distance: "
//					+ infos.get(ranksMinusOne[i]).getAuxFitness().get(0).getValue()
//					+ ", fitness: "
//					+ infos.get(ranksMinusOne[i]).getFitness()
//					+ ", rank: "
//					+ i);
		}
	}
	
	public CPopulationFitness evaluatePopulation(CPopulationInfo pi) {
		assert pi != null;
		
		int evaluationCount = 0;
		int n = pi.getPhenomes().size();
//		BufferedReader fr = null;
//		try {
//			fr = new BufferedReader(new FileReader(SCENARIO_NAME));
//		} catch (FileNotFoundException e) {}
	    String scenarioString = getScenarioString();
	    System.out.println("Scenario string: " + scenarioString);
	    
		List<CFitnessInfo> fitnessInfos = new ArrayList<CFitnessInfo>(n);
		for (int i = 0; i < pi.getPhenomes().size(); i++) {
			evaluationCount++;
			System.out.println("Evaluation count: " + evaluationCount);
			BufferedReader bfr = new BufferedReader(new StringReader(scenarioString));
			CFitnessInfo fi = evaluatePhenotype(pi.getPhenomes().get(i),bfr);
			fitnessInfos.add(i, fi);
			if (fi.stopConditionSatisfied) {
				System.out.println("Done!");
				break;
			}
		}
		CPopulationFitness pf = new CPopulationFitness();
		pf.setFitnessInfos(fitnessInfos);
		pf.setEvaluationCount(evaluationCount);
		return pf;
	}
	
	public CFitnessInfo evaluatePhenotype(CFastCyclicNetwork ann, BufferedReader bfr) {
		assert ann != null;
		assert bfr != null;
		
		FastCyclicNeuralNetwork fcn = new FastCyclicNeuralNetwork(ann);
		return evaluatePhenotype(fcn, bfr);
	}
	
	public CFitnessInfo evaluatePhenotype(BlackBox box, BufferedReader bfr) {
		assert box != null;
		assert bfr != null;
		
		Gendreau06Scenario scenario = null;
		StatisticsDTO sdto = null;
		try {
			//fr.reset();
			scenario = Gendreau06Parser.parse(bfr,FILE_NAME, NUMBER_OF_VEHICLES);
			//scenario = Gendreau06Parser.parse(SCENARIO_NAME, NUMBER_OF_VEHICLES);
			System.out.println(scenario.getPossibleEventTypes().toString());
			BlackBoxHeuristic bbh = new BlackBoxHeuristic(box);
			sdto = (GendreauHeuristicProblem.create(scenario, bbh)).simulate();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("IO exception");
		}
		double cost = OBJECTIVE_FUNCTION.computeCost(sdto);
		//double fitness = 15000 - cost;
		System.out.println("Cost: " + cost);
		FitnessInfo fi = new FitnessInfo(cost, cost);
		fi.setStopConditionSatisfied(cost < 500);
		return fi;
	}
}
