import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import neatsim.util.NeuralNetworkReader;

import org.junit.Test;


public class TmpGetBirthgens {
	final String dir =
			"C:\\Users\\Jonathan\\Dropbox\\univ\\ma2\\thesis\\own\\Data processing\\short_overfitting_600-30_absolute3000";

	/**
	 * Quick and dirty: get birthgens for in thesis...
	 * @throws IOException
	 */
	@Test
	public void moo() throws IOException {
		final NeuralNetworkReader r = new NeuralNetworkReader(3);
		final List<NeuralNetworkReader.Result> results = r.readDirectory(dir, "overfitting", ".xml", true);
		final Iterator<NeuralNetworkReader.Result> it = results.iterator();
		while(it.hasNext()) {
			System.out.print(it.next().birthGen);
			if (it.hasNext())
				System.out.print(";");
		}
	}
}
