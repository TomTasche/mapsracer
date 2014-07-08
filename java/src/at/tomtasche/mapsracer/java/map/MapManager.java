package at.tomtasche.mapsracer.java.map;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.Map;

import javax.swing.event.MouseInputListener;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.OSMTileFactoryInfo;
import org.jdesktop.swingx.input.PanMouseInputListener;
import org.jdesktop.swingx.mapviewer.DefaultTileFactory;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.TileFactoryInfo;
import org.jdesktop.swingx.painter.CompoundPainter;

import at.tomtasche.mapsracer.java.data.Cluster;
import at.tomtasche.mapsracer.java.data.NodeManager;
import at.tomtasche.mapsracer.java.data.NodeManager.Direction;
import at.tomtasche.mapsracer.java.math.CoordinateUtil;
import at.tomtasche.mapsracer.java.ui.CarPainter;
import at.tomtasche.mapsracer.java.ui.ClusterPainter;
import at.tomtasche.mapsracer.java.ui.GraphPainter;
import at.tomtasche.mapsracer.java.ui.MapsRacer;

public class MapManager {

	private JXMapViewer mapViewer;

	private NodeManager nodeManager;

	public MapManager() {
	}

	public void initialize(NodeManager nodeManager, Map<String, Car> allCars) {
		this.nodeManager = nodeManager;

		mapViewer = new JXMapViewer();

		TileFactoryInfo info = new OSMTileFactoryInfo();
		DefaultTileFactory tileFactory = new DefaultTileFactory(info);
		tileFactory.setThreadPoolSize(8);

		mapViewer.setTileFactory(tileFactory);

		mapViewer.setAddressLocation(new GeoPosition(48.14650327493638,
				16.329095363616943));

		mapViewer.setZoom(3);

		CarPainter carPainter = new CarPainter();
		GraphPainter graphPainter = new GraphPainter();
		ClusterPainter clusterPainter = new ClusterPainter();

		CompoundPainter<JXMapViewer> painter = new CompoundPainter<JXMapViewer>();
		painter.addPainter(carPainter);
		if (MapsRacer.DEBUG) {
			painter.addPainter(graphPainter);
			painter.addPainter(clusterPainter);
		}

		carPainter.initialize(allCars);
		graphPainter.initialize(nodeManager.getStreets());
		clusterPainter.initialize(nodeManager.getClusters());

		mapViewer.setOverlayPainter(painter);

		if (MapsRacer.DEBUG) {
			MouseInputListener mouseListener = new PanMouseInputListener(
					mapViewer);
			mapViewer.addMouseListener(mouseListener);
			mapViewer.addMouseMotionListener(mouseListener);
		}
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

	public JXMapViewer getMapViewer() {
		return mapViewer;
	}
}
