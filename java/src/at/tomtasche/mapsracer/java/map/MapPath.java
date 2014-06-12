package at.tomtasche.mapsracer.java.map;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import at.tomtasche.mapsracer.java.data.Cluster;

public class MapPath {

	private final long id;
	private final String name;

	private final List<MapNode> nodes;
	private final Set<Cluster> clusters;

	public MapPath(long id) {
		this(id, "unknown");
	}

	public MapPath(long id, String name) {
		this(id, name, new LinkedList<MapNode>());
	}

	public MapPath(long id, String name, List<MapNode> nodes) {
		this.id = id;
		this.name = name;

		this.nodes = nodes;

		clusters = new HashSet<>();
		for (MapNode node : nodes) {
			clusters.add(node.getCluster());
		}
	}

	public void addNode(MapNode newNode) {
		nodes.add(newNode);

		clusters.add(newNode.getCluster());
	}

	public void addNodes(Collection<MapNode> newNodes) {
		for (MapNode node : newNodes) {
			if (!nodes.contains(node)) {
				nodes.add(node);

				clusters.add(node.getCluster());
			}
		}
	}

	public Collection<MapNode> getNodes() {
		return Collections.unmodifiableCollection(nodes);
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Set<Cluster> getClusters() {
		return Collections.unmodifiableSet(clusters);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MapPath other = (MapPath) obj;
		if (id != other.id)
			return false;
		return true;
	}
}
