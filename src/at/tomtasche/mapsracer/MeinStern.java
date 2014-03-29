package at.tomtasche.mapsracer;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openstreetmap.osmosis.core.domain.v0_6.Node;

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
	private Map<Node, List<Node>> graph;
	/*
	 * prevents endless-loops by remembering all previously visited / inspected
	 * / "closed" Things. we won't inspect closed Things a second time.
	 */
	private List<Node> closedThings;
	/*
	 * holds the shortest path currently known to the algorithm. whenever we
	 * find a shorter one we replace this with the shorter one.
	 */
	private List<Node> currentShortestKnownPath;

	public void initialize(Map<Node, List<Node>> graph) {
		this.graph = graph;

		closedThings = new LinkedList<>();
		currentShortestKnownPath = new LinkedList<>();
	}

	public List<Node> search(Node fromThing, Node toThing) {
		closedThings.clear();
		currentShortestKnownPath.clear();

		// remembers the path we were "walking" so far. if we get stuck, we go
		// back step by step until there is a new possible path to walk on
		List<Node> currentPath = new LinkedList<>();
		discover(currentPath, fromThing, toThing);

		// this is the path you're looking for. congratulations!
		List<Node> shortestPath = currentShortestKnownPath;
		System.out.println("shortest path takes " + shortestPath.size()
				+ " hops");

		return shortestPath;
	}

	private void discover(List<Node> currentPath, Node fromThing, Node toThing) {
		// we're now inspecting this Thing, so we close it because we don't want
		// to inspect it another time later on
		closedThings.add(fromThing);

		List<Node> things = graph.get(fromThing);
		for (Node thing : things) {
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