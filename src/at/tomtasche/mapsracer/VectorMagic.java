package at.tomtasche.mapsracer;

import java.util.Iterator;
import java.util.Set;

import at.tomtasche.mapsracer.map.MapNode;

public class VectorMagic {

	public static double angle(Vector2d a, Vector2d b) {
		return MathUtil.atan2(a) - MathUtil.atan2(b);
	}

	public static Vector2d direction(MapNode a, MapNode b) {
		return new Vector2d(b.getX() - a.getX(), b.getY() - a.getY());
	}

	public static MapNode crossing(MapNode from, MapNode to,
			Set<MapNode> neighbors, double direction) {
		if (neighbors.size() == 1) {
			return from;
		} else if (neighbors.size() == 2) {
			Iterator<MapNode> iterator = neighbors.iterator();
			MapNode tmp = iterator.next();
			if (tmp == to) {
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
				double angle = angle(origin, way);
				double distance = Math.abs(direction - angle);
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