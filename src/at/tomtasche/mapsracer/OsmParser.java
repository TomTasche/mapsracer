package at.tomtasche.mapsracer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.task.v0_6.RunnableSource;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.xml.v0_6.XmlReader;

public class OsmParser {

	public static void main(String[] args) throws FileNotFoundException {
		// create new file using http://www.openstreetmap.org/export
		final MapPanel mapPanel = new MapPanel(new File("test.osm"));

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.add(mapPanel);
		frame.setSize(500, 500);
		frame.setVisible(true);

		new Thread() {

			@Override
			public void run() {
				mapPanel.initialize();
			}
		}.start();
	}

	@SuppressWarnings("serial")
	private static class MapPanel extends JPanel {

		private static final boolean SHOW_NAMES = false;

		private static final List<String> STREET_DISQUALIFIERS;

		static {
			STREET_DISQUALIFIERS = new LinkedList<>();
			STREET_DISQUALIFIERS.add("footway");
			STREET_DISQUALIFIERS.add("steps");
			STREET_DISQUALIFIERS.add("cycleway");
			STREET_DISQUALIFIERS.add("path");
		}

		private File osmFile;

		private double minLat;
		private double minLon;
		private double maxLat;
		private double maxLon;

		private final Map<Long, Node> nodes;
		private final List<Way> ways;

		public MapPanel(File osmFile) {
			this.osmFile = osmFile;

			nodes = new HashMap<>();
			ways = new LinkedList<>();

			minLat = 48;
			minLon = 16;
			maxLat = 0;
			maxLon = 0;
		}

		private void initialize() {
			Sink sinkImplementation = new Sink() {
				@Override
				public void process(EntityContainer entityContainer) {
					Entity entity = entityContainer.getEntity();
					if (entity instanceof Bound) {
						Bound bound = (Bound) entity;

						minLat = bound.getTop();
						minLon = bound.getLeft();

						maxLat = bound.getBottom();
						maxLon = bound.getRight();
					} else if (entity instanceof Node) {
						Node node = (Node) entity;

						nodes.put(node.getId(), node);
					} else if (entity instanceof Way) {
						Way way = (Way) entity;

						boolean isStreet = false;
						for (Tag tag : way.getTags()) {
							if (tag.getKey().equals("highway")) {
								if (STREET_DISQUALIFIERS.contains(tag
										.getValue())) {
									break;
								}
								// very likely to be a street

								isStreet = true;
								break;
							}
						}

						if (!isStreet) {
							return;
						}

						ways.add(way);

						for (WayNode wayNode : way.getWayNodes()) {
							Long nodeId = wayNode.getNodeId();
							Node node = nodes.get(nodeId);
							if (node == null) {
								System.err
										.println("fatal error! unknown node: "
												+ nodeId);

								return;
							}
						}
					} else {
						System.out.println("unknown entity: "
								+ entity.getType());
					}
				}

				@Override
				public void release() {
				}

				@Override
				public void complete() {
				}

				@Override
				public void initialize(Map<String, Object> arg0) {
				}
			};

			RunnableSource reader = new XmlReader(osmFile, false,
					CompressionMethod.None);
			reader.setSink(sinkImplementation);

			reader.run();

			setMinimumSize(new Dimension(calculateX(maxLon), calculateY(maxLat)));

			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					repaint();
				}
			});
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);

			Graphics2D g2d = (Graphics2D) g;
			g2d.setColor(Color.BLACK);

			for (Way way : ways) {
				Node lastNode = null;
				for (WayNode wayNode : way.getWayNodes()) {
					Long nodeId = wayNode.getNodeId();
					Node node = nodes.get(nodeId);
					if (node == null) {
						System.err.println("unknown node: " + nodeId);

						continue;
					}

					if (lastNode != null) {
						g2d.drawLine(calculateX(lastNode.getLongitude()),
								calculateY(lastNode.getLatitude()),
								calculateX(node.getLongitude()),
								calculateY(node.getLatitude()));
					} else {
						String name = "none";
						for (Tag tag : way.getTags()) {
							if (!tag.getKey().equals("name")) {
								continue;
							} else {
								name = tag.getValue();
							}
						}

						if (SHOW_NAMES) {
							g2d.drawString(name,
									calculateX(node.getLongitude()),
									calculateY(node.getLatitude()));
						}
					}

					lastNode = node;
				}
			}
		}

		private int calculateX(double longitude) {
			return (int) distFrom(minLat, minLon, minLat, longitude);
		}

		private int calculateY(double latitude) {
			return (int) distFrom(minLat, minLon, latitude, minLon);
		}

		private double distFrom(double lat1, double lng1, double lat2,
				double lng2) {
			double earthRadius = 3958.75;
			double dLat = Math.toRadians(lat2 - lat1);
			double dLng = Math.toRadians(lng2 - lng1);
			double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
					+ Math.cos(Math.toRadians(lat1))
					* Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2)
					* Math.sin(dLng / 2);
			double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
			double dist = earthRadius * c;

			int meterConversion = 1609;

			return dist * meterConversion;
		}
	}
}
