package at.tomtasche.mapsracer.map;

import at.tomtasche.mapsracer.Cluster;

public class MapNode {

	private final int x;
	private final int y;
	private final Cluster cluster;

	public MapNode(int x, int y, Cluster cluster) {
		this.x = x;
		this.y = y;
		this.cluster = cluster;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public Cluster getCluster() {
		return cluster;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + cluster.hashCode();
		result = prime * result + x;
		result = prime * result + y;
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
		MapNode other = (MapNode) obj;
		if (!cluster.equals(other.cluster))
			return false;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}
}
