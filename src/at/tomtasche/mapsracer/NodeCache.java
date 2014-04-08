package at.tomtasche.mapsracer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.tomtasche.mapsracer.map.MapNode;
import at.tomtasche.mapsracer.map.MapPath;

public class NodeCache {

	// only used for quick iteration over all streets
	private final Map<Long, MapPath> streets;

	// contains all nodes, and all streets that pass a node
	private final Map<MapNode, Set<MapPath>> graph;

	// holds ids of currently cached clusters
	private final Cluster[][] clusters;

	public NodeCache() {
		this.graph = new HashMap<>();
		this.streets = new HashMap<>();

		this.clusters = new Cluster[3][3];
	}

	protected synchronized void addStreet(MapPath newStreet) {
		MapPath cachedStreet = streets.get(newStreet.getId());
		if (cachedStreet != null) {
			cachedStreet.addNodes(newStreet.getNodes());
		} else {
			for (MapNode node : newStreet.getNodes()) {
				Set<MapPath> paths = graph.get(node);
				if (paths == null) {
					paths = new HashSet<>();
					graph.put(node, paths);
				}

				paths.add(newStreet);
			}

			streets.put(newStreet.getId(), newStreet);
		}
	}

	protected synchronized void cleanup(boolean aggro) {
		if (!aggro) {
			// inspect only whole streets and continue as soon as the street
			// contains only one node that's assigned to a currently cached
			// cluster
			for (MapPath street : streets.values()) {
				if (!isActive(street)) {
					streets.remove(street.getId());
				}
			}
		} else {
			// inspect EACH node and check its clusterId
			List<MapNode> nodeGarbage = new LinkedList<>();
			List<MapPath> streetGarbage = new LinkedList<>();
			for (MapPath street : streets.values()) {
				for (MapNode node : street.getNodes()) {
					if (!isActive(node)) {
						nodeGarbage.add(node);
					}
				}

				if (!isActive(street)) {
					streetGarbage.add(street);
				}
			}

			for (MapNode node : nodeGarbage) {
				graph.remove(node);
			}

			for (MapPath street : streetGarbage) {
				streets.remove(street.getId());
			}
		}
	}

	private boolean isActive(MapPath street) {
		for (Cluster cluster : street.getClusters()) {
			if (clustersContain(cluster)) {
				return true;
			}
		}

		return false;
	}

	private boolean isActive(MapNode node) {
		return clustersContain(node.getCluster());
	}

	private boolean clustersContain(Cluster cluster) {
		for (int i = 0; i < clusters.length; i++) {
			for (int j = 0; j < clusters.length; j++) {
				if (clusters[i][j].equals(cluster)) {
					return true;
				}
			}
		}

		return false;
	}

	protected Cluster[][] getClusters() {
		return clusters;
	}

	protected Map<MapNode, Set<MapPath>> getGraph() {
		return graph;
	}

	protected Map<Long, MapPath> getStreets() {
		return streets;
	}
}
