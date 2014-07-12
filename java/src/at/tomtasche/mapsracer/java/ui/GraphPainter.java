package at.tomtasche.mapsracer.java.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.painter.Painter;

import at.tomtasche.mapsracer.java.map.MapNode;
import at.tomtasche.mapsracer.java.map.MapPath;

public class GraphPainter implements Painter<JXMapViewer> {

	private Collection<MapPath> streets;

	public void initialize(Collection<MapPath> streets) {
		this.streets = streets;
	}

	@Override
	public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
		if (streets == null) {
			return;
		}

		g = (Graphics2D) g.create();

		// convert from viewport to world bitmap
		Rectangle rect = map.getViewportBounds();
		g.translate(-rect.x, -rect.y);

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		// do the drawing
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(4));

		Collection<MapPath> streetsCopy = new ArrayList<>(streets);
		for (MapPath street : streetsCopy) {
			drawStreet(g, map, street);
		}

		// do the drawing again
		g.setColor(Color.RED);
		g.setStroke(new BasicStroke(2));

		for (MapPath street : streetsCopy) {
			drawStreet(g, map, street);
		}

		g.dispose();
	}

	private void drawStreet(Graphics2D g, JXMapViewer map, MapPath street) {
		int lastX = 0;
		int lastY = 0;

		boolean first = true;

		for (MapNode node : street.getNodes()) {
			GeoPosition position = nodeToPosition(node);

			// convert geo-coordinate to world bitmap pixel
			Point2D pt = map.getTileFactory().geoToPixel(position,
					map.getZoom());

			if (first) {
				first = false;
			} else {
				g.drawLine(lastX, lastY, (int) pt.getX(), (int) pt.getY());
			}

			lastX = (int) pt.getX();
			lastY = (int) pt.getY();
		}
	}

	private GeoPosition nodeToPosition(MapNode node) {
		return new GeoPosition(node.getyLat(), node.getxLon());
	}
}