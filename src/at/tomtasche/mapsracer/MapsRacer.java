package at.tomtasche.mapsracer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.OSMTileFactoryInfo;
import org.jdesktop.swingx.mapviewer.DefaultTileFactory;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.TileFactoryInfo;

import at.tomtasche.mapsracer.map.MapConverter;
import at.tomtasche.mapsracer.map.MapNode;
import at.tomtasche.mapsracer.map.MapPath;

public class MapsRacer {
	private static JFrame frame;
	private static MapPanel mapPanel;

	private static MeinStern routing;
	private static NodeManager nodeManager;

	public static void main(String[] args) throws FileNotFoundException {
		nodeManager = new NodeManager();

		routing = new MeinStern();

		mapPanel = new MapPanel();

		JScrollPane scrollPane = new JScrollPane(mapPanel);
		
		JXMapViewer mapViewer = new JXMapViewer();

		TileFactoryInfo info = new OSMTileFactoryInfo();
		DefaultTileFactory tileFactory = new DefaultTileFactory(info);
		tileFactory.setThreadPoolSize(8);

		mapViewer.setTileFactory(tileFactory);

		mapViewer.setZoom(3);
		// TODO: fetch .osm-file according to this GeoPosition
		mapViewer.setAddressLocation(new GeoPosition(48.1768, 16.3498));
		
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

				mapPanel.setStreets(nodeManager.getStreets());

				// routing.initialize(map.getStreetGraph());
				// MapNode start = map.getStreets().get(1).getNodes().get(1);
				// MapNode end = map.getStreets().get(5).getNodes().get(0);
				// List<MapNode> path = routing.search(start, end);
				// mapPanel.setPath(new MapPath(0, "route", path));

				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						mapPanel.repaint();
					}
				});
			}
		}.start();
	}

	@SuppressWarnings("serial")
	private static class MapPanel extends JPanel {
		private static final boolean SHOW_NAMES = false;

		private MapPath path;
		private List<MapPath> streets;

		private double xScale;
		private double yScale;

		public MapPanel() {
			streets = Collections.emptyList();

			xScale = 1;
			yScale = 1;
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);

			Graphics2D g2d = (Graphics2D) g;

			g2d.scale(xScale, xScale);

			g2d.setColor(Color.BLACK);
			for (MapPath street : streets) {
				drawPath(street, g2d);
			}

			g2d.setColor(Color.RED);

			if (path != null) {
				drawPath(path, g2d);
			}
		}

		private void drawPath(MapPath path, Graphics2D g2d) {
			MapNode lastNode = null;
			for (MapNode node : path.getNodes()) {
				if (lastNode != null) {
					boolean nameDrawn = false;
					if (!nameDrawn) {
						if (SHOW_NAMES) {
							g2d.drawString(path.getName(),
									calculateScaledX(node),
									calculateScaledY(node));
						}

						nameDrawn = true;
					}

					g2d.drawLine(calculateScaledX(lastNode),
							calculateScaledY(lastNode), calculateScaledX(node),
							calculateScaledY(node));
				}

				lastNode = node;
			}
		}

		private int calculateScaledX(MapNode node) {
			int rawX = node.getX();
			double scaledX = rawX * xScale;
			return (int) scaledX;
		}

		private int calculateScaledY(MapNode node) {
			int rawY = node.getY();
			double scaledY = rawY * yScale;
			return (int) scaledY;
		}

		public void setStreets(Collection<MapPath> streets) {
			this.streets = new ArrayList<>(streets);
		}

		public void setPath(MapPath path) {
			this.path = path;
		}

		public void setxScale(double xScale) {
			this.xScale = xScale;
		}

		public void setyScale(double yScale) {
			this.yScale = yScale;
		}
	}
}
