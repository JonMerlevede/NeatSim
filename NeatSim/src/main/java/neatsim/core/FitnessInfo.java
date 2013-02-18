package neatsim.core;

import java.util.ArrayList;

import neatsim.thrift.CAuxFitnessInfo;
import neatsim.thrift.CFitnessInfo;

/**
 * Instances of this class carry information on fitness of a population, AND can
 * be transported over the Thrift protocol.
 * 
 * In comparison with the {@see CFitnessInfo} class, this class contains handy
 * constructor functions and static handles to fitness information objects which
 * should be re-used for efficiency.
 * 
 * Users are not meant to tread instances of this class as immutable classes.
 * Because this class is an extension of {@see CFitnessInfo}, users have access
 * to all of the properties stored inside, so it is unfortunately not possible
 * to enforce this.
 * 
 * @author Jonathan Merlevede
 */
public class FitnessInfo extends CFitnessInfo {
	private static final long serialVersionUID = -912194936153284719L;
	
	/**
	 * Handle to a fitness information object with zero fitness.
	 * 
	 * Although he underlying fitness info object is MUTABLE, as it is a Thrift
	 * object, it is _not_ actually allowed to change this object.
	 */
	public static final FitnessInfo ZERO = new FitnessInfo(0,0);
	
	/**
	 * Creates a fitness information object with the given fitness and
	 * alternative fitness.
	 * 
	 * @param fitness The fitness of this new fitness information object.
	 * @param alternativeFitness The alternative fitness of this new fitness
	 *           information object.
	 * @post The fitness of this new fitness information object is equal to the
	 *       given fitness.
	 * 
	 *       | getFitness() == fitness
	 * @post The alternative fitness of this new fitness information object is
	 *       equal to the given alternative fitness.
	 * 
	 *       | getAuxFitness().get(0).value == fitness
	 *       | && getAuxFitness().get(0).name = "Alternative fitness"
	 */
	public FitnessInfo(double fitness, double alternativeFitness) {
		assert Double.isInfinite(fitness) != true;
		assert Double.isNaN(fitness) != true;
		assert fitness >= 0;
		
		this.fitness = fitness;
		this.auxFitness = new ArrayList<>();
		this.auxFitness.add(new CAuxFitnessInfo("Alternative fitness", alternativeFitness));
	}
	
	public FitnessInfo(double fitness) {
		this(fitness, fitness);
	}
}
