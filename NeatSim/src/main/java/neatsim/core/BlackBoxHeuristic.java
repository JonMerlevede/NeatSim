package neatsim.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import rinde.ecj.GPFunc;
import rinde.ecj.Heuristic;
import rinde.evo4mas.common.GPFunctions.Ado;
import rinde.evo4mas.common.GPFunctions.Dist;
import rinde.evo4mas.common.GPFunctions.Est;
import rinde.evo4mas.common.GPFunctions.Mado;
import rinde.evo4mas.common.GPFunctions.Mido;
import rinde.evo4mas.common.GPFunctions.Ttl;
import rinde.evo4mas.common.GPFunctions.Urge;
import rinde.evo4mas.gendreau06.GendreauContext;
import rinde.evo4mas.gendreau06.GendreauFunctions.Adc;
import rinde.evo4mas.gendreau06.GendreauFunctions.CargoSize;
import rinde.evo4mas.gendreau06.GendreauFunctions.IsInCargo;
import rinde.evo4mas.gendreau06.GendreauFunctions.Madc;
import rinde.evo4mas.gendreau06.GendreauFunctions.Midc;
import rinde.evo4mas.gendreau06.GendreauFunctions.TimeUntilAvailable;
import rinde.evo4mas.gendreau06.MyopicFunctions.Waiters;

/**
 * TODO document
 * @author Jonathan Merlevede
 *
 */
public class BlackBoxHeuristic implements Heuristic<GendreauContext> {
	/**
	 * Default, immutable list of input heuristics.
	 * 
	 */
	public static final List<GPFunc<GendreauContext>> DEFAULT_INPUT_LIST;
	static {
		// Initialise temporary list with all required heuristics
		ArrayList<GPFunc<GendreauContext>> tmpList
			= new ArrayList<GPFunc<GendreauContext>>();
		tmpList.add(new Waiters()); //1
		tmpList.add(new CargoSize<GendreauContext>()); //2
		tmpList.add(new IsInCargo<GendreauContext>()); //3
		tmpList.add(new TimeUntilAvailable<GendreauContext>()); //4
		tmpList.add(new Ado<GendreauContext>()); //5
		tmpList.add(new Mido<GendreauContext>()); //6
		tmpList.add(new Mado<GendreauContext>()); //7
		tmpList.add(new Dist<GendreauContext>()); //8
		tmpList.add(new Urge<GendreauContext>()); //9
		tmpList.add(new Est<GendreauContext>()); //10
		tmpList.add(new Ttl<GendreauContext>()); //11
		tmpList.add(new Adc<GendreauContext>()); //12
		tmpList.add(new Midc<GendreauContext>()); //13
		tmpList.add(new Madc<GendreauContext>()); //14
		// Create an immutable view for the list
		DEFAULT_INPUT_LIST = Collections.unmodifiableList(tmpList);
	}
	
	/**
	 * The list of input heuristics used by this black box heuristics.
	 * 
	 * Each input heuristic is connected to a specific input of the the black box
	 * enclosed by this black box heuristic.
	 * 
	 * @see #compute(GendreauContext)
	 */
	private final List<GPFunc<GendreauContext>> inputList;
	
	public List<GPFunc<GendreauContext>> getInputList() {
		return inputList;
	}
	
	/**
	 * A reference to the black box that implements the heuristic wrapped by
	 * this black box heuristic.
	 */
	private final BlackBox box;
	
	/**
	 * Creates a new black box heuristic that encloses the given black box and
	 * the default set of input heuristics.
	 * 
	 * @param box The black box to be enclosed into the black box heuristic.
	 * @effect Creates this black box heuristic in the same way a black box
	 *         heuristic is initialised with the given black box and the default
	 *         set of input heuristics, specified by {@see #DEFAULT_INPUT_LIST}.
	 * 
	 *         | this(DEFAULT_INPUT_LIST,box)
	 */
	public BlackBoxHeuristic(BlackBox box) {
		this(DEFAULT_INPUT_LIST, box);
	}
	
