package neatsim.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import neatsim.thrift.CConnection;
import neatsim.thrift.CFastCyclicNetwork;

/**
 * This class represents a cyclic neural network.
 *
 * Neural nets are evaluated by activating each neuron a specified maximum
 * number of times. It is possible that the network returns values of neurons
 * before they have converged, or that the network returns values of neurons in
 * networks that do not converge at all.
 *
 * The evaluation of neurons is done in an efficient way, sacrificing nice
 * object oriented structures for speed.
 *
 * To see how to use the cyclic neural network, see the documentation of
 * {@link BlackBox}.
 *
 * The implementation of this class draws heavily on the implementation of 'fast
 * cyclic neural network' in SharpNeatV2 by Colin Green.
 *
 * @author Jonathan Merlevede
 */
public class FastCyclicNeuralNetwork implements BlackBox {
	private static final FunctionLibrary FL = FunctionLibrary.INSTANCE;

	/**
	 * Array of connections between neurons.
	 * Neurons are identified by their number.
	 */
	private final List<CConnection> connectionArray;

	/**
	 * Array of activation functions.
	 */
	private final List<String> neuronActivationFnArray;

	/**
	 * Array of auxiliary arguments to these functions.
	 * (These are not currently used)
	 */
	private final List<List<Double>>neuronAuxArgsArray;

	/**
	 * Array of activations before activation.
	 *
	 * @see #inputNeuronCount
	 */
	private final List<Double> neuronInputArray;

	/**
	 * Array of activations after activation.
	 *
	 * @see #inputNeuronCount
	 */
	private final List<Double> neuronOutputArray;

	/**
	 * The number of input neurons.
	 *
	 * Neurons are not stored as neuron objects, but are instead identified by an
	 * integer identifier. Their activation values are stored in two 'flat'
	 * lists: {@link #neuronInputArray} and {@link #neuronOutputArray}.
	 *
	 * Node identifiers are structured as follows.
	 * <ol>
	 * <li>This first neuron (neuron number zero) is the bias node. Its first
	 * neurons (neuron number zero) is fixed, and set to 1.
	 * <li>The nodes with identifiers in the interval [1,inputNeuronCount] map
	 * onto the input nodes 0 to inputNeuronCount-1]
	 * <li>The nodes with identifiers in the interval [inputNeuronCount+1,
	 * inputNeuronCount+outputNeuronCount] map onto the output nodes 0 to
	 * outputNeuronCount - 1.
	 * <li>The nodes with other identifiers correspond to hidden nodes. These
	 * nodes are required only for activation of the CANN, and remain internal
	 * to this class and entirely hidden from the user.
	 * </ol>
	 */
	private final int inputNeuronCount;

	/**
	 * The number of output neurons.
	 *
	 * @see #inputNeuronCount
	 */
	private final int outputNeuronCount;

	/**
	 * The maximum number of time steps per activation.
	 */
	private final int timestepsPerActivation;

	private final String id;

	public boolean validConnections(final int numberOfNeurons, final List<CConnection> cons) {
		assert cons != null;
		for (final CConnection con : cons) {
			if (con.fromNeuronId >= numberOfNeurons
					|| con.toNeuronId >= numberOfNeurons)
				return false;
		}
		return true;
	}

