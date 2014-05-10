package at.tomtasche.mapsracer.map;

import at.tomtasche.mapsracer.math.CoordinateUtil;

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
}