	/**
	 * Creates a new black box heuristic that uses the given black box as its
	 * black box.
	 * 
	 * @param box The black box to be enclosed into the black box heuristic.
	 * @param inputList The given list of input heuristics.
	 * @pre The given list of input heuristics is effective.
	 * 
	 *      | inputList != null
	 * @pre The given black box is effective.
	 * 
	 *      | box != null
	 * @pre The given black box has as much inputs as there are input heuristics
	 *      in the given list of input heuristics.
	 * 
	 *      | box.getNumberOfInputs() == inputList.size()
	 * @pre The given black box has precisely one output.
	 * 
	 *      | box.getNumberOfOutputs() == 1
	 * @post The box enclosed by this black box heuristic is the same box as the
	 *       given box.
	 * 
	 *       | getBlackBox() == box
	 * @note Changes to the original box are visible as changes to the black box
	 *       enclosed by this black box heuristic. It is therefore not allowed to
	 *       change a black box after it has been passed to this black box
	 *       heuristic. This is prone to change.
	 */
	public BlackBoxHeuristic(List<GPFunc<GendreauContext>> inputList, BlackBox box) {
		assert inputList != null;
		assert box != null;
		assert box.getNumberOfInputs() == inputList.size();
		assert box.getNumberOfOutputs() == 1;
		
		// Deep copy of the given input list
		LinkedList<GPFunc<GendreauContext>> tmpList = new LinkedList<>();
		for (GPFunc<GendreauContext> gpf : inputList) {
			tmpList.add(gpf);
		}
		// Set the input list to an unmodifiable view of the deep copy
		this.inputList = Collections.unmodifiableList(tmpList);
		// Set the black box enclosed by this black box heuristic to the given black box
		this.box = box;
	}

	/**
	 * Sets the inputs of all the inputs of the black box to the correct values.
	 * Correct values are determined by the input heuristics in the {@see
	 * #INPUT_LIST} and the supplied Gendreau context.
	 * 
	 * @param gc The supplied Gendreau context.
	 * @post The inputs of the black box underlying this black box heuristics are
	 *       set to the values defined by the input heuristics in the {@see
	 *       #INPUT_LIST} and the supplied Gendreau context.
	 *		| for 0 <= i < INPUT_LIST.size()
	 *		|   getBlackBox().getInput(i) == INPUT_LIST.get(i).execute(null,gc)
	 */
	protected void setInputs(GendreauContext gc) {
		for (int i = 0; i < inputList.size(); i++) {
			// Note that we give '0' for the input value of the GPFuncs in the
			// inputList.
			// Although generally a GPFunc can have an array of inputs, 'input'
			// GPFuncs should not need check use any inputs at all.
			box.setInput(i, inputList.get(i).execute(null, gc));
		}
	}
	
	/**
	 * Computes the heuristic defined by this black box heuristic for the given
	 * Gendreau context.
	 * 
	 * If the black box enclosed by this black box heuristic has state, the
	 * result of compute may depend on values passed to previous calls to
	 * the black box heuristic.
	 * 
	 * @see BlackBox
	 */
	@Override
	public double compute(GendreauContext gc) {
		setInputs(gc);
		box.activate();
		return box.getOutput(0);
	}
	
	/**
	 * Returns a reference to the black box enclosed by this black box heuristic.
	 * 
	 * Changes to this box are visible to this black box heuristic. In
	 * particular, if the underlying black box has state, this reference may be
	 * be used to reset the enclosed black box and therefore also the black box
	 * heuristic.
	 * 
	 * @return A reference to the black box enclosed by this black box heuristic.
	 * @see BlackBox
	 */
	public BlackBox getBlackBox() {
		return box;
	}

	// TODO ask what this id is for...?
	@Override
	public String getId() {
		return "I do not know what this is! " +
				"Does 'blackbox' seem like a sensible id?" +
				hashCode(); // add the hashcode in case the id needs to be unique :)
	}
	
	
}
