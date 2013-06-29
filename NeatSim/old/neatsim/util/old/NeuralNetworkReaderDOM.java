package neatsim.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import neatsim.core.NeuralNetwork;
import neatsim.server.thrift.CConnection;

import org.javatuples.Pair;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class NeuralNetworkReaderDOM {

	public NeuralNetwork readFile(final String path) {
		return readFile(new File(path));
	}

	public NeuralNetwork readFile(final File file) {
		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			final DocumentBuilder dBuilder = dbf.newDocumentBuilder();
			final Document doc = dBuilder.parse(file);
			doc.normalize();
			assert doc.getDocumentElement().getNodeName() == "Root";
			final Map<Integer,String> activationFunctions = createActivationFunctionsMap(doc);
			final Pair<Integer,Integer> t = numberOfInputOutputNodes(doc);
			final int numberOfInputs = t.getValue0();
			final int numberOfOutputs = t.getValue1();
			System.out.println(numberOfInputs);
			System.out.println(numberOfOutputs);
			final ArrayList<CConnection> connectionList = readConnections(doc);
			System.out.println(connectionList);
		} catch (SAXException | IOException | ParserConfigurationException e) {
			assert false;
		}
		return null;
	}

	/**
	 * Returns map that maps node innovation identifiers onto fast cyclic network id's.
	 * @param doc Document file
	 * @return
	 */
	private Map<Integer,Integer> createNodeIdMap(final Node doc) {
//		final NodeListWrapper nodeList = new NodeListWrapper(
//				doc.getElementsByTagName("Node")
//				);
		return null;
	}

	private ArrayList<CConnection> readConnections(final Document doc) {
		final NodeListWrapper nodeList = new NodeListWrapper(
				doc.getElementsByTagName("Con"));
		final ArrayList<CConnection> connectionList = new ArrayList<>();
		for (final Node node : nodeList) {
			assert node.getNodeType() == Node.ELEMENT_NODE;
			final NamedNodeMap attr = node.getAttributes();
			final Attr srcAttr = (Attr)attr.getNamedItem("src");
			final Attr tgtAttr = (Attr)attr.getNamedItem("tgt");
			final Attr wghtAttr = (Attr)attr.getNamedItem("wght");
			final int src = Integer.parseInt(srcAttr.getValue());
			final int tgt = Integer.parseInt(tgtAttr.getValue());
			final double wght = Double.parseDouble(wghtAttr.getValue());
			final CConnection con = new CConnection(src,tgt,wght);
			connectionList.add(con);
		}
		return connectionList;
	}

	private Pair<Integer, Integer> numberOfInputOutputNodes(final Document doc) {
		final NodeListWrapper nodeList = new NodeListWrapper(
				doc.getElementsByTagName("Node"));
		int highestInputId = 0;
		int highestOutputId = 0;
		for (final Node node : nodeList) {
			assert node.getNodeType() == Node.ELEMENT_NODE;
			final NamedNodeMap attr = node.getAttributes();
			final Attr typeAttr = (Attr) attr.getNamedItem("type");
			final String type = typeAttr.getValue();
			final Attr idAttr = (Attr) attr.getNamedItem("id");
			final int id = Integer.parseInt(idAttr.getValue());
			if (type.equals("in")) {
				highestInputId = id > highestInputId ? id : highestInputId;
			} else if (type.equals("out")) {
				highestOutputId = id > highestOutputId ? id : highestOutputId;
			}
		}
		final int numberOfInputs = highestInputId;
		final int numberOfOutputs = highestOutputId - highestInputId;
		return new Pair<Integer, Integer>(numberOfInputs, numberOfOutputs);
	}

	private Map<Integer,String> createActivationFunctionsMap(final Document doc) {
		assert doc.getElementsByTagName("ActivationFunctions").getLength() == 1;
		final Node activationFunctionsNode =
				doc.getElementsByTagName("ActivationFunctions").item(0);
		assert activationFunctionsNode.getNodeName() == "ActivationFunctions";
		final NodeListWrapper activationFunctionList = new NodeListWrapper(
				activationFunctionsNode.getChildNodes());
		final Map<Integer,String> map = new HashMap<>();
		for (final Node node : activationFunctionList) {
			// Skip over text node
			if (node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			final NamedNodeMap attr = node.getAttributes();
			assert attr.getNamedItem("id").getNodeType() == Node.ATTRIBUTE_NODE;
			assert attr.getNamedItem("name").getNodeType() == Node.ATTRIBUTE_NODE;
			map.put(
					Integer.parseInt(attr.getNamedItem("id").getNodeValue()),
					attr.getNamedItem("name").getNodeValue());
		}
		return map;
	}
}
