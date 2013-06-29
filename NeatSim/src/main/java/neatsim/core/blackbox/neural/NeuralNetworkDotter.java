package neatsim.core.blackbox.neural;

import java.util.Iterator;
import java.util.LinkedList;

import neatsim.server.thrift.CConnection;

/**
 * Class that I quickly made for graphs in my final thesis presentation.
 * Unfinished :).
 *
 * Can convert a NeuralNetwork to a .dot representation
 *
 * @author Jonathan Merlevede
 *
 */
public class NeuralNetworkDotter {
	protected boolean varywidth = true;
	protected double maxwidth = 3;
	protected boolean varycolor = true;
	protected boolean printlabel = false;


	public NeuralNetworkDotter() {};

	public String toDot(final NeuralNetwork ann) {
		final StringBuilder builder = new StringBuilder();
		builder.append("digraph G {\n");
		for (final CConnection connection : ann.connectionArray) {
			builder.append("\t");
			builder.append(connection.fromNeuronId);
			builder.append("->");
			builder.append(connection.toNeuronId);

			builder.append("[");

			double maxval=0;
			if (varywidth) {
				for (final CConnection c : ann.connectionArray) {
					final double abs = Math.abs(c.weight);
					if (abs > maxval)
						maxval = abs;
				}
			}

			final LinkedList<String> options = new LinkedList<>();
				if (printlabel)
					options.add("label=\""+connection.weight+"\"");
				if (varywidth) {
					options.add("penwidth=\""
							+ Math.abs(connection.weight/maxval*maxwidth)
							+ "\"");
				}
				if (varycolor) {
					if (connection.weight < 0)
						options.add("color=\"#FF0000\"");
				}
				final Iterator<String> it = options.iterator();
				while (it.hasNext()) {
					builder.append(it.next());
					if (it.hasNext())
						builder.append(",");
				}
			builder.append("]\n");

		}
		builder.append("}");
		return builder.toString();
	}
}
