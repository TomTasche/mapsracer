package at.tomtasche.mapsracer.gameplay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.tomtasche.mapsracer.data.Cluster;
import at.tomtasche.mapsracer.map.Car;
import at.tomtasche.mapsracer.map.MapNode;
import at.tomtasche.mapsracer.math.CoordinateUtil;
import at.tomtasche.mapsracer.math.Vector2d;
import at.tomtasche.mapsracer.math.VectorMagic;

public class CarEngine implements Runnable {

	private List<Car> cars;

	private Map<MapNode, Set<MapNode>> graph;
	private Collection<Cluster> clusters;

	private Thread engineThread;

	public CarEngine() {
		this.cars = new LinkedList<>();
	}

	public void initialize(Map<MapNode, Set<MapNode>> graph,
			Collection<Cluster> clusters) {
		this.graph = graph;
		this.clusters = clusters;

		engineThread = new Thread(this);
		engineThread.start();
	}

	public void addCar(Car car) {
		cars.add(car);
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

			Collection<Cluster> clustersCopy = new ArrayList<>(clusters);
			Collection<Car> carsCopy = new ArrayList<>(cars);
			for (Car car : carsCopy) {
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

				for (Cluster cluster : clustersCopy) {
					if (!CoordinateUtil.contains(cluster.getBoundingBox(),
							position.getY(), position.getX())) {
						// TODO: move and reload clusters
					}
				}
			}

			try {
				Thread.sleep(25);
			} catch (InterruptedException e) {
				e.printStackTrace();

				break;
			}
		} while (true);
	}
}
