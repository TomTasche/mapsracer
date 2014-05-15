package at.tomtasche.mapsracer.ui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.WindowConstants;
import javax.swing.event.MouseInputListener;

import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.OSMTileFactoryInfo;
import org.jdesktop.swingx.input.PanMouseInputListener;
import org.jdesktop.swingx.mapviewer.DefaultTileFactory;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.TileFactoryInfo;
import org.jdesktop.swingx.painter.CompoundPainter;

import at.tomtasche.mapsracer.data.NodeManager;
import at.tomtasche.mapsracer.gameplay.CarEngine;
import at.tomtasche.mapsracer.map.Car;
import at.tomtasche.mapsracer.map.MapNode;

public class MapsRacer {

	public static final boolean DEBUG = true;

	private static JFrame frame;

	private static NodeManager nodeManager;

	private static CarEngine engine;

	private static JXMapViewer mapViewer;
	private static CarPainter carPainter;
	private static GraphPainter graphPainter;
	private static ClusterPainter clusterPainter;

	private static Thread repaintThread;

	public static void main(String[] args) throws IOException {
		File cacheDirectory = new File("cache");
		cacheDirectory.mkdir();

		engine = new CarEngine();

		nodeManager = new NodeManager(cacheDirectory);

		mapViewer = new JXMapViewer();

		TileFactoryInfo info = new OSMTileFactoryInfo();
		DefaultTileFactory tileFactory = new DefaultTileFactory(info);
		tileFactory.setThreadPoolSize(8);

		mapViewer.setTileFactory(tileFactory);

		mapViewer.setAddressLocation(new GeoPosition(48.14650327493638,
				16.329095363616943));

		mapViewer.setZoom(3);

		carPainter = new CarPainter();
		graphPainter = new GraphPainter();
		clusterPainter = new ClusterPainter();

		CompoundPainter<JXMapViewer> painter = new CompoundPainter<JXMapViewer>();
		painter.addPainter(carPainter);
		if (DEBUG) {
			painter.addPainter(graphPainter);
			painter.addPainter(clusterPainter);
		}

		mapViewer.setOverlayPainter(painter);

		if (DEBUG) {
			MouseInputListener mouseListener = new PanMouseInputListener(
					mapViewer);
			mapViewer.addMouseListener(mouseListener);
			mapViewer.addMouseMotionListener(mouseListener);
		}

		frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.add(mapViewer);
		frame.setResizable(DEBUG);
		frame.setSize(768, 768);

		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosed(WindowEvent e) {
				repaintThread.interrupt();

				super.windowClosed(e);
			}
		});

		// always display window before initializing NodeManager, because
		// otherwise JXMapViewer.getViewportBounds() is empty and we are not
		// able to calculate cluster-sizes
		frame.setVisible(true);

		repaintThread = new Thread() {

			@Override
			public void run() {
				engine.initialize(nodeManager.getGraph());

				carPainter.initialize();
				graphPainter.initialize(nodeManager.getStreets());
				clusterPainter.initialize(nodeManager.getClusters());

				nodeManager.initialize(mapViewer);

				MapNode start = nodeManager.getStreets().iterator().next()
						.getNodes().iterator().next();
				MapNode end = nodeManager.getGraph().get(start).iterator()
						.next();

				final Car car = new Car();
				car.setVelocity(100);
				car.setFrom(start);
				car.setTo(end);
				car.setDistance(0);

				engine.addCar(car);
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
						Thread.sleep(100);
					} catch (InterruptedException e1) {
						e1.printStackTrace();

						return;
					}

					frame.repaint();
				}
			}
		};
		repaintThread.start();
	}
}
