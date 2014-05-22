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

import javax.imageio.ImageIO;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.painter.Painter;

import at.tomtasche.mapsracer.map.Car;
import at.tomtasche.mapsracer.math.Vector2d;

public class CarPainter implements Painter<JXMapViewer> {

	private List<Car> cars;

	private BufferedImage carIcon;

	public CarPainter() {
		this.cars = new LinkedList<>();
	}

	public void initialize() {
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
		g = (Graphics2D) g.create();

		// convert from viewport to world bitmap
		Rectangle rect = map.getViewportBounds();
		g.translate(-rect.x, -rect.y);

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		Collection<Car> carsCopy = new ArrayList<>(cars);
		for (Car car : carsCopy) {
			Vector2d lastPosition = car.getLastPosition();
			GeoPosition geoPosition = new GeoPosition(lastPosition.getY(),
					lastPosition.getX());

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