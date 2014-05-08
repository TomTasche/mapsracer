package at.tomtasche.mapsracer.data;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoPosition;

import at.tomtasche.mapsracer.map.BoundingBox;
import at.tomtasche.mapsracer.map.MapNode;
import at.tomtasche.mapsracer.map.MapPath;

public class NodeManager {

	private static final boolean AGGRESSIVE_CLEANUP = false;

	private final GeoPosition originPosition;

	private NodeFetcher fetcher;
	private NodeCache cache;

	private boolean initialized;

	private JXMapViewer mapViewer;

	public NodeManager(File cacheDirectory, GeoPosition originPosition)
			throws IOException {
		this.originPosition = originPosition;

		this.fetcher = new NodeFetcher(cacheDirectory);
		this.cache = new NodeCache();
	}

	/**
	 * @param mapViewer
	 *            has to be fully initialized at the time of calling this
	 *            method. Especially getViewportBounds has to return sane data
	 */
	public void initialize(JXMapViewer mapViewer) {
		this.mapViewer = mapViewer;

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

	private BoundingBox calculateBoundingBox(Point2D middle) {
		// TODO: only works for middle-cluster right now

		Rectangle viewportBounds = mapViewer.getViewportBounds();

		Point2D upperLeftPoint = new Point2D.Double(viewportBounds.getMinX(),
				viewportBounds.getMinY());
		GeoPosition topLeftPosition = pixelToGeo(upperLeftPoint);

		Point2D bottomRightPoint = new Point2D.Double(viewportBounds.getMaxX(),
				viewportBounds.getMaxY());
		GeoPosition bottomRightPosition = pixelToGeo(bottomRightPoint);

		return new BoundingBox(topLeftPosition.getLongitude(),
				topLeftPosition.getLatitude(),
				bottomRightPosition.getLongitude(),
				bottomRightPosition.getLatitude());
	}

	private void fetchCluster(Direction direction) {
		switch (direction) {
		case MIDDLE:
			// only called once at initialization!
			if (initialized) {
				throw new RuntimeException(
						"fetchCluster for Direction.MIDDLE called twice. this should never happen!");
			}
			initialized = true;

			try {
				Point2D centerPoint = geoToPixel(originPosition);

				BoundingBox centerBoundingBox = calculateBoundingBox(centerPoint);

				Cluster centerCluster = new Cluster(0, 0, centerBoundingBox);
				setCluster(Direction.MIDDLE, centerCluster);

				List<MapPath> streets = fetcher
						.getBoundingBox(centerBoundingBox);
				for (MapPath street : streets) {
					cache.addStreet(street);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			break;
		case BOTTOM:
		case LEFT:
		case RIGHT:
		case TOP:
			// there is always a cluster in the middle after initialization!
			Cluster middleCluster = getCluster(Direction.MIDDLE);

			// TODO: load new cluster

			return;
		}
	}

	private Point2D geoToPixel(GeoPosition position) {
		return mapViewer.getTileFactory().geoToPixel(position,
				mapViewer.getZoom());
	}

	private GeoPosition pixelToGeo(Point2D point) {
		return mapViewer.getTileFactory()
				.pixelToGeo(point, mapViewer.getZoom());
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
