package at.tomtasche.mapsracer.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.stefl.commons.io.FluidInputStreamReader;
import at.stefl.commons.lwxml.LWXMLEvent;
import at.stefl.commons.lwxml.reader.LWXMLReader;
import at.stefl.commons.lwxml.reader.LWXMLStreamReader;
import at.tomtasche.mapsracer.map.MapNode;
import at.tomtasche.mapsracer.map.MapPath;
import at.tomtasche.mapsracer.osm.OsmMap;
import at.tomtasche.mapsracer.ui.MapsRacer;

public class NodeParser {

	public OsmMap parse(InputStream inputStream, Cluster cluster)
			throws IOException {
		LWXMLReader lwxmlReader = new LWXMLStreamReader(
				new FluidInputStreamReader(inputStream));

		Map<Long, MapNode> nodes = new HashMap<Long, MapNode>();
		List<MapPath> ways = new LinkedList<MapPath>();

		LWXMLEvent event = lwxmlReader.readEvent();
		while (true) {
			if (event == LWXMLEvent.END_DOCUMENT) {
				break;
			}

			// TODO: do not hardcode readEvent and readValue calls
			if (event.hasValue()) {
				String value = lwxmlReader.readValue();
				if (value.equals("node")) {
					lwxmlReader.readEvent();
					if (!"id".equals(lwxmlReader.readValue())) {
						continue;
					}
					lwxmlReader.readEvent();
					long id = Long.parseLong(lwxmlReader.readValue());

					lwxmlReader.readEvent();
					if (!"lat".equals(lwxmlReader.readValue())) {
						continue;
					}
					lwxmlReader.readEvent();
					double lat = Double.parseDouble(lwxmlReader.readValue());

					lwxmlReader.readEvent();
					if (!"lon".equals(lwxmlReader.readValue())) {
						continue;
					}
					lwxmlReader.readEvent();
					double lon = Double.parseDouble(lwxmlReader.readValue());

					MapNode node = new MapNode(lon, lat, cluster);
					nodes.put(id, node);
				} else if (value.equals("way")) {
					lwxmlReader.readEvent();
					if (!"id".equals(lwxmlReader.readValue())) {
						continue;
					}
					lwxmlReader.readEvent();
					long id = Long.parseLong(lwxmlReader.readValue());

					lwxmlReader.readEvent();
					lwxmlReader.readEvent();

					MapPath way = new MapPath(id);
					while (true) {
						lwxmlReader.readEvent();
						if (!"nd".equals(lwxmlReader.readValue())) {
							break;
						}
						lwxmlReader.readEvent();
						lwxmlReader.readEvent();

						long nodeId = Long.parseLong(lwxmlReader.readValue());
						MapNode node = nodes.get(nodeId);
						if (node == null) {
							// TODO: handle malformed xml
							System.err.println("node null!");
						}

						way.addNode(node);

						lwxmlReader.readEvent();
						lwxmlReader.readEvent();
						lwxmlReader.readEvent();
					}
					ways.add(way);
				}
			}

			event = lwxmlReader.readEvent();
		}

		Map<MapNode, Set<MapNode>> neighborMap = generateNeighborMap(nodes,
				ways);

		if (MapsRacer.DEBUG) {
			System.out.println(nodes.size());
			System.out.println(ways.size());
			System.out.println(neighborMap.size());
		}

		lwxmlReader.close();

		return new OsmMap(ways, neighborMap);
	}

	private Map<MapNode, Set<MapNode>> generateNeighborMap(
			Map<Long, MapNode> nodes, List<MapPath> ways) {
		Map<MapNode, Set<MapNode>> neighborMap = new HashMap<MapNode, Set<MapNode>>();
		for (MapPath way : ways) {
			MapNode lastNode = null;
			for (MapNode mapNode : way.getNodes()) {
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

				lastNode = mapNode;
			}
		}

		return neighborMap;
	}
}
