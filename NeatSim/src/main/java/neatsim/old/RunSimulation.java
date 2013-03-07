package neatsim;

import static java.lang.System.out;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import neatsim.core.BlackBoxHeuristic;
import neatsim.core.FastCyclicNeuralNetwork;
import neatsim.sim.GendreauHeuristicProblem;
import neatsim.sim.neuralnets.NeuralNetworkFactory;
import rinde.sim.problem.gendreau06.Gendreau06ObjectiveFunction;
import rinde.sim.problem.gendreau06.Gendreau06Parser;
import rinde.sim.problem.gendreau06.Gendreau06Scenario;

public class RunSimulation {
	public static final String SCENARIO_NAME = "data/req_rapide_1_240_24";
	public static final int NUMBER_OF_VEHICLES = 10;
	public static final String FILE_NAME = new File(SCENARIO_NAME).getName();

	
	public static void main(String[] args) throws IOException {
		// Create the neural network we want to use as a heuristic.
		//FastCyclicNeuralNetwork fccn = (new NeuralNetworkFactory()).createDist();
		//FastCyclicNeuralNetwork fccn = (new NeuralNetworkFactory()).createDist2();
		FastCyclicNeuralNetwork fccn = (new NeuralNetworkFactory()).createClosest();
		// Wrap the neural network in a black box heuristic.
		BlackBoxHeuristic bbh = new BlackBoxHeuristic(fccn);
		// Create a Gendreau scenario
		BufferedReader bfr = new BufferedReader(new FileReader(SCENARIO_NAME));
		Gendreau06Scenario scenario = Gendreau06Parser.parse(bfr,FILE_NAME, NUMBER_OF_VEHICLES);
		// Start up a local Gendreau heuristic problem that uses this scenario heuristic.
		GendreauHeuristicProblem ghp = GendreauHeuristicProblem.create(scenario, bbh, false);
		// Start simulation
		out.println("Starting simulation");
		ghp.simulate();
		out.println("Simulation finished");
		out.println("Simulation statistics: " + ghp.getStatistics());
		out.println("Cost: " + (new Gendreau06ObjectiveFunction()).computeCost(ghp.getStatistics()));
	}
}