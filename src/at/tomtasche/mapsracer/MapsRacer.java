package at.tomtasche.mapsracer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;

import at.tomtasche.mapsracer.map.MapConverter;

public class MapsRacer {
	private static JFrame frame;
	private static MapPanel mapPanel;

	private static OsmParser parser;
	private static MapConverter converter;
	private static MeinStern routing;

	public static void main(String[] args) throws FileNotFoundException {
		parser = new OsmParser(new File("test.osm"));

		routing = new MeinStern();

		mapPanel = new MapPanel();

		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.add(mapPanel);
		frame.setSize(1024, 1024);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setVisible(true);

		new Thread() {

			@Override
			public void run() {
				parser.initialize();

				routing.initialize(parser.getNodeLinks());

				Node start = parser.toNode(parser.getWays().get(1)
						.getWayNodes().get(1));
				Node end = parser.toNode(parser.getWays().get(5).getWayNodes()
						.get(0));

				List<Node> path = routing.search(start, end);

				mapPanel.setWays(parser.getWays());
				mapPanel.setPath(path);

				// TODO: broken
				// Dimension frameSize = frame.getSize();
				// double yScale = (frameSize.height * 1.0)
				// / parser.calculateY(parser.getMaxLat());
				// mapPanel.setyScale(yScale);
				//
				// // TODO: remove
				// mapPanel.setxScale(yScale);
				// // mapPanel.setxScale((frameSize.width * 0.5)
				// // / parser.calculateX(parser.getMaxLon()));

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

		private List<Node> path;
		private List<Way> ways;

		private double xScale;
		private double yScale;

		public MapPanel() {
			ways = Collections.emptyList();
			path = Collections.emptyList();

			xScale = 1;
			yScale = 1;
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);

			Graphics2D g2d = (Graphics2D) g;

			g2d.scale(xScale, xScale);

			g2d.setColor(Color.BLACK);
			for (Way way : ways) {
				boolean nameDrawn = false;
				List<Node> pathNodes = new LinkedList<>();
				for (WayNode wayNode : way.getWayNodes()) {
					Node node = parser.toNode(wayNode);
					pathNodes.add(node);

					if (!nameDrawn) {
						if (SHOW_NAMES) {
							String name = "unknown name";
							for (Tag tag : way.getTags()) {
								if (!tag.getKey().equals("name")) {
									continue;
								} else {
									name = tag.getValue();
								}
							}

							g2d.drawString(name, calculateScaledX(node),
									calculateScaledY(node));
						}

						nameDrawn = true;
					}
				}

				drawPath(pathNodes, g2d);
			}

			g2d.setColor(Color.RED);
			drawPath(path, g2d);
		}

		private void drawPath(List<Node> pathNodes, Graphics2D g2d) {
			Node lastNode = null;

			for (Node node : pathNodes) {
				if (lastNode != null) {
					g2d.drawLine(calculateScaledX(lastNode),
							calculateScaledY(lastNode), calculateScaledX(node),
							calculateScaledY(node));
				}

				lastNode = node;
			}
		}

		private int calculateScaledX(Node node) {
			int rawX = parser.calculateX(node);
			// double scaledX = rawX * xScale;
			return (int) rawX;
		}

		private int calculateScaledY(Node node) {
			int rawY = parser.calculateY(node);
			// double scaledY = rawY * yScale;
			return (int) rawY;
		}

		public void setWays(List<Way> ways) {
			this.ways = ways;
		}

		public void setPath(List<Node> path) {
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
