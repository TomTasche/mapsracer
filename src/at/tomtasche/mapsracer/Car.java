package at.tomtasche.mapsracer;

import at.tomtasche.mapsracer.map.MapNode;

public class Car {

	private MapNode from;
	private MapNode to;
	private double distance;
	private double velocity;
	private double direction;

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
}
