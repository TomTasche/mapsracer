package at.tomtasche.mapsracer.gameplay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.tomtasche.mapsracer.map.Car;
import at.tomtasche.mapsracer.map.MapNode;
import at.tomtasche.mapsracer.math.CoordinateUtil;
import at.tomtasche.mapsracer.math.Vector2d;
import at.tomtasche.mapsracer.math.VectorMagic;

public class CarEngine implements Runnable {

	private Map<MapNode, Set<MapNode>> graph;

	private List<Car> cars = new LinkedList<>();

	private Thread engineThread;

	public void initialize(Map<MapNode, Set<MapNode>> graph) {
		this.graph = graph;

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
				Vector2d position = a.add(direction.mul(car.getDistance()
						/ length));

				car.setLastPosition(position);
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
