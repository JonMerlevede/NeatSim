package neatsim.core;

/**
 * Interface to a black box.
 * 
 * Black boxes have a number of inputs and a number of outputs. Outputs are
 * calculated from the current but also possibly previous inputs (i.e. black
 * boxes may contain state). Black boxes should be used as follows.
 * <ul>
 * <li> Set inputs using {@link #setInput(int, double)}.
 * <li> Call {@link #activate()}.
 * <li> Read outputs using {@link #getOutput(int)}.
 * </ul>
 * 
 * As mentioned before, some black boxes may have state. Calling
 * {@link #reset()} sets the state of the black box to the value it has upon
 * initialisation.
 * 
 * @author Jonathan Merlevede
 */
public interface BlackBox {
	void setInput(int no, double val);
	/**
	 * Returns the value of output number no.
	 * 
	 * @param no The number of the output to be queried.
	 * @pre The queried output exists.
	 * 	| 0 <= no < getNumberOfOutputs()
	 * @pre The black box is in a valid state.
	 * 	| isValid()
	 * @return The value of the output.
	 */
	double getOutput(int no);
	
	/**
	 * Returns the number of inputs of this black box.
	 * @invar The number of inputs is positive
	 * 	| getNumberOfInputs() >= 0
	 */
	int getNumberOfInputs();
	
	/**
	 * Returns the number of outputs of this black box.
	 * @invar The number of outputs is positive
	 * 	| getNumberOfOutputs() >= 0
	 */
	int getNumberOfOutputs();
	
	/**
	 * Activates the black box.
	 * 
	 * This 'connects' the (possibly changed) input values through the black box,
	 * possibly changing its outputs. Also see the class description.
	 */
	void activate();
	
	/**
	 * Resets the black box by setting its state back to the state it had upon
	 * initialisation.
	 */
	void reset();
	
	/**
	 * Returns whether the black box is valid.
	 */
	boolean isValid();
}
