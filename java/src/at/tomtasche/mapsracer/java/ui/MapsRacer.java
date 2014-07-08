package at.tomtasche.mapsracer.java.ui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Map;

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

import at.tomtasche.mapsracer.java.data.ThreadedNodeManager;
import at.tomtasche.mapsracer.java.data.ThreadedNodeManager.NodeManagerListener;
import at.tomtasche.mapsracer.java.gameplay.CarEngine;
import at.tomtasche.mapsracer.java.map.Car;
import at.tomtasche.mapsracer.java.map.MapManager;
import at.tomtasche.mapsracer.java.map.MapNode;

public class MapsRacer {

	public static final boolean DEBUG = true;

	private static JFrame frame;

	private static ThreadedNodeManager nodeManager;

	private static MapManager mapManager;

	private static CarEngine engine;

	private static Thread repaintThread;

	private static Car car;

	public static void main(String[] args) throws IOException {
		File cacheDirectory = new File("cache");
		cacheDirectory.mkdir();

		engine = new CarEngine();

		nodeManager = new ThreadedNodeManager(cacheDirectory);

		mapManager = new MapManager();

		frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosed(WindowEvent e) {
				repaintThread.interrupt();

				super.windowClosed(e);
			}
		});

		mapManager.initialize(nodeManager, engine.getAllCars());

		JXMapViewer mapViewer = mapManager.getMapViewer();

		frame.add(mapViewer);
		frame.setResizable(DEBUG);
		frame.setSize(768, 768);

		// always display window before initializing NodeManager, because
		// otherwise JXMapViewer.getViewportBounds() is empty and we are not
		// able to calculate cluster-sizes
		frame.setVisible(true);

		engine.initialize(nodeManager, mapManager);

		car = new Car();

		nodeManager.setListener(new NodeManagerListener() {

			@Override
			public void initialized() {
				MapNode start = nodeManager.getStreets().iterator().next()
						.getNodes().iterator().next();
				MapNode end = nodeManager.getGraph().get(start).iterator()
						.next();

				car.setVelocity(CarEngine.CAR_VELOCITY);
				car.setFrom(start);
				car.setTo(end);
				car.setDistance(0);

				engine.addCar(car, true);
			}
		});

		nodeManager.initialize(mapManager);

		repaintThread = new Thread() {

			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();

						return;
					}

					frame.repaint();
				}
			}
		};
		repaintThread.start();

		mapViewer.setFocusable(true);
		mapViewer.requestFocusInWindow();
		mapViewer.addKeyListener(new KeyAdapter() {

			boolean halted = false;

			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_LEFT:
					car.setDirection(Math.PI / 2);
					break;
				case KeyEvent.VK_RIGHT:
					car.setDirection(-Math.PI / 2);
					break;
				case KeyEvent.VK_DOWN:
					car.setVelocity(0);
					break;
				case KeyEvent.VK_SPACE:
					if (halted) {
						car.setVelocity(CarEngine.CAR_VELOCITY);
					} else {
						car.setVelocity(0);
					}

					halted = !halted;

					break;
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_LEFT:
				case KeyEvent.VK_RIGHT:
					car.setDirection(0);
					break;
				case KeyEvent.VK_DOWN:
					car.setVelocity(CarEngine.CAR_VELOCITY);
					break;
				}
			}
		});
	}
}
