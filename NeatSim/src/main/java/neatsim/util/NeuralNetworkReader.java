package neatsim.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
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

import neatsim.core.NeuralNetwork;
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

	// Parameters
	protected int numberOfTimestepsPerActivation;

	// List of constructed networks
	protected ArrayList<NeuralNetwork> neuralNetworks;
	private final Map<Integer,String> functionIdToName;
	// 'Buffer' during construction of network
	private int maxInputId;
	private int maxOutputId;
	private final SortedMap<Integer,Integer> nodeFunctions;
	private final ArrayList<CConnection> connections;

	public NeuralNetworkReader(final int numberOfTimestepsPerActivation) {
		neuralNetworks = new ArrayList<>();
		functionIdToName = new HashMap<>();
		nodeFunctions = new TreeMap<>();
		connections = new ArrayList<>();
		this.numberOfTimestepsPerActivation = numberOfTimestepsPerActivation;
	}

	public int getNumberOfTimestepsPerActivation() {
		return numberOfTimestepsPerActivation;
	}

	public void setNumberOfTimestepsPerActivation(final int numberOfTimestepsPerActivation) {
		this.numberOfTimestepsPerActivation = numberOfTimestepsPerActivation;
	}


	private class XMLParser extends DefaultHandler {
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
				openNetwork();
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

	private void openNetwork() {
		maxInputId = 0;
		maxOutputId = 0;
		nodeFunctions.clear();
		connections.clear();
	}

	private void processActivationFunction(final Attributes attr) {
		functionIdToName.put(
				Integer.parseInt(attr.getValue("id")),
				attr.getValue("name"));
	}

	public List<NeuralNetwork> readFile(final URL url) throws IOException {
		return readFile(new File(url.getFile()));
	}

	public List<NeuralNetwork> readFile(final String path) throws IOException {
		return readFile(new File(path));
	}

	public List<NeuralNetwork> readFile(final File file) throws IOException {
		try {
			final SAXParserFactory spf = SAXParserFactory.newInstance();
			final SAXParser sp = spf.newSAXParser();
			final DefaultHandler handler = new XMLParser();
			sp.parse(file, handler);
			return neuralNetworks;
		} catch (SAXException | ParserConfigurationException e) {
			e.printStackTrace();
			assert false;
		}
		return null;
	}

}
