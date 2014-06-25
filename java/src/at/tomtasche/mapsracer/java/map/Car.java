package at.tomtasche.mapsracer.java.map;

import at.tomtasche.mapsracer.java.math.Vector2d;

public class Car {

	private String id;

	private MapNode from;
	private MapNode to;

	private double distance;
	private double velocity;
	private double direction;

	private Vector2d lastPosition;

	private boolean significant;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public MapNode getFrom() {
		return from;
	}

	public void setFrom(MapNode from) {
		this.from = from;
	}

	public MapNode getTo() {
		return to;
	}

	public void setTo(MapNode to) {
		this.to = to;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public double getVelocity() {
		return velocity;
	}

	public void setVelocity(double velocity) {
		this.velocity = velocity;
	}

	public double getDirection() {
		return direction;
	}

	public void setDirection(double direction) {
		this.direction = direction;
	}

	public Vector2d getLastPosition() {
		return lastPosition;
	}

	public void setLastPosition(Vector2d lastPosition) {
		this.lastPosition = lastPosition;
	}

	public boolean isSignificant() {
		return significant;
	}

	public void setSignificant(boolean significant) {
		this.significant = significant;
	}
}
