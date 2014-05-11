package at.tomtasche.mapsracer.data;

import java.util.Collection;
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

	// contains all nodes, and all nodes connected to a node
	private final Map<MapNode, Set<MapNode>> graph;

	// holds ids of currently cached clusters
	private final Cluster[][] clusters;
	private final Collection<Cluster> clusterCollection;

	public NodeCache() {
		this.graph = new HashMap<>();
		this.streets = new HashMap<>();

		this.clusters = new Cluster[3][3];
		this.clusterCollection = new LinkedList<>();
	}

	protected synchronized void addStreet(MapPath newStreet) {
		streets.put(newStreet.getId(), newStreet);

		MapNode lastNode = null;
		for (MapNode node : newStreet.getNodes()) {
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

	protected Cluster getCluster(int xIndex, int yIndex) {
		return clusters[xIndex][yIndex];
	}

	protected void setCluster(Cluster cluster, int xIndex, int yIndex) {
		clusters[xIndex][yIndex] = cluster;

		// TODO: optimize
		clusterCollection.clear();
		for (int i = 0; i < clusters.length; i++) {
			Cluster[] clusterArray = clusters[i];

			for (int j = 0; j < clusterArray.length; j++) {
				clusterCollection.add(clusterArray[j]);
			}
		}
	}

	public Collection<Cluster> getClusters() {
		return clusterCollection;
	}

	protected Map<MapNode, Set<MapNode>> getGraph() {
		return graph;
	}

	protected Map<Long, MapPath> getStreets() {
		return streets;
	}
}
