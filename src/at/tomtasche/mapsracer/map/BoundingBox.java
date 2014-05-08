package at.tomtasche.mapsracer.map;

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
}
