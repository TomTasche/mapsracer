package at.tomtasche.mapsracer.java.math;

import java.util.LinkedList;
import java.util.List;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import at.tomtasche.mapsracer.java.map.BoundingBox;

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
	 *            in m
	 * @return
	 */
	public static GeoPosition positionAt(GeoPosition position,
			double direction, double distance) {
		distance /= 1000;

		double bearing = Math.toRadians(direction);

		double latitude1 = Math.toRadians(position.getLatitude());
		double longitude1 = Math.toRadians(position.getLongitude());

		double earthDistance = distance / EARTH_RADIUS_KM;
		double latitude2 = Math.asin(Math.sin(latitude1)
				* Math.cos(earthDistance) + Math.cos(latitude1)
				* Math.sin(earthDistance) * Math.cos(bearing));
		double longitude2 = longitude1
				+ Math.atan2(
						Math.sin(bearing) * Math.sin(earthDistance)
								* Math.cos(latitude1), Math.cos(earthDistance)
								- Math.sin(latitude1) * Math.sin(latitude2));

		longitude2 = (longitude2 + 3 * Math.PI) % (2 * Math.PI) - Math.PI;

		return new GeoPosition(Math.toDegrees(latitude2),
				Math.toDegrees(longitude2));
	}

	public static boolean contains(BoundingBox boundingBox, double lat,
			double lon) {
		return between(boundingBox.getTop(), boundingBox.getBottom(), lat)
				&& between(boundingBox.getLeft(), boundingBox.getRight(), lon);
	}

	private static boolean between(double a, double b, double candidate) {
		double min = Math.min(a, b);
		double max = Math.max(a, b);

		return candidate >= min && candidate <= max;
	}

	public static GeoPosition calculateMiddle(BoundingBox boundingBox) {
		List<GeoPosition> coordinateList = new LinkedList<>();
		coordinateList.add(boundingBox.getTopLeft());
		coordinateList.add(boundingBox.getBottomLeft());
		coordinateList.add(boundingBox.getBottomRight());
		coordinateList.add(boundingBox.getTopRight());

		double middleX = 0;
		double middleY = 0;
		double middleZ = 0;

		for (GeoPosition coordinate : coordinateList) {
			double lat = Math.toRadians(coordinate.getLatitude());
			double lon = Math.toRadians(coordinate.getLongitude());

			double x = Math.cos(lat) * Math.cos(lon);
			double y = Math.cos(lat) * Math.sin(lon);
			double z = Math.sin(lat);

			middleX += x;
			middleY += y;
			middleZ += z;
		}

		int total = coordinateList.size();

		middleX = middleX / total;
		middleY = middleY / total;
		middleZ = middleZ / total;

		double centerLon = Math.atan2(middleY, middleX);
		double centerHyp = Math.sqrt(middleX * middleX + middleY * middleY);
		double centerLat = Math.atan2(middleZ, centerHyp);

		return new GeoPosition(Math.toDegrees(centerLat),
				Math.toDegrees(centerLon));
	}
}
