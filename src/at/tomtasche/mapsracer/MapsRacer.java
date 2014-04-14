package at.tomtasche.mapsracer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import at.tomtasche.mapsracer.map.MapConverter;
import at.tomtasche.mapsracer.map.MapNode;
import at.tomtasche.mapsracer.map.MapPath;
import at.tomtasche.mapsracer.map.OsmMap;

public class MapsRacer {
	private static JFrame frame;
	private static MapPanel mapPanel;

	private static OsmMap map;
	private static MeinStern routing;

	public static void main(String[] args) throws FileNotFoundException {
		// http://www.openstreetmap.org/export
		final OsmParser parser = new OsmParser(new File("test.osm"));

		routing = new MeinStern();

		mapPanel = new MapPanel();

		JScrollPane scrollPane = new JScrollPane(mapPanel);

		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.add(scrollPane);
		frame.setSize(512, 512);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setVisible(true);

		new Thread() {

			@Override
			public void run() {
				parser.initialize();

				map = MapConverter.convert(parser);

				routing.initialize(map.getStreetGraph());
				MapNode start = map.getStreets().get(1).getNodes().get(1);
				MapNode end = map.getStreets().get(5).getNodes().get(0);
				List<MapNode> path = routing.search(start, end);
				mapPanel.setPath(new MapPath("route", path));

				mapPanel.setStreets(map.getStreets());

				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						// the map will NOT show all streets completely, because
						// some parts of some streets are actually ouside the
						// bounding box (OSM returns always the complete street,
						// not only parts inside the bounding box)
						mapPanel.setPreferredSize(new Dimension(map.getWidth(),
								map.getHeight()));

						frame.pack();
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

		public void setStreets(List<MapPath> streets) {
			this.streets = streets;
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