	/**
	 * Creates a new FCNN (fast cyclic neural network) with the properties and
	 * functionality specified by the given fast cyclic neural network in Thrift
	 * format.
	 *
	 * After creation, changes to the provided CFCN are not reflected in the
	 * created FCNN.
	 *
	 * The bias node has id zero, and is not part of the input neuron count
	 * specified by the given FCNN.
	 *
	 * There are more similarities than can be seen from this constructor's
	 * postconditions.
	 *
	 * @param cfcn The FCNN in Thrift format.
	 * @pre The given Thrift FCNN is effective.
	 *
	 *      | cfcn != null
	 * @pre In the given FCNN, the number of neurons in the network is at least
	 *      zero.
	 *
	 *      | cfcn.getNeuronCount() >= 0
	 * @pre In the given FCNN, there are as least as many neurons as there are
	 *      input and output neurons.
	 *
	 *      | cfcn.getInputNeuronCount() + cfcn.getOutputNeuronCount( <=
	 *      | cfcn.getNeuronCount()
	 * @post This FCNN has the same number of input neurons as the given FCNN in
	 *       Thrift format.
	 *
	 *       | cfcn.getInputNeuronCount() == getNumberOfInputs()
	 * @pre The connections specified by the given FCNN are valid.
	 *
	 *      | validConnections(numberOfNeurons, cfcn.getConnections())
	 *

	 * @post This FCNN has the same number of output neurons as the given FCNN in
	 *       Thrift format.
	 *
	 *       | cfcn.getOutputNeuronCount() == getNumberOfOutputs()
	 */
	public FastCyclicNeuralNetwork(final CFastCyclicNetwork cfcn) {
		assert cfcn != null;
		final int numberOfNeurons = cfcn.getNeuronCount();
		assert numberOfNeurons >= 0;
		assert cfcn.getInputNeuronCount() + cfcn.getOutputNeuronCount() <= cfcn.getNeuronCount();
		assert numberOfNeurons == cfcn.getActivationFunctions().size();
		assert numberOfNeurons == cfcn.getNeuronAuxArgs().size();
		assert validConnections(numberOfNeurons, cfcn.getConnections());

		// Note: the bias node is already present in the given CFCN network.

		// Deep cloning of the given connection array
		//connectionArray = cfcn.getConnections();
		connectionArray =	new ArrayList<>(cfcn.getConnections().size());
		for (final CConnection c : cfcn.getConnections()) {
			connectionArray.add(
					new CConnection(
							c.getFromNeuronId(),
							c.getToNeuronId(),
							c.getWeight()));
		}

		// Deep cloning of the given neuron activation function array.
		//neuronActivationFnArray = cfcn.getActivationFunctions();
		neuronActivationFnArray = new ArrayList<>(cfcn.getActivationFunctions());

		// Deep cloning of the list of auxiliary function arguments.
		//neuronAuxArgsArray = cfcn.getNeuronAuxArgs();
		neuronAuxArgsArray = new ArrayList<>(cfcn.getNeuronAuxArgs().size());
		for (final List<Double> e : cfcn.getNeuronAuxArgs())
			neuronAuxArgsArray.add(new ArrayList<Double>(e));

		// Initialise arrays of the correct size with zero values.
		neuronInputArray = new ArrayList<Double>(
				Collections.nCopies(cfcn.getNeuronCount(), 0.0));
		neuronOutputArray = new ArrayList<Double>(
				Collections.nCopies(cfcn.getNeuronCount(), 0.0));

		inputNeuronCount = cfcn.getInputNeuronCount();
		outputNeuronCount = cfcn.getOutputNeuronCount();
		timestepsPerActivation = cfcn.getTimestepsPerActivation();
		neuronOutputArray.set(0, 1.0);

		final StringBuilder sb = new StringBuilder();
		sb.append(Integer.toString(inputNeuronCount));
		sb.append(Integer.toString(outputNeuronCount));
		for (final String activationFc : neuronActivationFnArray) {
			sb.append(activationFc);
		}
		for (final List<Double> auxArgs : neuronAuxArgsArray) {
			for (final Double auxArg : auxArgs)
				sb.append(auxArg.toString());
		}
		for (final CConnection connection : connectionArray) {
			sb.append(Integer.toString(connection.getFromNeuronId()));
			sb.append(Integer.toString(connection.getToNeuronId()));
			sb.append(Double.toString(connection.getWeight()));
		}
		id = sb.toString();
	}

	/**
	 * Returns the value of the specified input neuron.
	 *
	 * @param non The number of the input neuron to be queried.
	 * @pre The input neuron exists.
	 * 	| 0 <= no < getNumberOfInputs()
	 * @return The value of queried input neuron.
	 */
	public double getInput(final int no) {
		assert no >= 0 && no < getNumberOfInputs();
		return neuronOutputArray.get(1+no);
	}

	/**
	 * Sets the value of an input neuron.
	 *
	 * @param no The input neuron whose value to change.
	 * @param val The value to set the input neuron whose value to change to.
	 * @pre The input neuron exists.
	 * 	| 0 <= no < getNumberOfInputs()
	 * @pre The given value is a finite number.
	 * 	| val != Double.NaN && !Double.isInfinite(val)
	 * @post The value of the specified input neuron is equal to the given value.
	 * 	| getInput(no) == val
	 */
	@Override
	public void setInput(final int no, final double val) {
		assert 0 <= no && no < getNumberOfInputs();
		assert val != Double.NaN && !Double.isInfinite(val);
		neuronOutputArray.set(1+no,val);
	}

