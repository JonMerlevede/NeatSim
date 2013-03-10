package neatsim.util;

import java.util.AbstractList;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NodeListWrapper extends AbstractList<Node> {
	protected NodeList nodeList;

	public NodeListWrapper(final NodeList nodeList) {
		this.nodeList = nodeList;
	}

	@Override
	public Node get(final int index) {
		return nodeList.item(index);
	}

	@Override
	public int size() {
		return nodeList.getLength();
	}

}
