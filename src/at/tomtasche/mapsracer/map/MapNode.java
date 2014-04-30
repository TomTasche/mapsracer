package at.tomtasche.mapsracer.map;

import at.tomtasche.mapsracer.Cluster;

public class MapNode {

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
}
