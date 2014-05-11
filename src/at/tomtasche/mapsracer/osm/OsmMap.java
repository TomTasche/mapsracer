package at.tomtasche.mapsracer.osm;

import java.util.List;
import java.util.Map;
import java.util.Set;

import at.tomtasche.mapsracer.map.MapNode;
import at.tomtasche.mapsracer.map.MapPath;

public class OsmMap {
	private int width;
	private int height;

	private final List<MapPath> streets;
	private final Map<MapNode, Set<MapNode>> neighborMap;

	public OsmMap(int width, int height, List<MapPath> streets,
			Map<MapNode, Set<MapNode>> streetGraph) {
		this.width = width;
		this.height = height;
		this.streets = streets;
		this.neighborMap = streetGraph;
	}

	public Map<MapNode, Set<MapNode>> getNeighborMap() {
		return neighborMap;
	}

	public List<MapPath> getStreets() {
		return streets;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}
