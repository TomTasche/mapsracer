package at.tomtasche.mapsracer.java.gameplay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import at.tomtasche.mapsracer.java.data.Cluster;
import at.tomtasche.mapsracer.java.data.NodeManager;
import at.tomtasche.mapsracer.java.data.NodeManager.Direction;
import at.tomtasche.mapsracer.java.map.Car;
import at.tomtasche.mapsracer.java.map.MapManager;
import at.tomtasche.mapsracer.java.map.MapNode;
import at.tomtasche.mapsracer.java.math.CoordinateUtil;
import at.tomtasche.mapsracer.java.math.Vector2d;
import at.tomtasche.mapsracer.java.math.VectorMagic;
import at.tomtasche.mapsracer.java.ui.MapsRacer;

public class CarEngine implements Runnable {

	private Car significantCar;
	private Map<String, Car> allCars;

	private NodeManager nodeManager;
	private Map<MapNode, Set<MapNode>> graph;

	private MapManager mapManager;

	private Thread engineThread;

	public CarEngine() {
		this.allCars = new HashMap<>();
	}

	public void initialize(NodeManager nodeManager, MapManager mapManager) {
		this.nodeManager = nodeManager;
		this.mapManager = mapManager;

		this.graph = nodeManager.getGraph();

		engineThread = new Thread(this);
		engineThread.start();
	}

	public Map<String, Car> getAllCars() {
		return allCars;
	}

	public void addCar(Car car, boolean signifcant) {
		if (signifcant) {
			if (significantCar != null) {
				throw new IllegalArgumentException(
						"significant car already set!");
			}

			String id = UUID.randomUUID().toString();
			car.setId(id);

			car.setSignificant(true);

			significantCar = car;
		}

		if (car.getId() == null) {
			throw new IllegalArgumentException("no id set for car");
		}

		allCars.put(car.getId(), car);
	}

	@Override
	public void run() {
		long lastNano = -1;

		do {
			long tmp = System.nanoTime();
			if (lastNano == -1) {
				lastNano = tmp;
			}

			double time = (tmp - lastNano) * 0.000000001;

			lastNano = tmp;

			Collection<Car> carsCopy = new ArrayList<>();
			// TODO: this is not possible currently, because devices fetched
			// from the server don't have from- and to-fields set
			// carsCopy.addAll(allCars.values());
			carsCopy.add(significantCar);
			for (Car car : carsCopy) {
				if (car == null) {
					continue;
				}

				double newDistance = car.getDistance() + car.getVelocity()
						* time;

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

				Vector2d a = new Vector2d(car.getFrom().getxLon(), car
						.getFrom().getyLat());
				Vector2d b = new Vector2d(car.getTo().getxLon(), car.getTo()
						.getyLat());
				Vector2d direction = b.sub(a);
				double length = CoordinateUtil.distance(direction);

				// swap x and y to get lat, lon
				Vector2d position = a.add(direction.mul(car.getDistance()
						/ length));

				car.setLastPosition(position);
			}

			if (significantCar != null) {
				Cluster centerCluster = nodeManager
						.getCluster(Direction.CENTER);

				if (!clusterContainsCar(centerCluster, significantCar)) {
					Direction moveDirection = null;
					for (Direction direction : Direction.values()) {
						Cluster cluster = nodeManager.getCluster(direction);
						if (cluster != null
								&& clusterContainsCar(cluster, significantCar)) {
							moveDirection = direction;
							break;
						}
					}

					if (moveDirection == null) {
						String message = "signifcant car left all loaded clusters!";
						if (MapsRacer.DEBUG) {
							System.out.println(message);

							continue;
						} else {
							throw new RuntimeException(message);
						}
					}

					if (MapsRacer.DEBUG) {
						System.out.println("significant car moved to "
								+ moveDirection);
					}

					mapManager.moveViewport(moveDirection);

					nodeManager.moveClusters(moveDirection);
				}

				try {
					Vector2d position = significantCar.getLastPosition();
					position = position.setYX(position);
					// TODO: make async
					pushPosition(significantCar.getId(), position);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			/*
			 * try { Thread.sleep(25); } catch (InterruptedException e) {
			 * e.printStackTrace();
			 * 
			 * break; }
			 */
			// TODO: make async, sleep thread for 25ms
			try {
				fetchPositions();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} while (true);
	}

	private boolean clusterContainsCar(Cluster cluster, Car car) {
		Vector2d position = car.getLastPosition();

		return CoordinateUtil.contains(cluster.getBoundingBox(),
				position.getY(), position.getX());
	}

	private void pushPosition(String id, Vector2d position) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL(
				"https://mapsracer.appspot.com/position").openConnection();
		connection.setRequestMethod("POST");
		connection.setDoOutput(true);

		String parameters = "lat=" + position.getX() + "&lon="
				+ position.getY() + "&id=" + id;

		connection.getOutputStream().write(
				parameters.getBytes(Charset.forName("UTF-8")));
		connection.getOutputStream().flush();

		if (MapsRacer.DEBUG) {
			System.out.println("pushed with code: "
					+ connection.getResponseCode());
		}
	}

	private void fetchPositions() throws IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL(
				"https://mapsracer.appspot.com/position").openConnection();
		connection.setRequestMethod("GET");

		InputStreamReader streamReader = new InputStreamReader(
				connection.getInputStream());
		BufferedReader bufferedReader = new BufferedReader(streamReader);
		for (String s = bufferedReader.readLine(); s != null; s = bufferedReader
				.readLine()) {
			String[] splitString = s.split(";");

			String id = splitString[0];
			if (significantCar != null && significantCar.getId().equals(id)) {
				continue;
			}

			double lat = Double.parseDouble(splitString[1]);
			double lon = Double.parseDouble(splitString[2]);

			Vector2d position = new Vector2d(lat, lon);

			position = position.setYX(position);

			Car car = allCars.get(id);
			if (car == null) {
				if (MapsRacer.DEBUG) {
					System.out.println("new car joined with id: " + id);
				}

				car = new Car();
				car.setId(id);

				allCars.put(id, car);
			}

			car.setLastPosition(position);

			if (MapsRacer.DEBUG) {
				System.out.println("updated position for id: " + id);
			}
		}

	}
}
