package at.tomtasche.mapsracer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * some kind of depth-first and dijkstra mixture. this little code snippet is
 * able to find a path between "Things" - points without any distance or similar
 * between each other
 * 
 * @author Thomas Taschauer
 * 
 */
public class EinStern {
	/*
	 * maps the names of a Thing to its connected Things.
	 */
	private Map<String, List<Thing>> graph;
	/*
	 * prevents endless-loops by remembering all previously visited / inspected
	 * / "closed" Things. we won't inspect closed Things a second time.
	 */
	private List<String> closedThings;
	/*
	 * holds the shortest path currently known to the algorithm. whenever we
	 * find a shorter one we replace this with the shorter one.
	 */
	private List<Thing> currentShortestKnownPath;

	public void initialize(List<Thing> things) {
		buildGraph(things);

		closedThings = new LinkedList<String>();
		currentShortestKnownPath = new LinkedList<Thing>();
	}

	public List<Thing> search(Thing fromThing, Thing toThing) {
		closedThings.clear();
		currentShortestKnownPath.clear();

		// remembers the path we were "walking" so far. if we get stuck, we go
		// back step by step until there is a new possible path to walk on
		List<Thing> currentPath = new LinkedList<Thing>();
		discover(currentPath, fromThing.getName(), toThing.getName());

		// this is the path you're looking for. congratulations!
		List<Thing> shortestPath = currentShortestKnownPath;
		System.out.println("shortest path takes " + shortestPath.size()
				+ " hops");

		return shortestPath;
	}

	private void buildGraph(List<Thing> things) {
		graph = new HashMap<String, List<Thing>>();
		for (Thing thing : things) {
			String name = thing.getName();
			List<Thing> connectedThings = graph.get(name);
			if (connectedThings == null) {
				connectedThings = new LinkedList<Thing>();
				graph.put(name, connectedThings);
			}
			connectedThings.add(thing);
		}

		// now you can get all the Things connected to a Thing only by its name
	}

	private void discover(List<Thing> currentPath, String fromThing,
			String toThing) {
		// we're now inspecting this Thing, so we close it because we don't want
		// to inspect it another time later on
		closedThings.add(fromThing);

		List<Thing> things = graph.get(fromThing);
		for (Thing thing : things) {
			String toDevice = thing.getName();
			if (toThing.equals(toDevice)) {
				currentPath.add(thing);

				System.out.println("found path with " + currentPath.size()
						+ " hops");

				if (currentShortestKnownPath.isEmpty()
						|| currentPath.size() < currentShortestKnownPath.size()) {
					currentShortestKnownPath.clear();
					currentShortestKnownPath.addAll(currentPath);
				}
			} else {
				String newFrom = thing.getName();
				if (!closedThings.contains(newFrom)) {
					currentPath.add(thing);

					discover(currentPath, newFrom, toThing);

					currentPath.remove(thing);
				}
			}
		}
	}
}