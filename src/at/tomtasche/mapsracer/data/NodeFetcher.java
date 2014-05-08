package at.tomtasche.mapsracer.data;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import at.tomtasche.mapsracer.map.BoundingBox;
import at.tomtasche.mapsracer.map.MapConverter;
import at.tomtasche.mapsracer.map.MapPath;
import at.tomtasche.mapsracer.osm.OsmMap;
import at.tomtasche.mapsracer.osm.OsmParser;

public class NodeFetcher {

	private static final String API_BASE_URL = "http://www.overpass-api.de";

	private File cacheDirectory;

	public NodeFetcher(File cacheDirectory) throws IOException {
		this.cacheDirectory = cacheDirectory;
		if (!cacheDirectory.exists()) {
			throw new IOException("cache does not exist: "
					+ cacheDirectory.getAbsolutePath());
		}
	}

	/**
	 * http://wiki.openstreetmap.org/wiki/API_v0.6#
	 * Retrieving_map_data_by_bounding_box:_GET_.2Fapi.2F0.6.2Fmap
	 * 
	 * http://wiki.openstreetmap.org/wiki/XAPI#Overpass_API
	 * 
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 * 
	 * @return
	 * @throws IOException
	 */
	private URL buildUrl(BoundingBox boundingBox) throws IOException {
		String url = API_BASE_URL;
		url += "/api/xapi_meta?*[bbox=";
		url += boundingBox.getLeft() + ",";
		url += boundingBox.getBottom() + ",";
		url += boundingBox.getRight() + ",";
		url += boundingBox.getTop();
		url += "]";

		return new URL(url);
	}

	private File fetchBoundingBox(BoundingBox boundingBox) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) buildUrl(boundingBox)
				.openConnection();

		try {
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

	private OsmMap parseBoundingBox(File cacheFile) {
		// http://www.openstreetmap.org/export
		final OsmParser parser = new OsmParser(cacheFile);
		parser.initialize();

		return MapConverter.convert(parser);
	}

	protected List<MapPath> getBoundingBox(BoundingBox boundingBox)
			throws IOException {
		File cacheFile = fetchBoundingBox(boundingBox);
		OsmMap map = parseBoundingBox(cacheFile);

		cacheFile.delete();

		return map.getStreets();
	}
}
