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

public class MapsRacer {
	private static OsmParser parser;
	private static MeinStern routing;
	private static MapPanel mapPanel;

	public static void main(String[] args) throws FileNotFoundException {
		parser = new OsmParser(new File("test.osm"));

		routing = new MeinStern();

		mapPanel = new MapPanel();

		JFrame frame = new JFrame();
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

		public MapPanel() {
			ways = Collections.emptyList();
			path = Collections.emptyList();
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);

			Graphics2D g2d = (Graphics2D) g;

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

							g2d.drawString(name, parser.calculateX(node),
									parser.calculateY(node));
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
					g2d.drawLine(parser.calculateX(lastNode),
							parser.calculateY(lastNode),
							parser.calculateX(node), parser.calculateY(node));
				}

				lastNode = node;
			}
		}

		public void setWays(List<Way> ways) {
			this.ways = ways;
		}

		public void setPath(List<Node> path) {
			this.path = path;
		}
	}
}
