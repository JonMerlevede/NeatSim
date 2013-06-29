package neatsim.experiments.sim.test;

import java.io.IOException;

import neatsim.core.BlackBox;
import neatsim.experiments.sim.GendreauHeuristicProblem;
import rinde.sim.problem.common.StatsTracker.StatisticsDTO;
import rinde.sim.problem.gendreau06.Gendreau06ObjectiveFunction;
import rinde.sim.problem.gendreau06.Gendreau06Parser;
import rinde.sim.problem.gendreau06.Gendreau06Scenario;

public class MainTestSimulation {
	public static final String SCENARIO_NAME = "data/req_rapide_1_240_24";
	public static final int NUMBER_OF_VEHICLES = 5;
	
	public static void main(String[] args) throws IOException {
		Gendreau06Scenario scenario = Gendreau06Parser.parse(SCENARIO_NAME, NUMBER_OF_VEHICLES);
		System.out.println(scenario.getPossibleEventTypes().toString());
		BlackBox heur = new TestHeuristic();
		GendreauHeuristicProblem testGHP = new GendreauHeuristicProblem(scenario, heur);
		testGHP.enableUI();
		StatisticsDTO stats = testGHP.simulate();
		System.out.println("Stats: " + stats);
		System.out.println("Fitness: " + (15000 - (new Gendreau06ObjectiveFunction()).computeCost(stats)));

	}
	
	private static class TestHeuristic implements BlackBox {
		double dist = 0d;
		double queuedDist = 0d;
		double urge = 0;
		double queuedUrge = 0;
		
		@Override
		public void setInput(int no, double val) {
			assert no >= 0;
			assert no < getNumberOfInputs();
			if (no == 3)
				queuedDist = val;
			if (no == 4)
				queuedUrge = val;
		}
		@Override
		public double getOutput(int no) {
			assert no >= 0;
			assert no < getNumberOfOutputs();
			if (no < 0 || no > 1)
				throw new IndexOutOfBoundsException();
			return dist;
		}

		@Override
		public void activate() {
			dist = queuedDist;
			urge = queuedUrge;
		}

		@Override
		public void reset() {
			dist = 0;
			queuedDist = 0;
		}

		@Override
		public boolean isValid() {
			return true;
		}
		
		@Override
		public int getNumberOfInputs() {
			return 9;
		}
		
		@Override
		public int getNumberOfOutputs() {
			return 1;
		}
	}
}
