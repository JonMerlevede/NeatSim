package neatsim.core.blackbox.neural;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import neatsim.core.Activationfunction;
import neatsim.core.blackbox.BlackBox;
import neatsim.server.thrift.CConnection;
import neatsim.server.thrift.CFastCyclicNetwork;

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
public class NeuralNetwork implements BlackBox, Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -4370512373443228102L;

	/**
	 * Array of connections between neurons.
	 * Neurons are identified by their number.
	 */
	public final List<CConnection> connectionArray;

	/**
	 * Array of activation functions.
	 */
	private final List<Activationfunction> activationFunctions;

	/**
	 * Array of auxiliary arguments to these functions.
	 * (These are not currently used)
	 */
	private final List<List<Double>> auxArgsMatrix;

	/**
	 * Array of activations before activation.
	 *
	 * @see #inputNeuronCount
	 */
	private final List<Double> preActivation;

	/**
	 * Array of activations after activation.
	 *
	 * @see #inputNeuronCount
	 */
	private final List<Double> postActivation;

	/**
	 * The number of input neurons.
	 *
	 * Neurons are not stored as neuron objects, but are instead identified by an
	 * integer identifier. Their activation values are stored in two 'flat'
	 * lists: {@link #preActivation} and {@link #postActivation}.
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
	 */
	public NeuralNetwork(final CFastCyclicNetwork cfcn) {
		// The given Thrift FCNN is effective.
		assert cfcn != null;
		// In the given FCNN, the number of neurons in the network is at least one (there must always be a bias node).
		assert cfcn.getNeuronCount() >= 1;
		// In the given FCNN, there are as least as many neurons as there are input and output neurons and bias nodes.
		assert cfcn.getInputNeuronCount() + cfcn.getOutputNeuronCount() + 1 <= cfcn.getNeuronCount();
		// This FCNN has the same number of input neurons as the given FCNN in Thrift format.
		assert cfcn.getNeuronCount() == cfcn.getActivationFunctions().size();
		assert cfcn.getNeuronCount() == cfcn.getNeuronAuxArgs().size();
		// The connections specified by the given FCNN are valid.
		assert validConnections(cfcn.getNeuronCount(), cfcn.getConnections());

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
		activationFunctions = new ArrayList<>(cfcn.getActivationFunctions().size());
		for (final String functionName : cfcn.getActivationFunctions()) {
			activationFunctions.add(Activationfunction.fromString(functionName));
		}

		// Deep cloning of the list of auxiliary function arguments.
		//neuronAuxArgsArray = cfcn.getNeuronAuxArgs();
		auxArgsMatrix = new ArrayList<>(cfcn.getNeuronAuxArgs().size());
		for (final List<Double> e : cfcn.getNeuronAuxArgs())
			auxArgsMatrix.add(new ArrayList<Double>(e));

		// Initialise arrays of the correct size with zero values.
		preActivation = new ArrayList<Double>(
				Collections.nCopies(cfcn.getNeuronCount(), 0.0));
		postActivation = new ArrayList<Double>(
				Collections.nCopies(cfcn.getNeuronCount(), 0.0));

		inputNeuronCount = cfcn.getInputNeuronCount();
		outputNeuronCount = cfcn.getOutputNeuronCount();
		timestepsPerActivation = cfcn.getTimestepsPerActivation();
		postActivation.set(0, 1.0);

		final StringBuilder sb = new StringBuilder();
		sb.append(this.hashCode()+"");
		sb.append(Integer.toString(inputNeuronCount));
		sb.append(Integer.toString(outputNeuronCount));
		for (final Activationfunction activationFc : activationFunctions) {
			sb.append(activationFc);
		}
		for (final List<Double> auxArgs : auxArgsMatrix) {
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
	 * @return The value of queried input neuron.
	 */
	public double getInput(final int no) {
		// The input neuron exists.
		assert no >= 0 && no < getNumberOfInputs();
		return postActivation.get(1+no);
	}

	/**
	 * Sets the value of an input neuron.
	 *
	 * @param no The input neuron whose value to change.
	 * @param val The value to set the input neuron whose value to change to.
	 */
	@Override
	public void setInput(final int no, final double val) {
		// The input neuron exists.
		assert 0 <= no && no < getNumberOfInputs();
		// The given value is a finite number.
		assert val != Double.NaN && !Double.isInfinite(val);
		postActivation.set(1+no,val);
		// The value of the specified input neuron is equal to the given value.
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
		final double val = postActivation.get(1+inputNeuronCount);
		//assert !Double.isNaN(val);
		return val;
	}

	/**
	 * Sets the value of the specified output neuron to the specified value.
	 *
	 * @param no The number of the output neuron to change.
	 * @param val The value to set the output neuron to change to.
	 */
	protected void setOutput(final int no, final double val) {

		assert no >= 0 && no < getNumberOfOutputs();
		// The given value is a finite number.
		assert val != Double.NaN && !Double.isInfinite(val);
		postActivation.set(1+inputNeuronCount, val);
		// The value of the specified output neuron is equal to the given value.
	}

	/**
	 * Activates this fast cyclic neural network.
	 * Activation makes values that are at the inputs of the ANN propagate towards its outputs.
	 *
	 * @see BlackBox#activate()
	 */
	@Override
	public void activate() {
		assert (postActivation.get(0) == 1d);
		// Do the following 'timestepsPerActivation' number of times.
		for (int i = 0; i < timestepsPerActivation; i++) {
			// Loop connections
			for (final CConnection con : connectionArray) {
				final int to = con.getToNeuronId();
				final int from = con.getFromNeuronId();
				assert to >= getInputAndBiasNeuronCount(); // never connect to the bias neuron or input neurons
				final double fromVal = postActivation.get(from);
				double toVal = preActivation.get(to);
				toVal += fromVal * con.getWeight();
				preActivation.set(to, toVal);
			}
			// Loop neurons
			for (int j = getInputAndBiasNeuronCount(); j < getNeuronCount() ; j++) {
				final double output = activationFunctions.get(j).calculate(
						preActivation.get(j), auxArgsMatrix.get(j));
//				final String functionName = activationFunctions.get(j);
//				final double output = FL.evaluate(functionName,
//						preActivation.get(j), auxArgsMatrix.get(j));
				postActivation.set(j, output);
				// We need to reset the input to zero, because we add to it in the
				// previous loop (because the input can be the sum of activation
				// gathered over multiple connections; this is the easiest / fastest way).
				preActivation.set(j, 0.0);
			}
		}
//		for (int i = getInputAndBiasNeuronCount(); i < getNeuronCount() ; i++) {
//			neuronOutputArray.set(i, neuronOutputArray.get(i)/timestepsPerActivation);
//		}
	}

	protected int getNeuronCount() {
		return preActivation.size();
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
		// Reset the value of all nodes except from the bias node
		for (int i = getInputAndBiasNeuronCount(); i < preActivation.size(); i++) {
			// TODO I do not see why this is necessary. Try removing.
			preActivation.set(i, 0.0);
			postActivation.set(i, 0.0);
		}
		//preActivationArray.clear();
		//postActivationArray.clear();
	}

	public void resetAll() {
		for (int i = 1; i < preActivation.size(); i++) {
			// TODO I do not see why this is necessary. Try removing.
			preActivation.set(i, 0.0);
			postActivation.set(i, 0.0);
		}
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

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName());
		sb.append("(");
		sb.append("\n\tconnections: [");
		for (final CConnection connection : connectionArray) {
			sb.append("\n\t\t");
			sb.append(connection);
		}
		sb.append("],\n\tactivationFunctions: ");
		sb.append(activationFunctions);
		sb.append("],\n\tneuronAuxArgs: ");
		sb.append(auxArgsMatrix);
		sb.append("\n\tneuronCount: " + getNeuronCount());
		sb.append("\n\tinputNeuronCount: " + getNumberOfInputs());
		sb.append("\n\toutputNeuronCount: " + getNumberOfOutputs());
		sb.append("\n\ttimeStepsPerActivation: " + timestepsPerActivation);
		sb.append("\n)");
		return sb.toString();
	}
}
