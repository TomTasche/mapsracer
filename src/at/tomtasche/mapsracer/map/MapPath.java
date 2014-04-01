package at.tomtasche.mapsracer.map;

import java.util.LinkedList;
import java.util.List;

public class MapPath {
	private final List<MapNode> nodes;

	public MapPath() {
		nodes = new LinkedList<>();
	}

	public void addNode(MapNode mapNode) {
		nodes.add(mapNode);
	}

	public List<MapNode> getNodes() {
		return nodes;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MapPath other = (MapPath) obj;
		if (nodes == null) {
			if (other.nodes != null)
				return false;
		} else if (!nodes.equals(other.nodes))
			return false;
		return true;
	}
}
