package at.tomtasche.mapsracer.map;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;

import at.tomtasche.mapsracer.OsmParser;

public final class MapConverter {

	private MapConverter() {
	}

	public static OsmMap convert(OsmParser parser) {
		List<MapPath> streets = new LinkedList<>();
		Map<MapNode, List<MapNode>> streetGraph = new HashMap<>();
		for (Way way : parser.getWays()) {
			String name = "unknown";
			for (Tag tag : way.getTags()) {
				if (!tag.getKey().equals("name")) {
					continue;
				} else {
					name = tag.getValue();
				}
			}

			MapNode lastNode = null;
			MapPath path = new MapPath(name);
			List<WayNode> wayNodes = way.getWayNodes();
			for (int i = 0; i < wayNodes.size(); i++) {
				WayNode wayNode = wayNodes.get(i);

				MapNode mapNode = toMapNode(parser, wayNode);
				List<MapNode> links = streetGraph.get(mapNode);
				if (links == null) {
					links = new LinkedList<>();
					streetGraph.put(mapNode, links);
				}

				if (lastNode != null) {
					links.add(mapNode);
				}
				if (i + 1 < wayNodes.size()) {
					links.add(toMapNode(parser, wayNodes.get(i + 1)));
				}

				path.addNode(mapNode);
				lastNode = mapNode;
			}

			streets.add(path);
		}

		return new OsmMap(streets, streetGraph);
	}

	private static MapNode toMapNode(OsmParser parser, WayNode wayNode) {
		return toMapNode(parser, parser.toNode(wayNode));
	}

	private static MapNode toMapNode(OsmParser parser, Node node) {
		int x = calculateX(parser, node);
		int y = calculateY(parser, node);

		MapNode mapNode = new MapNode(x, y);

		return mapNode;
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
