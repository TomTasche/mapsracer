package at.tomtasche.mapsracer.java.data;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.tomtasche.mapsracer.java.map.MapNode;
import at.tomtasche.mapsracer.java.map.MapPath;

public class NodeCache {

	// only used for quick iteration over all streets
	private final Map<Long, MapPath> streets;

	// only used for quick iteration over all nodes
	private final Map<Long, MapNode> nodes;

	// contains all nodes, and all nodes connected to a node
	// TODO: use weakreference?
	private final Map<MapNode, Set<MapNode>> graph;

	// holds ids of currently cached clusters
	private final Cluster[][] clusters;
	private final Collection<Cluster> clusterCollection;

	public NodeCache() {
		Map<MapNode, Set<MapNode>> tempGraph = new HashMap<>();
		this.graph = Collections.synchronizedMap(tempGraph);

		Map<Long, MapPath> tempStreets = new HashMap<>();
		this.streets = Collections.synchronizedMap(tempStreets);

		Map<Long, MapNode> tempNodes = new HashMap<>();
		this.nodes = Collections.synchronizedMap(tempNodes);

		this.clusters = new Cluster[3][3];

		Collection<Cluster> tempClusterCollection = new LinkedList<>();
		this.clusterCollection = Collections
				.synchronizedCollection(tempClusterCollection);
	}

	protected synchronized void addStreet(MapPath newStreet) {
		streets.put(newStreet.getId(), newStreet);

		MapNode lastNode = null;
		for (MapNode node : newStreet.getNodes()) {
			MapNode previous = nodes.put(node.getId(), node);
			if (previous != null && !previous.equals(node)) {
				System.out
						.println("data structure corrupted. unexpected behavior to be exptected now!");

				continue;
			}

			Set<MapNode> neighbors = graph.get(node);

			if (neighbors == null) {
				neighbors = new HashSet<>();
				graph.put(node, neighbors);
			}

			if (lastNode != null) {
				// add left
				neighbors.add(lastNode);
				// add right
				graph.get(lastNode).add(node);
			}

			lastNode = node;
		}
	}

	protected synchronized void cleanup(boolean aggressive) {
		if (!aggressive) {
			// inspect only whole streets and continue as soon as the street
			// contains only one node that's assigned to a currently cached
			// cluster

			Collection<MapPath> removeStreets = new LinkedList<>();
			for (MapPath street : streets.values()) {
				if (!isActive(street)) {
					removeStreets.add(street);
				}
			}

			for (MapPath removeStreet : removeStreets) {
				streets.remove(removeStreet.getId());

				// TODO: delete associated nodes
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
				nodes.remove(node.getId());
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
				Cluster tempCluster = clusters[i][j];
				if (tempCluster != null && tempCluster.equals(cluster)) {
					return true;
				}
			}
		}

		return false;
	}

	protected synchronized Cluster getCluster(int xIndex, int yIndex) {
		return clusters[xIndex][yIndex];
	}

	protected synchronized void setCluster(Cluster cluster, int xIndex,
			int yIndex) {
		clusters[xIndex][yIndex] = cluster;

		// TODO: optimize
		clusterCollection.clear();
		for (int i = 0; i < clusters.length; i++) {
			Cluster[] clusterArray = clusters[i];

			for (int j = 0; j < clusterArray.length; j++) {
				Cluster temp = clusterArray[j];
				if (temp == null) {
					continue;
				}

				clusterCollection.add(temp);
			}
		}
	}

	public synchronized Collection<Cluster> getClusters() {
		return clusterCollection;
	}

	protected Map<MapNode, Set<MapNode>> getGraph() {
		return graph;
	}

	protected Map<Long, MapPath> getStreets() {
		return streets;
	}

	public Map<Long, MapNode> getNodes() {
		return nodes;
	}
}
