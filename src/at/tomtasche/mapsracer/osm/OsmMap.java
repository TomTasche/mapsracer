package at.tomtasche.mapsracer.osm;

import java.util.List;
import java.util.Map;
import java.util.Set;

import at.tomtasche.mapsracer.map.MapNode;
import at.tomtasche.mapsracer.map.MapPath;

public class OsmMap {

	private final List<MapPath> streets;
	private final Map<MapNode, Set<MapNode>> neighborMap;

	public OsmMap(List<MapPath> streets, Map<MapNode, Set<MapNode>> streetGraph) {
		this.streets = streets;
		this.neighborMap = streetGraph;
	}

	public Map<MapNode, Set<MapNode>> getNeighborMap() {
		return neighborMap;
	}

	public List<MapPath> getStreets() {
		return streets;
	}
}
