package at.tomtasche.mapsracer.data;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.tomtasche.mapsracer.map.MapConverter;
import at.tomtasche.mapsracer.map.MapNode;
import at.tomtasche.mapsracer.map.MapPath;
import at.tomtasche.mapsracer.osm.OsmMap;
import at.tomtasche.mapsracer.osm.OsmParser;

public class NodeManager {

	private static final boolean AGGRESSIVE_CLEANUP = false;

	private NodeFetcher fetcher;
	private NodeCache cache;

	public NodeManager(File cacheDirectory) throws IOException {
		this.fetcher = new NodeFetcher(cacheDirectory);
		this.cache = new NodeCache();
	}

	public void initialize() {
		fetchCluster(Direction.MIDDLE);

		// TODO:
		// updateClusters();
	}

	public void moveClusters(Direction direction) {
		if (direction == Direction.MIDDLE) {
			System.err.println("what are you doing?");
			return;
		}

		moveCluster(Direction.MIDDLE, direction);
		moveCluster(direction.getOpposite(), Direction.MIDDLE);

		updateClusters();

		cache.cleanup(AGGRESSIVE_CLEANUP);
	}

	private void updateClusters() {
		for (Direction direction : Direction.values()) {
			if (getCluster(direction) == null) {
				fetchCluster(direction);
			}
		}
	}

	private void fetchCluster(Direction direction) {
		switch (direction) {
		case MIDDLE:
			break;
		case BOTTOM:
		case LEFT:
		case RIGHT:
		case TOP:
			// TODO: load new cluster

			// there is always a cluster in the middle!
			Cluster middleCluster = getCluster(Direction.MIDDLE);

			return;
		}

		try {
			List<MapPath> streets = fetcher.getBoundingBox(16.31919, 48.15234, 16.33346, 48.14856);
			for (MapPath street : streets) {
				cache.addStreet(street);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Cluster getCluster(Direction direction) {
		return cache.getCluster(direction.getxIndex(), direction.getyIndex());
	}

	private void setCluster(Direction direction, Cluster cluster) {
		cache.setCluster(cluster, direction.getxIndex(), direction.getyIndex());
	}

	private void moveCluster(Direction fromDirection, Direction toDirection) {
		Cluster cluster = getCluster(fromDirection);
		setCluster(toDirection, cluster);

		setCluster(fromDirection, null);
	}

	public MapPath getStreet(long id) {
		return cache.getStreets().get(id);
	}

	public Collection<MapPath> getStreets() {
		return Collections.unmodifiableCollection(cache.getStreets().values());
	}

	public Map<MapNode, Set<MapNode>> getGraph() {
		return Collections.unmodifiableMap(cache.getGraph());
	}

	public enum Direction {
		LEFT(0, 1), TOP(1, 0), RIGHT(2, 1), BOTTOM(1, 2), MIDDLE(1, 1);

		private final int xIndex;
		private final int yIndex;

		Direction(int xIndex, int yIndex) {
			this.xIndex = xIndex;
			this.yIndex = yIndex;
		}

		public int getxIndex() {
			return xIndex;
		}

		public int getyIndex() {
			return yIndex;
		}

		public Direction getOpposite() {
			switch (this) {
			case BOTTOM:
				return TOP;
			case LEFT:
				return RIGHT;
			case RIGHT:
				return LEFT;
			case TOP:
				return BOTTOM;
			case MIDDLE:
			default:
				return null;
			}
		}
	}
}
