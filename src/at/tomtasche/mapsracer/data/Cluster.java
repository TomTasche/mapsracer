package at.tomtasche.mapsracer.data;

import at.tomtasche.mapsracer.data.NodeManager.Direction;
import at.tomtasche.mapsracer.map.BoundingBox;

public class Cluster {

	// relative cluster-coordinates (not lat/lon)
	private final int x;
	private final int y;

	private BoundingBox boundingBox;

	public Cluster(Direction direction, BoundingBox boundingBox) {
		this.x = direction.getxIndex();
		this.y = direction.getyIndex();

		this.boundingBox = boundingBox;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public BoundingBox getBoundingBox() {
		return boundingBox;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		Cluster other = (Cluster) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}
}
