package at.tomtasche.mapsracer.map;

import java.util.List;
import java.util.Map;

public class OsmMap {
	private final List<MapPath> streets;
	private final Map<MapNode, List<MapNode>> streetGraph;

	public OsmMap(List<MapPath> streets, Map<MapNode, List<MapNode>> streetGraph) {
		super();
		this.streets = streets;
		this.streetGraph = streetGraph;
	}

	public Map<MapNode, List<MapNode>> getStreetGraph() {
		return streetGraph;
	}

	public List<MapPath> getStreets() {
		return streets;
	}
}
