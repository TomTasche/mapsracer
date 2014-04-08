package at.tomtasche.mapsracer;

import java.io.FileNotFoundException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.OSMTileFactoryInfo;
import org.jdesktop.swingx.mapviewer.DefaultTileFactory;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.TileFactoryInfo;
import org.jdesktop.swingx.painter.CompoundPainter;

public class MapsRacer {
	private static JFrame frame;

	private static MeinStern routing;
	private static NodeManager nodeManager;

	private static JXMapViewer mapViewer;
	private static GraphPainter graphPainter;

	public static void main(String[] args) throws FileNotFoundException {
		nodeManager = new NodeManager();

		routing = new MeinStern();

		mapViewer = new JXMapViewer();

		TileFactoryInfo info = new OSMTileFactoryInfo();
		DefaultTileFactory tileFactory = new DefaultTileFactory(info);
		tileFactory.setThreadPoolSize(8);

		mapViewer.setTileFactory(tileFactory);

		mapViewer.setZoom(3);
		// TODO: fetch .osm-file according to this GeoPosition
		mapViewer.setAddressLocation(new GeoPosition(48.1465, 16.3291));

		graphPainter = new GraphPainter();

		CompoundPainter<JXMapViewer> painter = new CompoundPainter<JXMapViewer>(
				graphPainter);
		mapViewer.setOverlayPainter(painter);

		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.add(mapViewer);
		frame.setSize(512, 512);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setVisible(true);

		new Thread() {

			@Override
			public void run() {
				nodeManager.initialize();

				graphPainter.initialize(nodeManager.getStreets());

				// routing.initialize(map.getStreetGraph());
				// MapNode start = map.getStreets().get(1).getNodes().get(1);
				// MapNode end = map.getStreets().get(5).getNodes().get(0);
				// List<MapNode> path = routing.search(start, end);
				// mapPanel.setPath(new MapPath(0, "route", path));

				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						mapViewer.repaint();
					}
				});
			}
		}.start();
	}
}
