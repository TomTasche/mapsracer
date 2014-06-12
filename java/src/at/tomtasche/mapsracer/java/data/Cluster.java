package at.tomtasche.mapsracer.java.data;

import at.tomtasche.mapsracer.java.map.BoundingBox;

public class Cluster {

	private final int id;

	private final BoundingBox boundingBox;

	public Cluster(BoundingBox boundingBox) {
		this.boundingBox = boundingBox;

		this.id = boundingBox.hashCode();
	}

	public BoundingBox getBoundingBox() {
		return boundingBox;
	}

	public int getId() {
		return id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Cluster [id=" + id + ", boundingBox=" + boundingBox + "]";
	}
}
