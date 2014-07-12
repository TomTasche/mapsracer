package at.tomtasche.mapsracer.java.data;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import at.tomtasche.mapsracer.java.map.MapManager;
import at.tomtasche.mapsracer.java.map.MapNode;
import at.tomtasche.mapsracer.java.map.MapPath;

public class ThreadedNodeManager implements NodeManager {

	private SimpleNodeManager nodeManager;

	private ExecutorService threadExecutor;

	private NodeManagerListener listener;

	public ThreadedNodeManager(File cacheDirectory) throws IOException {
		nodeManager = new SimpleNodeManager(cacheDirectory);
	}

	public void setListener(NodeManagerListener listener) {
		this.listener = listener;
	}

	@Override
	public void initialize(final MapManager mapManager) {
		threadExecutor = Executors.newFixedThreadPool(1);

		threadExecutor.execute(new Runnable() {

			@Override
			public void run() {
				nodeManager.initialize(mapManager);

				listener.initialized();
			}
		});
	}

	@Override
	public void moveClusters(final Direction direction) {
		threadExecutor.execute(new Runnable() {

			@Override
			public void run() {
				nodeManager.moveClusters(direction);
			}
		});
	}

	@Override
	public Collection<Cluster> getClusters() {
		return nodeManager.getClusters();
	}

	@Override
	public Collection<MapPath> getStreets() {
		return nodeManager.getStreets();
	}

	@Override
	public Map<MapNode, Set<MapNode>> getGraph() {
		return nodeManager.getGraph();
	}

	@Override
	public Map<Long, MapNode> getNodes() {
		return nodeManager.getNodes();
	}

	@Override
	public Cluster getCluster(Direction direction) {
		return nodeManager.getCluster(direction);
	}

	public interface NodeManagerListener {

		/**
		 * called from a background thread!
		 */
		public void initialized();
	}
}
