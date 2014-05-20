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
import at.tomtasche.mapsracer.ui.MapsRacer;

// need a better name for this...
public class SimpleNodeManager implements NodeManager {

	private static final boolean AGGRESSIVE_CLEANUP = false;

	private NodeFetcher fetcher;
	private NodeCache cache;

	private boolean initialized;

	// TODO: remove this direct dependency on UI (only needed for getting
	// map-dimensions and viewport
	private JXMapViewer mapViewer;

	public SimpleNodeManager(File cacheDirectory) throws IOException {
		this.fetcher = new NodeFetcher(cacheDirectory);
		this.cache = new NodeCache();
	}

	/**
	 * @param mapViewer
	 *            has to be fully initialized at the time of calling this
	 *            method. Especially getViewportBounds has to return sane data
	 */
	@Override
	public void initialize(JXMapViewer mapViewer) {
		this.mapViewer = mapViewer;

		fetchCluster(Direction.CENTER);

		updateClusters();
	}

	@Override
	public void moveClusters(Direction direction) {
		if (direction == Direction.CENTER) {
			System.err.println("what are you doing?");
			return;
		}

		Cluster toCluster = getCluster(direction);
		Cluster centerCluster = getCluster(Direction.CENTER);

		setCluster(Direction.CENTER, toCluster);
		setCluster(direction.getOpposite(), centerCluster);

		for (Direction oneDirection : Direction.values()) {
			if (oneDirection != direction.getOpposite()
					&& oneDirection != Direction.CENTER) {
				// delete all remaining clusters but CENTER (already set)
				setCluster(oneDirection, null);
			}
		}

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

		GeoPosition oldTopLeftPosition = oldBoundingBox.getTopLeft();
		GeoPosition oldTopRightPosition = oldBoundingBox.getTopRight();
		GeoPosition oldBottomRightPosition = oldBoundingBox.getBottomRight();
		GeoPosition oldBottomLeftPosition = oldBoundingBox.getBottomLeft();

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

		if (MapsRacer.DEBUG) {
			System.out.println("old width: " + oldWidth + " vs new width: "
					+ newBoundingBox.getWidth());
			System.out.println("old height: " + oldHeight + " vs new height: "
					+ newBoundingBox.getHeight());
		}

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

		Cluster cluster = new Cluster(boundingBox);
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

		if (MapsRacer.DEBUG) {
			System.out.println("finished loading " + direction);
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

	@Override
	public Cluster getCluster(Direction direction) {
		return cache.getCluster(direction.getxIndex(), direction.getyIndex());
	}

	private void setCluster(Direction direction, Cluster cluster) {
		cache.setCluster(cluster, direction.getxIndex(), direction.getyIndex());
	}

	private void moveCluster(Direction fromDirection, Direction toDirection) {
		Cluster fromCluster = getCluster(fromDirection);
		Cluster toCluster = getCluster(toDirection);

		setCluster(toDirection, fromCluster);
		setCluster(fromDirection, toCluster);
	}

	@Override
	public Collection<Cluster> getClusters() {
		return Collections.unmodifiableCollection(cache.getClusters());
	}

	@Override
	public Collection<MapPath> getStreets() {
		return Collections.unmodifiableCollection(cache.getStreets().values());
	}

	@Override
	public Map<MapNode, Set<MapNode>> getGraph() {
		return Collections.unmodifiableMap(cache.getGraph());
	}
}
