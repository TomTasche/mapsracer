package at.tomtasche.mapsracer.java.map;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import at.tomtasche.mapsracer.java.math.CoordinateUtil;

public class BoundingBox {

	// coordinates
	private final double left;
	private final double top;
	private final double right;
	private final double bottom;

	public BoundingBox(double left, double top, double right, double bottom) {
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}

	public double getLeft() {
		return left;
	}

	public double getTop() {
		return top;
	}

	public double getRight() {
		return right;
	}

	public double getBottom() {
		return bottom;
	}

	public GeoPosition getTopLeft() {
		return new GeoPosition(top, left);
	}

	public GeoPosition getTopRight() {
		return new GeoPosition(top, right);
	}

	public GeoPosition getBottomRight() {
		return new GeoPosition(bottom, right);
	}

	public GeoPosition getBottomLeft() {
		return new GeoPosition(bottom, left);
	}

	/**
	 * @return height of this BoundingBox in meter
	 */
	public double getHeight() {
		// top-left to bottom-left
		return CoordinateUtil.distance(top, left, bottom, left);
	}

	/**
	 * @return width of this BoundingBox in meter
	 */
	public double getWidth() {
		// top-left to top-right
		return CoordinateUtil.distance(top, left, top, right);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(bottom);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(left);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(right);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(top);
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
		BoundingBox other = (BoundingBox) obj;
		if (Double.doubleToLongBits(bottom) != Double
				.doubleToLongBits(other.bottom))
			return false;
		if (Double.doubleToLongBits(left) != Double
				.doubleToLongBits(other.left))
			return false;
		if (Double.doubleToLongBits(right) != Double
				.doubleToLongBits(other.right))
			return false;
		if (Double.doubleToLongBits(top) != Double.doubleToLongBits(other.top))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "BoundingBox [left=" + left + ", top=" + top + ", right="
				+ right + ", bottom=" + bottom + "]";
	}
}
