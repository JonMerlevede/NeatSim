package neatsim.experiments.sim;

import java.io.IOException;

import rinde.sim.problem.gendreau06.Gendreau06Parser;
import rinde.sim.problem.gendreau06.Gendreau06Scenario;

public class Main {
	public static final String SCENARIO_NAME = "data/req_rapide_1_240_24";
	//public static final String SCENARIO_NAME = "C:\\Dropbox\\univ\\ma2\\thesis\\problems\\output\\req_rapide_1_240_24";
	public static final int NUMBER_OF_VEHICLES = 5;
	
	public static void main(String[] args) throws IOException {
		Gendreau06Scenario scenario = Gendreau06Parser.parse(SCENARIO_NAME, NUMBER_OF_VEHICLES);
		System.out.println(scenario.getPossibleEventTypes().toString());
		(new GendreauProblem(scenario)).simulate();
	}
}
