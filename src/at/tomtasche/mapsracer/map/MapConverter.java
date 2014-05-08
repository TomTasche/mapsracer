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
import at.tomtasche.mapsracer.osm.OsmMap;
import at.tomtasche.mapsracer.osm.OsmParser;

public final class MapConverter {

	/**
	 * origin-cluster
	 */
	private static final Cluster cluster = new Cluster(0, 0);

	private MapConverter() {
	}

	public static OsmMap convert(OsmParser parser) {
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
					mapNode = toMapNode(parser, wayNode);
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

		double mapWidth = distFrom(parser.getMinLat(), parser.getMinLon(),
				parser.getMinLat(), parser.getMaxLon());
		double mapHeight = distFrom(parser.getMinLat(), parser.getMinLon(),
				parser.getMaxLat(), parser.getMinLon());

		return new OsmMap((int) mapWidth, (int) mapHeight, streets, neighborMap);
	}

	private static MapNode toMapNode(OsmParser parser, WayNode wayNode) {
		return toMapNode(parser, parser.toNode(wayNode));
	}

	private static MapNode toMapNode(OsmParser parser, Node node) {
		return new MapNode(node.getLongitude(), node.getLatitude(), cluster);
	}

	private static int calculateX(OsmParser parser, Node node) {
		return calculateX(parser, node.getLongitude());
	}

	private static int calculateX(OsmParser parser, double longitude) {
		return (int) distFrom(parser.getMinLat(), parser.getMinLon(),
				parser.getMinLat(), longitude);
	}

	private static int calculateY(OsmParser parser, Node node) {
		return calculateY(parser, node.getLatitude());
	}

	private static int calculateY(OsmParser parser, double latitude) {
		return (int) distFrom(parser.getMinLat(), parser.getMinLon(), latitude,
				parser.getMinLon());
	}

	private static double distFrom(double lat1, double lng1, double lat2,
			double lng2) {
		double earthRadius = 3958.75;
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
				+ Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2)
				* Math.sin(dLng / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double dist = earthRadius * c;

		int meterConversion = 1609;

		return dist * meterConversion;
	}
}
