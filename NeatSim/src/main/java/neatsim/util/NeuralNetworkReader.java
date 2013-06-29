package neatsim.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import neatsim.core.blackbox.neural.NeuralNetwork;
import neatsim.server.thrift.CConnection;
import neatsim.server.thrift.CFastCyclicNetwork;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Jonathan Merlevede
 *	@note Auxiliary arguments are currently unsupported.
 */
public class NeuralNetworkReader {

	public static class Result {
		public final List<NeuralNetwork> neuralNetworks;
		public final int birthGen;
		public Result(final List<NeuralNetwork> neuralNetworks, final int birthGen) {
			this.neuralNetworks = neuralNetworks;
			this.birthGen = birthGen;
		}
	}

	// Parameters
	final protected int numberOfTimestepsPerActivation;

	// List of constructed networks
	protected ArrayList<NeuralNetwork> neuralNetworks;
	private Map<Integer,String> functionIdToName;
	private int birthGen=-1;
	// 'Buffer' during construction of network
	private int maxInputId;
	private int maxOutputId;
	private SortedMap<Integer,Integer> nodeFunctions;
	private ArrayList<CConnection> connections;

	public NeuralNetworkReader(final int numberOfTimestepsPerActivation) {
		//neuralNetworks = new ArrayList<>();
		//functionIdToName = new HashMap<>();
		//nodeFunctions = new TreeMap<>();
		//connections = new ArrayList<>();
		this.numberOfTimestepsPerActivation = numberOfTimestepsPerActivation;
	}

	public int getNumberOfTimestepsPerActivation() {
		return numberOfTimestepsPerActivation;
	}

//	public void setNumberOfTimestepsPerActivation(final int numberOfTimestepsPerActivation) {
//		this.numberOfTimestepsPerActivation = numberOfTimestepsPerActivation;
//	}


	private class NeatGenomeXMLParser extends DefaultHandler {
		@Override
		public void startElement(
				final String uri,
				final String localName,
				final String qName,
				final Attributes attr) {
			switch (qName.toUpperCase()) {
			case ("FN") :
//				System.out.println("Processing <Fn>");
				processActivationFunction(attr);
				break;
			case ("NETWORK") :
//				System.out.println("Opening <Network>");
				openNetwork(attr);
				break;
			case ("NODE") :
//				System.out.println("Processing <Node>");
				processNode(attr);
				break;
			case ("CON") :
//				System.out.println("Processing <Con>");
				processConnection(attr);
				break;
			}
		}
		@Override
		public void endElement(
				final String uri,
				final String localName,
				final String qName) {
			switch (qName.toUpperCase()) {
			case ("NETWORK") :
//				System.out.println("Closing <Network>");
				closeNetwork();
			}
		}
	}

	private Map<Integer,Integer> createNodeRemapping() {
		final Map<Integer,Integer> remapping = new HashMap<>();
		int i = 0;
		final Set<Integer> keySet = nodeFunctions.keySet();
		for (final Integer key : keySet) {
			remapping.put(key, i);
			i++;
		}
		return remapping;
	}

	private void closeNetwork() {
		final int numberOfInputs = maxInputId;
		final int numberOfOutputs = maxOutputId - maxInputId;
		final int numberOfNeurons = nodeFunctions.size();
		final Map<Integer,Integer> innovationToNetworkId = createNodeRemapping();
		// Remap connections
		for (final CConnection connection : connections) {
			connection.setFromNeuronId(
					innovationToNetworkId.get(connection.getFromNeuronId()));
			connection.setToNeuronId(
					innovationToNetworkId.get(connection.getToNeuronId()));
		}
		// Order activation functions
		final ArrayList<String> activationFunctions = new ArrayList<>(nodeFunctions.size());
		final ArrayList<List<Double>> auxArgs = new ArrayList<>();
		for(final Entry<Integer,Integer> entry : nodeFunctions.entrySet()) {
			activationFunctions.add(
					innovationToNetworkId.get(entry.getKey()),
					functionIdToName.get(entry.getValue()));
			// Create empty auxiliary argument arrays
			auxArgs.add(new ArrayList<Double>());
		}

		final CFastCyclicNetwork closedNetwork = new CFastCyclicNetwork(
				connections,
				activationFunctions,
				auxArgs,
				numberOfNeurons,
				numberOfInputs,
				numberOfOutputs,
				numberOfTimestepsPerActivation);
		neuralNetworks.add(new NeuralNetwork(closedNetwork));
	}

