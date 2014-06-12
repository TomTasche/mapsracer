package at.tomtasche.mapsracer.java.map;

import at.tomtasche.mapsracer.java.data.Cluster;

public class MapNode {

	// coordinates
	private final double xLon;
	private final double yLat;

	private final Cluster cluster;

	public MapNode(double xLon, double yLat, Cluster cluster) {
		this.xLon = xLon;
		this.yLat = yLat;
		this.cluster = cluster;
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
		result = prime * result + ((cluster == null) ? 0 : cluster.hashCode());
		long temp;
		temp = Double.doubleToLongBits(xLon);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(yLat);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		if (cluster == null) {
			if (other.cluster != null)
				return false;
		} else if (!cluster.equals(other.cluster))
			return false;
		if (Double.doubleToLongBits(xLon) != Double
				.doubleToLongBits(other.xLon))
			return false;
		if (Double.doubleToLongBits(yLat) != Double
				.doubleToLongBits(other.yLat))
			return false;
		return true;
	}
}
