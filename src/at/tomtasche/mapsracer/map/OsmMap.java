package at.tomtasche.mapsracer.map;

import java.util.List;
import java.util.Map;

public class OsmMap {
	private int width;
	private int height;

	private final List<MapPath> streets;
	private final Map<MapNode, List<MapNode>> streetGraph;

	public OsmMap(int width, int height, List<MapPath> streets,
			Map<MapNode, List<MapNode>> streetGraph) {
		this.width = width;
		this.height = height;
		this.streets = streets;
		this.streetGraph = streetGraph;
	}

	public Map<MapNode, List<MapNode>> getStreetGraph() {
		return streetGraph;
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
