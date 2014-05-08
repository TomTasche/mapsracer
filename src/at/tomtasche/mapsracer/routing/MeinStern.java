package at.tomtasche.mapsracer.routing;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.tomtasche.mapsracer.map.MapNode;

/**
 * some kind of depth-first and dijkstra mixture. this little code snippet is
 * able to find a path between "Things" - points without any distance or similar
 * between each other
 * 
 * @author Thomas Taschauer
 * 
 */
public class MeinStern {
	/*
	 * maps the names of a Thing to its connected Things.
	 */
	private Map<MapNode, Set<MapNode>> graph;
	/*
	 * prevents endless-loops by remembering all previously visited / inspected
	 * / "closed" Things. we won't inspect closed Things a second time.
	 */
	private Set<MapNode> closedThings;
	/*
	 * holds the shortest path currently known to the algorithm. whenever we
	 * find a shorter one we replace this with the shorter one.
	 */
	private List<MapNode> currentShortestKnownPath;

	public void initialize(Map<MapNode, Set<MapNode>> graph) {
		this.graph = graph;

		closedThings = new HashSet<>();
		currentShortestKnownPath = new LinkedList<>();
	}

	public List<MapNode> search(MapNode fromThing, MapNode toThing) {
		closedThings.clear();
		currentShortestKnownPath.clear();

		// remembers the path we were "walking" so far. if we get stuck, we go
		// back step by step until there is a new possible path to walk on
		List<MapNode> currentPath = new LinkedList<>();
		discover(currentPath, fromThing, toThing);

		// this is the path you're looking for. congratulations!
		List<MapNode> shortestPath = currentShortestKnownPath;
		System.out.println("shortest path takes " + shortestPath.size()
				+ " hops");

		return shortestPath;
	}

	private void discover(List<MapNode> currentPath, MapNode fromThing, MapNode toThing) {
		// we're now inspecting this Thing, so we close it because we don't want
		// to inspect it another time later on
		closedThings.add(fromThing);

		Set<MapNode> things = graph.get(fromThing);
		for (MapNode thing : things) {
			if (toThing.equals(thing)) {
				currentPath.add(thing);

				System.out.println("found path with " + currentPath.size()
						+ " hops");

				if (currentShortestKnownPath.isEmpty()
						|| currentPath.size() < currentShortestKnownPath.size()) {
					currentShortestKnownPath.clear();
					currentShortestKnownPath.addAll(currentPath);
				}
			} else {
				if (!closedThings.contains(thing)) {
					currentPath.add(thing);

					discover(currentPath, thing, toThing);

					currentPath.remove(thing);
				}
			}
		}
	}
}