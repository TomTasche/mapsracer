package at.tomtasche.mapsracer.java.data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import at.tomtasche.mapsracer.java.gameplay.CarEngine;
import at.tomtasche.mapsracer.java.map.Car;
import at.tomtasche.mapsracer.java.map.MapNode;
import at.tomtasche.mapsracer.java.math.Vector2d;
import at.tomtasche.mapsracer.java.ui.MapsRacer;

public class OnlineCarEngine implements Runnable {

	private Map<String, Car> allCars;

	private Map<Long, MapNode> nodes;

	private Thread networkThread;

	public void initialize(NodeManager nodeManager, Map<String, Car> allCars) {
		this.allCars = allCars;

		this.nodes = nodeManager.getNodes();

		networkThread = new Thread(this, "OnlineCarEngine");
		networkThread.start();
	}

	public Map<String, Car> getAllCars() {
		return allCars;
	}

	@Override
	public void run() {
		do {
			Collection<Car> carsCopy = new ArrayList<>();
			carsCopy.addAll(allCars.values());
			for (Car car : carsCopy) {
				if (!car.isSignificant()) {
					continue;
				}

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				try {
					pushPosition(car);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				try {
					fetchPositions(car);
				} catch (IOException e) {
					if (e instanceof FileNotFoundException) {
						if (MapsRacer.DEBUG) {
							System.out.println("no positions on server");
						}
					} else {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		} while (true);
	}

	private void pushPosition(Car significantCar) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL(
				"https://mapsracer.appspot.com/position").openConnection();
		connection.setRequestMethod("POST");
		connection.setDoOutput(true);

		Vector2d position = significantCar.getLastPosition();
		position = position.setYX(position);

		String parameters = "lat=" + position.getX() + "&lon="
				+ position.getY() + "&id=" + significantCar.getId() + "&from="
				+ significantCar.getFrom().getId() + "&to="
				+ significantCar.getTo().getId();

		connection.getOutputStream().write(
				parameters.getBytes(Charset.forName("UTF-8")));
		connection.getOutputStream().flush();
		connection.getOutputStream().close();

		if (MapsRacer.DEBUG) {
			System.out.println("pushed with code: "
					+ connection.getResponseCode());
		}

		connection.getInputStream().close();

		connection.disconnect();
	}

	private void fetchPositions(Car significantCar) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL(
				"https://mapsracer.appspot.com/position").openConnection();
		connection.setRequestMethod("GET");

		InputStreamReader streamReader = new InputStreamReader(
				connection.getInputStream());
		BufferedReader bufferedReader = new BufferedReader(streamReader);
		for (String s = bufferedReader.readLine(); s != null; s = bufferedReader
				.readLine()) {
			String[] splitString = s.split(";");
			if (splitString.length < 5) {
				continue;
			}

			String id = splitString[0];
			if (significantCar != null && significantCar.getId().equals(id)) {
				continue;
			}

			double lat = Double.parseDouble(splitString[1]);
			double lon = Double.parseDouble(splitString[2]);

			long from = Long.parseLong(splitString[3]);
			long to = Long.parseLong(splitString[4]);

			MapNode fromNode = nodes.get(from);
			MapNode toNode = nodes.get(to);

			Vector2d position = new Vector2d(lat, lon);

			position = position.setYX(position);

			Car car = allCars.get(id);
			if (car == null) {
				if (MapsRacer.DEBUG) {
					System.out.println("new car joined with id: " + id);
				}

				car = new Car();
				car.setId(id);
				car.setVelocity(CarEngine.CAR_VELOCITY);

				allCars.put(id, car);
			}

			car.setLastPosition(position);

			car.setFrom(fromNode);
			car.setTo(toNode);

			if (MapsRacer.DEBUG) {
				System.out.println("updated position for id: " + id);
			}
		}

		bufferedReader.close();
		streamReader.close();

		connection.disconnect();
	}
}
