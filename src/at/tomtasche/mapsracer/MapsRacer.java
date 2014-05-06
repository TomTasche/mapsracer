package at.tomtasche.mapsracer;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;

import javax.swing.*;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.OSMTileFactoryInfo;
import org.jdesktop.swingx.mapviewer.DefaultTileFactory;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.TileFactoryInfo;
import org.jdesktop.swingx.painter.CompoundPainter;

import at.tomtasche.mapsracer.map.MapNode;

public class MapsRacer {

	private static final boolean DEBUG_DRAW_GRAPH = false;

	private static JFrame frame;

	private static MeinStern routing;
	private static NodeManager nodeManager;

	private static JXMapViewer mapViewer;
	private static GraphPainter graphPainter;
	private static CarPainter carPainter;

	private static Thread thread;

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

		carPainter = new CarPainter();
		graphPainter = new GraphPainter();

		CompoundPainter<JXMapViewer> painter = new CompoundPainter<JXMapViewer>();
		painter.addPainter(carPainter);
		if (DEBUG_DRAW_GRAPH) {
			painter.addPainter(graphPainter);
		}

		mapViewer.setOverlayPainter(painter);

		frame = new JFrame();
		frame.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
		frame.setLocationRelativeTo(null);
		frame.add(mapViewer);
		frame.setSize(512, 512);

		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosed(WindowEvent e) {
				thread.interrupt();

				super.windowClosed(e);
			}
		});

		thread = new Thread() {

			@Override
			public void run() {
				nodeManager.initialize();

				MapNode start = nodeManager.getStreets().iterator().next()
						.getNodes().iterator().next();
				MapNode end = nodeManager.getGraph().get(start).iterator()
						.next();

				graphPainter.initialize(nodeManager.getStreets());
				carPainter.initialize(nodeManager.getGraph());

				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						mapViewer.repaint();

						frame.setVisible(true);
					}
				});

				final Car car = new Car();
				car.setVelocity(100);
				car.setFrom(start);
				car.setTo(end);
				car.setDistance(0);

				carPainter.addCar(car);

				frame.addKeyListener(new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						switch (e.getKeyCode()) {
						case KeyEvent.VK_LEFT:
							car.setDirection(-Math.PI / 2);
							break;
						case KeyEvent.VK_RIGHT:
							car.setDirection(Math.PI / 2);
							break;
						case KeyEvent.VK_ENTER:
							frame.repaint();
							break;
						}
					}

					public void keyReleased(KeyEvent e) {
						switch (e.getKeyCode()) {
						case KeyEvent.VK_LEFT:
						case KeyEvent.VK_RIGHT:
							car.setDirection(0);
							break;
						}
					}
				});

				while (true) {
					try {
						Thread.sleep(50);
					} catch (InterruptedException e1) {
						e1.printStackTrace();

						return;
					}

					frame.repaint();
				}
			}
		};
		thread.start();
	}
}
