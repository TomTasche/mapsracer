package at.tomtasche.mapsracer.java.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import at.tomtasche.mapsracer.java.map.BoundingBox;
import at.tomtasche.mapsracer.java.map.MapPath;

public class NodeFetcher {

	private static final String QUERY_FORMAT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<osm-script>"
			+ "<query type=\"way\">"
			+ "<has-kv k=\"highway\" />"
			+ "<bbox-query s=\"%f\" w=\"%f\" n=\"%f\" e=\"%f\"/>"
			+ "</query>"
			+ "<union>"
			+ "<item/>"
			+ "<recurse type=\"down\"/>"
			+ "</union>"
			+ "<print mode=\"skeleton\"/>"
			+ "<print order=\"quadtile\"/>"
			+ "<print/>" + "</osm-script>";

	private static final String API_BASE_URL = "http://www.overpass-api.de/api";

	private File cacheDirectory;

	private NodeParser nodeParser;

	public NodeFetcher(File cacheDirectory) throws IOException {
		this.cacheDirectory = cacheDirectory;
		if (!cacheDirectory.exists()) {
			throw new IOException("cache does not exist: "
					+ cacheDirectory.getAbsolutePath());
		}

		nodeParser = new NodeParser();
	}

	private File fetchBoundingBox(BoundingBox boundingBox) throws IOException {
		String url = API_BASE_URL + "/interpreter";

		HttpURLConnection connection = (HttpURLConnection) new URL(url)
				.openConnection();

		String query = String.format(QUERY_FORMAT, boundingBox.getBottom(),
				boundingBox.getLeft(), boundingBox.getTop(),
				boundingBox.getRight());

		try {
			connection.setDoOutput(true);

			OutputStreamWriter writer = new OutputStreamWriter(
					connection.getOutputStream());
			writer.write(query);
			writer.flush();

			File cacheFile = new File(cacheDirectory, "mapsracer-"
					+ System.currentTimeMillis() + ".xml");
			long writtenBytes = Files.copy(connection.getInputStream(),
					cacheFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			if (writtenBytes <= 0) {
				throw new IOException("something went wrong, you know?");
			}

			return cacheFile;
		} finally {
			connection.disconnect();
		}
	}

	private List<MapPath> parseBoundingBox(File cacheFile, Cluster cluster)
			throws IOException {
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(cacheFile);

			return nodeParser.parse(inputStream, cluster);
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}
	}

	protected List<MapPath> getBoundingBox(BoundingBox boundingBox,
			Cluster cluster) throws IOException {
		File cacheFile = fetchBoundingBox(boundingBox);
		List<MapPath> streets = parseBoundingBox(cacheFile, cluster);

		cacheFile.delete();

		return streets;
	}
}
