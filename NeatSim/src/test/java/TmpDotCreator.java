import java.io.IOException;
import java.util.List;

import neatsim.util.NeuralNetworkReader;
import neatsim.util.NeuralNetworkReader.Result;

import org.junit.Test;

/**
 * Class that I quickly made for creating a .dot representation of a genome for my presentation.
 *
 * @author Jonathan Merlevede
 */
public class TmpDotCreator {
	// Change this - this only makes sense on my pc :)


	@Test
	public void moo() throws IOException {
		final String inputdir = "C:\\Users\\Jonathan\\Dropbox\\univ\\ma2\\thesis\\own\\Data processing\\short_600-40_absolute4000_3";
		final String outputdir = "C:\\Users\\Jonathan\\Dropbox\\univ\\ma2\\thesis\\own\\Presentaties\\Presentatie 6 - Final\\dot";
		final String prefix = "short_600";
		final String postfix = ".dot";

		final NeuralNetworkReader r = new NeuralNetworkReader(3);
		final List<NeuralNetworkReader.Result> results = r.readDirectory(inputdir, "short_", ".xml", true);
		for (final Result result : results) {
			System.out.println(result.neuralNetworks.get(0).connectionArray.size());
		}
//		final NeuralNetworkDotter dotter = new NeuralNetworkDotter();
//		int i = 1;
//		for (final NeuralNetworkReader.Result result : results) {
//			final String dotresult = dotter.toDot(result.neuralNetworks.get(0));
//			final Writer writer = new BufferedWriter(new OutputStreamWriter(
//		          new FileOutputStream(outputdir+"\\"+prefix+i+postfix), "utf-8"));
//			writer.write(dotresult);
//			writer.close();
//			i++;
//		}
	}
}
