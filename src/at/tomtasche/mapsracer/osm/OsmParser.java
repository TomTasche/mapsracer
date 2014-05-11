package at.tomtasche.mapsracer.osm;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.task.v0_6.RunnableSource;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.xml.v0_6.XmlReader;

import at.tomtasche.mapsracer.ui.MapsRacer;

public class OsmParser {

	private static final List<String> STREET_DISQUALIFIERS;

	static {
		// http://wiki.openstreetmap.org/wiki/Key:highway
		STREET_DISQUALIFIERS = new LinkedList<>();
		STREET_DISQUALIFIERS.add("footway");
		STREET_DISQUALIFIERS.add("bridleway");
		STREET_DISQUALIFIERS.add("steps");
		STREET_DISQUALIFIERS.add("path");
		STREET_DISQUALIFIERS.add("cycleway");
		STREET_DISQUALIFIERS.add("proposed");
		STREET_DISQUALIFIERS.add("construction");
		STREET_DISQUALIFIERS.add("bus_stop");
		STREET_DISQUALIFIERS.add("crossing");
		STREET_DISQUALIFIERS.add("emergency_access_point");
		STREET_DISQUALIFIERS.add("bus_stop");
		STREET_DISQUALIFIERS.add("escape");
		STREET_DISQUALIFIERS.add("give_way");
		STREET_DISQUALIFIERS.add("passing_place");
		STREET_DISQUALIFIERS.add("rest_area");
		STREET_DISQUALIFIERS.add("speed_camera");
		STREET_DISQUALIFIERS.add("street_lamp");
		STREET_DISQUALIFIERS.add("services");
		STREET_DISQUALIFIERS.add("stop");
		STREET_DISQUALIFIERS.add("traffic_signals");
	}

	private File osmFile;

	private double minLat;
	private double minLon;
	private double maxLat;
	private double maxLon;

	private final Map<Long, Node> nodes;
	private final Map<Node, List<Node>> nodeLinks;
	private final List<Way> ways;

	public OsmParser(File osmFile) {
		this.osmFile = osmFile;

		nodes = new HashMap<>();
		nodeLinks = new HashMap<>();
		ways = new LinkedList<>();

		minLat = 0;
		minLon = 0;
		maxLat = 0;
		maxLon = 0;
	}

	public void initialize() {
		Sink sinkImplementation = new Sink() {
			@Override
			public void process(EntityContainer entityContainer) {
				Entity entity = entityContainer.getEntity();
				if (entity instanceof Bound) {
					Bound bound = (Bound) entity;

					minLat = bound.getTop();
					minLon = bound.getLeft();

					maxLat = bound.getBottom();
					maxLon = bound.getRight();
				} else if (entity instanceof Node) {
					Node node = (Node) entity;

					nodes.put(node.getId(), node);
				} else if (entity instanceof Way) {
					Way way = (Way) entity;

					boolean isStreet = false;
					for (Tag tag : way.getTags()) {
						if (tag.getKey().equals("highway")) {
							if (STREET_DISQUALIFIERS.contains(tag.getValue())) {
								break;
							}
							// very likely to be a street

							isStreet = true;
							break;
						}
					}

					if (!isStreet) {
						return;
					}

					ways.add(way);

					List<WayNode> wayNodes = way.getWayNodes();
					for (int i = 0; i < wayNodes.size(); i++) {
						WayNode wayNode = wayNodes.get(i);
						Node node = toNode(wayNode);

						List<Node> links = nodeLinks.get(node);
						if (links == null) {
							links = new LinkedList<>();
							nodeLinks.put(node, links);
						}

						if (i - 1 >= 0) {
							links.add(toNode(wayNodes.get(i - 1)));
						}
						if (i + 1 < wayNodes.size()) {
							links.add(toNode(wayNodes.get(i + 1)));
						}
					}
				} else if (MapsRacer.DEBUG) {
					System.out.println("unknown entity: " + entity.getType());
				}
			}

			@Override
			public void release() {
			}

			@Override
			public void complete() {
			}

			@Override
			public void initialize(Map<String, Object> arg0) {
			}
		};

		RunnableSource reader = new XmlReader(osmFile, false,
				CompressionMethod.None);
		reader.setSink(sinkImplementation);

		reader.run();
	}

	public Node toNode(WayNode wayNode) {
		Node node = nodes.get(wayNode.getNodeId());
		if (node == null) {
			System.err.println("unknown node: " + wayNode.getNodeId());
		}

		return node;
	}

	public Map<Long, Node> getNodes() {
		return nodes;
	}

	public Map<Node, List<Node>> getNodeLinks() {
		return nodeLinks;
	}

	public List<Way> getWays() {
		return ways;
	}

	public double getMinLat() {
		return minLat;
	}

	public double getMaxLat() {
		return maxLat;
	}

	public double getMinLon() {
		return minLon;
	}

	public double getMaxLon() {
		return maxLon;
	}
}
