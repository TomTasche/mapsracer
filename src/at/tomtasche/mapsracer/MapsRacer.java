package at.tomtasche.mapsracer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.LinkedList;
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
				mapPanel.setMap(map);

				routing.initialize(map.getNeighborMap());
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

				Car car = new Car();
				car.setVelocity(40);
				car.setDirection(Math.PI / 2);
				car.setFrom(start);
				car.setTo(map.getNeighborMap().get(start).iterator().next());
				car.setDistance(0);
				mapPanel.addCar(car);
			}
		}.start();
	}

	@SuppressWarnings("serial")
	private static class MapPanel extends JPanel {
		private static final boolean SHOW_NAMES = false;

		private OsmMap map;

		private MapPath path;
		private List<MapPath> streets;

		private long lastNano = -1;
		private List<Car> cars = new LinkedList<>();

		private double xScale;
		private double yScale;

		public MapPanel() {
			streets = Collections.emptyList();

			xScale = 1;
			yScale = 1;
		}

		public void setMap(OsmMap map) {
			this.map = map;
		}

		public void addCar(Car car) {
			cars.add(car);
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

			if (map != null) {
				g2d.setColor(Color.GREEN);

				long tmp = System.nanoTime();
				if (lastNano == -1)
					lastNano = tmp;
				double time = (tmp - lastNano) * 0.000000001;
				lastNano = tmp;

				for (Car car : cars) {
					double newDistance = car.getDistance() + car.getVelocity()
							* time;

					while (true) {
						double distance = VectorMagic.direction(car.getFrom(),
								car.getTo()).length();
						if (newDistance >= distance) {
							MapNode from = car.getTo();
							MapNode to = VectorMagic.crossing(car.getFrom(),
									car.getTo(),
									map.getNeighborMap().get(car.getTo()),
									car.getDirection());
							car.setFrom(from);
							car.setTo(to);
							newDistance -= distance;
						} else {
							break;
						}
					}

					car.setDistance(newDistance);

					Vector2d a = new Vector2d(calculateScaledX(car.getFrom()),
							calculateScaledY(car.getFrom()));
					Vector2d b = new Vector2d(calculateScaledX(car.getTo()),
							calculateScaledY(car.getTo()));
					Vector2d direction = b.sub(a);
					double length = direction.length();
					Vector2d position = a.add(direction.mul(car.getDistance()
							/ length));
					g2d.fillOval((int) (position.getX() - 10),
							(int) (position.getY() - 10), 20, 20);
				}
			}

			repaint(20);
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

	private static class Car {
		private MapNode from;
		private MapNode to;
		private double distance;
		private double velocity;
		private double direction;

		public MapNode getFrom() {
			return from;
		}

		public void setFrom(MapNode from) {
			this.from = from;
		}

		public MapNode getTo() {
			return to;
		}

		public void setTo(MapNode to) {
			this.to = to;
		}

		public double getDistance() {
			return distance;
		}

		public void setDistance(double distance) {
			this.distance = distance;
		}

		public double getVelocity() {
			return velocity;
		}

		public void setVelocity(double velocity) {
			this.velocity = velocity;
		}

		public double getDirection() {
			return direction;
		}

		public void setDirection(double direction) {
			this.direction = direction;
		}
	}

}
