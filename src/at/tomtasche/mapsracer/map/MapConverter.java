package at.tomtasche.mapsracer.map;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;

import at.tomtasche.mapsracer.data.Cluster;
import at.tomtasche.mapsracer.math.CoordinateUtil;
import at.tomtasche.mapsracer.osm.OsmMap;
import at.tomtasche.mapsracer.osm.OsmParser;

public final class MapConverter {

	private MapConverter() {
	}

	public static OsmMap convert(OsmParser parser, Cluster cluster) {
		Map<Long, MapNode> nodeMap = new HashMap<>();

		List<MapPath> streets = new LinkedList<>();
		Map<MapNode, Set<MapNode>> neighborMap = new HashMap<>();

		for (Way way : parser.getWays()) {
			String name = "unknown";

			for (Tag tag : way.getTags()) {
				if (tag.getKey().equals("name")) {
					name = tag.getValue();
					break;
				}
			}

			MapNode lastNode = null;
			MapPath path = new MapPath(way.getId(), name);
			List<WayNode> wayNodes = way.getWayNodes();

			for (WayNode wayNode : wayNodes) {
				MapNode mapNode = nodeMap.get(wayNode.getNodeId());
				if (mapNode == null) {
					mapNode = toMapNode(parser, wayNode, cluster);
					nodeMap.put(wayNode.getNodeId(), mapNode);
				}

				Set<MapNode> links = neighborMap.get(mapNode);
				if (links == null) {
					links = new HashSet<>();
					neighborMap.put(mapNode, links);
				}

				if (lastNode != null) {
					// add left
					links.add(lastNode);
					// add right
					neighborMap.get(lastNode).add(mapNode);
				}

				path.addNode(mapNode);
				lastNode = mapNode;
			}

			streets.add(path);
		}

		double mapWidth = CoordinateUtil.distance(parser.getMinLat(), parser.getMinLon(),
				parser.getMinLat(), parser.getMaxLon());
		double mapHeight = CoordinateUtil.distance(parser.getMinLat(), parser.getMinLon(),
				parser.getMaxLat(), parser.getMinLon());

		return new OsmMap((int) mapWidth, (int) mapHeight, streets, neighborMap);
	}

	private static MapNode toMapNode(OsmParser parser, WayNode wayNode,
			Cluster cluster) {
		return toMapNode(parser, parser.toNode(wayNode), cluster);
	}

	private static MapNode toMapNode(OsmParser parser, Node node,
			Cluster cluster) {
		return new MapNode(node.getLongitude(), node.getLatitude(), cluster);
	}

	private static int calculateX(OsmParser parser, Node node) {
		return calculateX(parser, node.getLongitude());
	}

	private static int calculateX(OsmParser parser, double longitude) {
		return (int) CoordinateUtil.distance(parser.getMinLat(),
				parser.getMinLon(), parser.getMinLat(), longitude);
	}

	private static int calculateY(OsmParser parser, Node node) {
		return calculateY(parser, node.getLatitude());
	}

	private static int calculateY(OsmParser parser, double latitude) {
		return (int) CoordinateUtil.distance(parser.getMinLat(),
				parser.getMinLon(), latitude, parser.getMinLon());
	}
}
