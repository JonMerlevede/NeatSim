package neatsim.core;

import java.util.ArrayList;
import java.util.List;

import neatsim.comm.thrift.CAuxFitnessInfo;
import neatsim.comm.thrift.CFitnessInfo;

public class FitnessInfo extends CFitnessInfo {
	private static final long serialVersionUID = -912194936153284719L;
	
	public static final FitnessInfo ZERO = new FitnessInfo(0,0);
	
	public FitnessInfo(double fitness, double alternativeFitness) {
		this.fitness = fitness;
		this.auxFitness = new ArrayList<>();
		this.auxFitness.add(new CAuxFitnessInfo("Alternative fitness", alternativeFitness));
	}
	public FitnessInfo(double fitness, List<CAuxFitnessInfo> auxFitness) {
		this.fitness = fitness;
		this.auxFitness = auxFitness;
	}
}