	/**
	 * Returns the value of the specified output neuron.
	 *
	 * @param no The number of the output neuron to be queried.
	 * @pre The output neuron exists.
	 * 	| 0 <= no < getNumberOfOutputs()
	 * @return The value of the queried output neuron.
	 */
	@Override
	public double getOutput(final int no) {
		assert 0 <= no && no < getNumberOfOutputs();
		return neuronOutputArray.get(1+inputNeuronCount);
	}

	/**
	 * Sets he value of the specified output neuron to the specified value.
	 *
	 * @param no The number of the output neuron to change.
	 * @param val The value to set the output neuron to change to.
	 * @pre The given value is a finite number.
	 * 	| val != Double.NaN && !Double.isInfinite(val)
	 * @post The value of the specified output neuron is equal to the given value.
	 * 	| getOutput(no) == val
	 */
	protected void setOutput(final int no, final double val) {
		assert no >= 0 && no < getNumberOfOutputs();
		assert val != Double.NaN && !Double.isInfinite(val);
		neuronOutputArray.set(1+inputNeuronCount, val);
	}

	/**
	 * Activates this fast cyclic neural network.
	 *
	 * @see BlackBox#activate()
	 */
	@Override
	public void activate() {
		// Do the following 'timestepsPerActivation' number of times.
		for (int i = 0; i < timestepsPerActivation; i++) {
			// Loop connections
			for (final CConnection con : connectionArray) {
				final int to = con.getToNeuronId();
				final int from = con.getFromNeuronId();
				final double fromVal = neuronOutputArray.get(from);
				double toVal = neuronInputArray.get(to);
				toVal += fromVal * con.getWeight();
				neuronInputArray.set(to, toVal);
			}
			// Loop neurons
			for (int j = getInputAndBiasNeuronCount(); j < getNeuronCount() ; j++) {
				final String functionName = neuronActivationFnArray.get(j);
				final double output = FL.evaluate(functionName,
						neuronInputArray.get(j), neuronAuxArgsArray.get(j));
				neuronOutputArray.set(j, output);
				// We need to reset the input to zero, because we add to it in the
				// previous loop (because the input can be the sum of activation
				// gathered over multiple connections; this is the easiest / fastest way).
				neuronInputArray.set(j, 0.0);
			}
		}
//		for (int i = getInputAndBiasNeuronCount(); i < getNeuronCount() ; i++) {
//			neuronOutputArray.set(i, neuronOutputArray.get(i)/timestepsPerActivation);
//		}
	}

	protected int getNeuronCount() {
		return neuronInputArray.size();
	}

	/**
	 * Returns the number of input nodes, including the bias node.
	 * @return The number of input nodes, including the bias node.
	 * 	| result == getNumberOfInputs() + 1
	 */
	protected int getInputAndBiasNeuronCount() {
		return getNumberOfInputs() + 1;
	}

	/**
	 * Resets this fast cyclic neural network.
	 *
	 * @see BlackBox#reset()
	 */
	@Override
	public void reset() {
		for (int i = 0; i < neuronInputArray.size(); i++) {
			// TODO I do not see why this is necessary. Try removing.
			neuronInputArray.set(i, 0.0);
			neuronOutputArray.set(i, 0.0);
		}
		//preActivationArray.clear();
		//postActivationArray.clear();
	}

	/**
	 * Returns true; a fast cyclic neural network is always valid.
	 *
	 * @return Returns true.
	 * 	| result == true
	 */
	@Override
	public boolean isValid() {
		return true;
	}

	/**
	 * @see BlackBox#getNumberOfInputs()
	 */
	@Override
	public int getNumberOfInputs() {
		return inputNeuronCount;
	}

	/**
	 * @see BlackBox#getNumberOfOutputs()
	 */
	@Override
	public int getNumberOfOutputs() {
		return outputNeuronCount;
	}

	@Override
	public String getId() {
		return id;
	}
}
