package at.tomtasche.mapsracer.data;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jdesktop.swingx.JXMapViewer;

import at.tomtasche.mapsracer.map.MapNode;
import at.tomtasche.mapsracer.map.MapPath;

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
	public void initialize(final JXMapViewer mapViewer) {
		threadExecutor = Executors.newFixedThreadPool(5);

		threadExecutor.execute(new Runnable() {

			@Override
			public void run() {
				nodeManager.initialize(mapViewer);

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
