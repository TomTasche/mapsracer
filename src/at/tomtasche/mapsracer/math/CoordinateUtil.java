package at.tomtasche.mapsracer.math;

import org.jdesktop.swingx.mapviewer.GeoPosition;

public class CoordinateUtil {

	private static final double EARTH_RADIUS_MILES = 3958.75;
	private static final double EARTH_RADIUS_KM = 6371;

	private CoordinateUtil() {
	}

	public static double distance(Vector2d vector) {
		return distance(0, 0, vector.getY(), vector.getX());
	}

	// http://stackoverflow.com/a/837957/198996
	public static double distance(double lat1, double lng1, double lat2,
			double lng2) {
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
				+ Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2)
				* Math.sin(dLng / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double dist = EARTH_RADIUS_MILES * c;

		int meterConversion = 1609;

		return dist * meterConversion;
	}

	/**
	 * http://www.movable-type.co.uk/scripts/latlong.html
	 * 
	 * @param position
	 * @param direction
	 *            in degrees, clockwise from north
	 * @param distance
	 *            in km
	 * @return
	 */
	public static GeoPosition positionAt(GeoPosition position,
			double direction, double distance) {
		double bearing = Math.toRadians(direction);
		
		double latitude1 = Math.toRadians(position.getLatitude());
		double longitude1 = Math.toRadians(position.getLongitude());

		double earthDistance = distance / EARTH_RADIUS_KM;
		double latitude2 = Math.asin(Math.sin(latitude1)
				* Math.cos(earthDistance)
				+ Math.cos(latitude1)
				* Math.sin(earthDistance)
				* Math.cos(bearing));
		double longitude2 = longitude1
				+ Math.atan2(
						Math.sin(bearing)
								* Math.sin(earthDistance)
								* Math.cos(latitude1),
						Math.cos(earthDistance)
								- Math.sin(latitude1)
								* Math.sin(latitude2));
		
		longitude2 = (longitude2 + 3 * Math.PI) % (2 * Math.PI) - Math.PI;

		return new GeoPosition(Math.toDegrees(latitude2), Math.toDegrees(longitude2));
	}
}
