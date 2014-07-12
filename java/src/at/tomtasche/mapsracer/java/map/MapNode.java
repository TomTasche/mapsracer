package at.tomtasche.mapsracer.java.map;

import at.tomtasche.mapsracer.java.data.Cluster;

public class MapNode {

	private long id;

	// coordinates
	private final double xLon;
	private final double yLat;

	private final Cluster cluster;

	public MapNode(long id, double xLon, double yLat, Cluster cluster) {
		this.id = id;
		this.xLon = xLon;
		this.yLat = yLat;
		this.cluster = cluster;
	}

	public long getId() {
		return id;
	}

	public double getxLon() {
		return xLon;
	}

	public double getyLat() {
		return yLat;
	}

	public Cluster getCluster() {
		return cluster;
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
		MapNode other = (MapNode) obj;
		if (id != other.id)
			return false;
		return true;
	}

}
