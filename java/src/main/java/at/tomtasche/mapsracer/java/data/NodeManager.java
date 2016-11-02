package at.tomtasche.mapsracer.java.data;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import at.tomtasche.mapsracer.java.map.MapManager;
import at.tomtasche.mapsracer.java.map.MapNode;
import at.tomtasche.mapsracer.java.map.MapPath;

public interface NodeManager {

	public void initialize(MapManager mapManager);

	public void moveClusters(Direction direction);

	public Collection<Cluster> getClusters();

	public Collection<MapPath> getStreets();

	public Map<MapNode, Set<MapNode>> getGraph();

	public Map<Long, MapNode> getNodes();

	public Cluster getCluster(Direction direction);

	public enum Direction {
		LEFT(1, 0), TOP(0, 1), RIGHT(1, 2), BOTTOM(2, 1), CENTER(1, 1);

		private final int xIndex;
		private final int yIndex;

		Direction(int xIndex, int yIndex) {
			this.xIndex = xIndex;
			this.yIndex = yIndex;
		}

		public int getxIndex() {
			return xIndex;
		}

		public int getyIndex() {
			return yIndex;
		}

		public Direction getOpposite() {
			switch (this) {
			case BOTTOM:
				return TOP;
			case LEFT:
				return RIGHT;
			case RIGHT:
				return LEFT;
			case TOP:
				return BOTTOM;
			case CENTER:
			default:
				return null;
			}
		}
	}
}
