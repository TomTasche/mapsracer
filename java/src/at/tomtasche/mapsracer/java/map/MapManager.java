package at.tomtasche.mapsracer.java.map;

import java.awt.Rectangle;
import java.awt.geom.Point2D;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoPosition;

import at.tomtasche.mapsracer.java.data.Cluster;
import at.tomtasche.mapsracer.java.data.NodeManager;
import at.tomtasche.mapsracer.java.data.NodeManager.Direction;
import at.tomtasche.mapsracer.java.math.CoordinateUtil;

public class MapManager {

	private JXMapViewer mapViewer;

	private NodeManager nodeManager;

	public MapManager() {
	}

	public void initialize(JXMapViewer mapViewer, NodeManager nodeManager) {
		this.mapViewer = mapViewer;
		this.nodeManager = nodeManager;
	}

	public void moveViewport(Direction direction) {
		Cluster newCenterCluster = nodeManager.getCluster(direction);
		GeoPosition newCenterPosition = CoordinateUtil
				.calculateMiddle(newCenterCluster.getBoundingBox());

		mapViewer.setCenterPosition(newCenterPosition);
	}

	public Rectangle getViewport() {
		return mapViewer.getViewportBounds();
	}

	public Point2D geoToPixel(GeoPosition position) {
		return mapViewer.getTileFactory().geoToPixel(position,
				mapViewer.getZoom());
	}

	public GeoPosition pixelToGeo(Point2D point) {
		return mapViewer.getTileFactory()
				.pixelToGeo(point, mapViewer.getZoom());
	}
}
