package at.tomtasche.mapsracer.java.math;

import java.util.Iterator;
import java.util.Set;

import at.tomtasche.mapsracer.java.map.MapNode;

public class VectorMagic {

	public static double angle(Vector2d a, Vector2d b) {
		return MathUtil.atan2(b) - MathUtil.atan2(a);
	}

	public static double normalize(double angle) {
		angle %= 2 * Math.PI;
		if (angle < 0) {
			angle += 2 * Math.PI;
		}
		return angle;
	}

	public static double distance(double a, double b) {
		a = normalize(a);
		b = normalize(b);
		double dif = Math.abs(a - b);
		if (dif < Math.PI) {
			return dif;
		} else {
			return 2 * Math.PI - dif;
		}
	}

	public static Vector2d direction(MapNode a, MapNode b) {
		return new Vector2d(b.getxLon() - a.getxLon(), b.getyLat()
				- a.getyLat());
	}

	public static MapNode crossing(MapNode from, MapNode to,
			Set<MapNode> neighbors, double direction) {
		direction = normalize(direction);

		if (neighbors.size() == 1) {
			return from;
		} else if (neighbors.size() == 2) {
			Iterator<MapNode> iterator = neighbors.iterator();
			MapNode tmp = iterator.next();
			if (tmp == from) {
				return iterator.next();
			} else {
				return tmp;
			}
		} else if (neighbors.size() > 2) {
			Vector2d origin = direction(from, to);
			MapNode result = null;
			double min = Double.MAX_VALUE;

			for (MapNode neighbor : neighbors) {
				Vector2d way = direction(to, neighbor);
				double angle = normalize(angle(origin, way));
				double distance = distance(direction, angle);
				if (min > distance) {
					result = neighbor;
					min = distance;
				}
			}

			return result;
		} else {
			throw new IllegalArgumentException("illegal neighbor count");
		}
	}

}