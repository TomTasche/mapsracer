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
import at.tomtasche.mapsracer.math.CoordinateUtil;

public class NodeManager {

	private static final boolean AGGRESSIVE_CLEANUP = false;

	private NodeFetcher fetcher;
	private NodeCache cache;

	private boolean initialized;

	private JXMapViewer mapViewer;

	public NodeManager(File cacheDirectory) throws IOException {
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

		fetchCluster(Direction.CENTER);

		updateClusters();
	}

	public void moveClusters(Direction direction) {
		if (direction == Direction.CENTER) {
			System.err.println("what are you doing?");
			return;
		}

		moveCluster(Direction.CENTER, direction);
		moveCluster(direction.getOpposite(), Direction.CENTER);

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

	private BoundingBox toBoundingBox(Rectangle viewportBounds) {
		Point2D topLeftPoint = new Point2D.Double(viewportBounds.getMinX(),
				viewportBounds.getMinY());
		GeoPosition topLeftPosition = pixelToGeo(topLeftPoint);

		Point2D bottomRightPoint = new Point2D.Double(viewportBounds.getMaxX(),
				viewportBounds.getMaxY());
		GeoPosition bottomRightPosition = pixelToGeo(bottomRightPoint);

		return boundingBoxFromGeoPositions(topLeftPosition, bottomRightPosition);
	}

	private BoundingBox boundingBoxFromGeoPositions(
			GeoPosition topLeftPosition, GeoPosition bottomRightPosition) {
		return new BoundingBox(topLeftPosition.getLongitude(),
				topLeftPosition.getLatitude(),
				bottomRightPosition.getLongitude(),
				bottomRightPosition.getLatitude());
	}

	private double directionToDirection(Direction direction) {
		switch (direction) {
		case BOTTOM:
			return 180;
		case LEFT:
			return 270;
		case RIGHT:
			return 90;
		case TOP:
			return 0;
		default:
			throw new RuntimeException("unknwon direction: " + direction);
		}
	}

	private BoundingBox calculateBoundingBox(Cluster center, Direction direction) {
		if (Direction.CENTER == direction) {
			return center.getBoundingBox();
		}

		BoundingBox oldBoundingBox = center.getBoundingBox();

		double oldWidth = oldBoundingBox.getWidth();
		double oldHeight = oldBoundingBox.getHeight();

		GeoPosition oldTopLeftPosition = new GeoPosition(
				oldBoundingBox.getTop(), oldBoundingBox.getLeft());
		GeoPosition oldTopRightPosition = new GeoPosition(
				oldBoundingBox.getTop(), oldBoundingBox.getRight());
		GeoPosition oldBottomRightPosition = new GeoPosition(
				oldBoundingBox.getBottom(), oldBoundingBox.getRight());
		GeoPosition oldBottomLeftPosition = new GeoPosition(
				oldBoundingBox.getBottom(), oldBoundingBox.getLeft());

		GeoPosition newTopLeftPosition = null;
		GeoPosition newBottomRightPosition = null;
		switch (direction) {
		case BOTTOM:
			// y-height
			newTopLeftPosition = oldBottomLeftPosition;

			newBottomRightPosition = CoordinateUtil.positionAt(
					oldBottomRightPosition, directionToDirection(direction),
					oldHeight);

			break;
		case LEFT:
			// x-width
			newTopLeftPosition = CoordinateUtil.positionAt(oldTopLeftPosition,
					directionToDirection(direction), oldWidth);

			newBottomRightPosition = oldBottomLeftPosition;

			break;
		case RIGHT:
			// x+width
			newTopLeftPosition = oldTopRightPosition;

			newBottomRightPosition = CoordinateUtil.positionAt(
					oldBottomRightPosition, directionToDirection(direction),
					oldHeight);

			break;
		case TOP:
			// y+width
			newTopLeftPosition = CoordinateUtil.positionAt(oldTopLeftPosition,
					directionToDirection(direction), oldWidth);

			newBottomRightPosition = oldTopRightPosition;

			break;
		default:
			throw new RuntimeException("unknwon direction: " + direction);
		}

		BoundingBox newBoundingBox = boundingBoxFromGeoPositions(
				newTopLeftPosition, newBottomRightPosition);

		System.out.println("old width: " + oldWidth + " vs new width: "
				+ newBoundingBox.getWidth());
		System.out.println("old height: " + oldHeight + " vs new height: "
				+ newBoundingBox.getHeight());

		return newBoundingBox;
	}

	private void fetchCluster(Direction direction) {
		BoundingBox boundingBox;
		if (direction == Direction.CENTER) {
			// only called once at initialization!
			if (initialized) {
				throw new RuntimeException("fetchCluster for " + direction
						+ " called twice. this should never happen!");
			}
			initialized = true;

			boundingBox = toBoundingBox(mapViewer.getViewportBounds());
		} else {
			// there is always a cluster in the center after initialization!
			Cluster centerCluster = getCluster(Direction.CENTER);

			boundingBox = calculateBoundingBox(centerCluster, direction);
		}

		Cluster cluster = new Cluster(direction, boundingBox);
		setCluster(direction, cluster);

		try {
			List<MapPath> streets = fetcher
					.getBoundingBox(boundingBox, cluster);
			for (MapPath street : streets) {
				cache.addStreet(street);
			}
		} catch (IOException e) {
			if (Direction.CENTER == direction) {
				throw new RuntimeException(
						"could not initialize NodeManager: loading "
								+ direction + " failed", e);
			} else {
				// TODO: retry?
				e.printStackTrace();
			}
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
		LEFT(0, 1), TOP(1, 0), RIGHT(2, 1), BOTTOM(1, 2), CENTER(1, 1);

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
			case CENTER:
			default:
				return null;
			}
		}
	}
}
