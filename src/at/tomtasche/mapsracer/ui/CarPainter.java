package at.tomtasche.mapsracer.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.painter.Painter;

import at.tomtasche.mapsracer.map.Car;
import at.tomtasche.mapsracer.map.MapNode;
import at.tomtasche.mapsracer.math.CoordinateUtil;
import at.tomtasche.mapsracer.math.Vector2d;
import at.tomtasche.mapsracer.math.VectorMagic;

public class CarPainter implements Painter<JXMapViewer> {

	private Map<MapNode, Set<MapNode>> graph;

	private long lastNano = -1;
	private List<Car> cars = new LinkedList<>();

	private BufferedImage carIcon;

	public void initialize(Map<MapNode, Set<MapNode>> graph) {
		this.graph = graph;

		try {
			carIcon = ImageIO.read(new File("car.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addCar(Car car) {
		cars.add(car);
	}

	@Override
	public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
		if (graph == null) {
			return;
		}

		g = (Graphics2D) g.create();

		// convert from viewport to world bitmap
		Rectangle rect = map.getViewportBounds();
		g.translate(-rect.x, -rect.y);

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		long tmp = System.nanoTime();
		if (lastNano == -1) {
			lastNano = tmp;
		}

		double time = (tmp - lastNano) * 0.000000001;

		lastNano = tmp;

		Collection<Car> carsCopy = new ArrayList<>(cars);
		for (Car car : carsCopy) {
			double newDistance = car.getDistance() + car.getVelocity() * time;

			while (true) {
				Vector2d direction = VectorMagic.direction(car.getFrom(),
						car.getTo());

				double distance = CoordinateUtil.distance(direction);

				if (newDistance >= distance) {
					MapNode from = car.getTo();
					MapNode to = VectorMagic.crossing(car.getFrom(),
							car.getTo(), graph.get(car.getTo()),
							car.getDirection());
					car.setFrom(from);
					car.setTo(to);
					newDistance -= distance;
				} else {
					break;
				}
			}

			car.setDistance(newDistance);

			Vector2d a = new Vector2d(car.getFrom().getxLon(), car.getFrom()
					.getyLat());
			Vector2d b = new Vector2d(car.getTo().getxLon(), car.getTo()
					.getyLat());
			Vector2d direction = b.sub(a);
			double length = CoordinateUtil.distance(direction);
			Vector2d position = a
					.add(direction.mul(car.getDistance() / length));

			GeoPosition geoPosition = new GeoPosition(position.getY(),
					position.getX());

			Point2D pt = map.getTileFactory().geoToPixel(geoPosition,
					map.getZoom());

			if (carIcon != null) {
				g.drawImage(carIcon, (int) (pt.getX() - 20),
						(int) (pt.getY() - 20), null);
			} else {
				g.fillOval((int) (pt.getX() - 10), (int) (pt.getY() - 10), 20,
						20);
			}

			if (MapsRacer.DEBUG) {
				geoPosition = new GeoPosition(car.getFrom().getyLat(), car
						.getFrom().getxLon());

				pt = map.getTileFactory()
						.geoToPixel(geoPosition, map.getZoom());

				g.setColor(Color.GREEN);
				g.fillOval((int) (pt.getX() - 10), (int) (pt.getY() - 10), 20,
						20);

				geoPosition = new GeoPosition(car.getTo().getyLat(), car
						.getTo().getxLon());

				pt = map.getTileFactory()
						.geoToPixel(geoPosition, map.getZoom());

				g.setColor(Color.BLUE);
				g.fillOval((int) (pt.getX() - 10), (int) (pt.getY() - 10), 20,
						20);
			}
		}

		g.dispose();
	}
}