	private void processConnection(final Attributes attr) {
		final String srcStr = attr.getValue("src");
		final String tgtStr = attr.getValue("tgt");
		final String wghtStr = attr.getValue("wght");
		if (srcStr == null || tgtStr == null || wghtStr == null) {
			throw new IncorrectlyFormattedXMLException();
		}
		final int innovationIdSrc = Integer.parseInt(srcStr);
		final int innovationIdTgt = Integer.parseInt(tgtStr);
		final double wght = Double.parseDouble(wghtStr);
		final CConnection connection = new CConnection(innovationIdSrc, innovationIdTgt, wght);
		connections.add(connection);
	}


	private void processNode(final Attributes attr) {
		final String idStr = attr.getValue("id");
		if (idStr == null) throw new IncorrectlyFormattedXMLException();
		final int innovationId = Integer.parseInt(idStr);
		if (attr.getValue("fnId") != null) {
			final int fnId = Integer.parseInt(attr.getValue("fnId"));
			nodeFunctions.put(innovationId, fnId);
		} else {
			nodeFunctions.put(innovationId, 0);
		}
		switch(attr.getValue("type").toUpperCase()) {
		case ("IN") :
			maxInputId = Math.max(maxInputId, innovationId);
			break;
		case ("OUT") :
			maxOutputId = Math.max(maxOutputId, innovationId);
			break;
		case ("HID") : break;
		case ("BIAS") : break;
		default :
			throw new IncorrectlyFormattedXMLException();
		}
	}

	private void openNetwork(final Attributes attr) {
		maxInputId = 0;
		maxOutputId = 0;
		nodeFunctions.clear();
		connections.clear();

		birthGen = Integer.parseInt(attr.getValue("birthGen"));
	}

	private void processActivationFunction(final Attributes attr) {
		functionIdToName.put(
				Integer.parseInt(attr.getValue("id")),
				attr.getValue("name"));
	}

	public List<NeuralNetwork> process(final InputStream genome) {
		try {
			final SAXParserFactory spf = SAXParserFactory.newInstance();
			final SAXParser sp = spf.newSAXParser();
			final DefaultHandler handler = new NeatGenomeXMLParser();
			sp.parse(genome, handler);
			return neuralNetworks;
		} catch (SAXException | ParserConfigurationException | IOException e) {
			e.printStackTrace();
			assert false;
		}
		return null;
	}

	public Result readFile(final URL url) throws IOException {
		return readFile(new File(url.getFile()));
	}

	public Result readFile(final String path) throws IOException {
		return readFile(new File(path));
	}

	public Result readFile(final File file) throws IOException {
		try {
			connections = new ArrayList<>();
			functionIdToName = new HashMap<>();
			maxInputId = -1;
			maxOutputId = -1;
			neuralNetworks = new ArrayList<>();
			nodeFunctions = new TreeMap<>();

			final SAXParserFactory spf = SAXParserFactory.newInstance();
			final SAXParser sp = spf.newSAXParser();
			final DefaultHandler handler = new NeatGenomeXMLParser();
			sp.parse(file, handler);
			return new Result(neuralNetworks, birthGen);
		} catch (SAXException | ParserConfigurationException e) {
			e.printStackTrace();
			assert false;
		}
		return null;
	}

	public List<NeuralNetworkReader.Result> readDirectory(
			final String path,
			final String prefix,
			final String suffix,
			final boolean singletonGenome) throws IOException {
		String dataDirectory = (new File(path)).getCanonicalPath();
		// Read timesteps setting

		if (!dataDirectory.endsWith(File.separator))
			dataDirectory = dataDirectory + File.separator;

		final File folder = new File(dataDirectory);
		final FilenameFilter filenameFilter = new PrefixSuffixFilter(prefix, suffix);
		final List<String> sgenomes = new ArrayList<String>();
		for (final File file : folder.listFiles(filenameFilter))
			sgenomes.add(dataDirectory + file.getName());
		Collections.sort(sgenomes, NaturalOrderComparator.CASEINSENSITIVE_NUMERICAL_ORDER);
		//# Convert the genome path names to ANNs
		final List<Result> genomes = new ArrayList<Result>(sgenomes.size());
		//final NeuralNetworkReader reader = new NeuralNetworkReader(numberOfSteps);
		if (singletonGenome) {
			for (final String sgenome : sgenomes) {
				final Result t = readFile(sgenome);
//				if (t.size() != 1) {
//					throw new RuntimeException("File " + sgenome + " contains more than one genome.");
//				}
				genomes.add(t);
			}
		} else {
			for (final String sgenome : sgenomes) {
				final Result t = readFile(sgenome);
				genomes.add(t);
//				for (final BlackBox bb : t)
//					genomes.add(bb);
			}
		}
		return genomes;
	}
}
