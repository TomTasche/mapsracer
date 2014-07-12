package at.tomtasche.mapsracer.engine;

import java.io.Serializable;

public class Position implements Serializable {

	private static final long serialVersionUID = -643640682461196951L;

	public double lat;
	public double lon;

	public long from;
	public long to;
}