package at.tomtasche.mapsracer.engine;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

@SuppressWarnings("serial")
public class PositionServlet extends HttpServlet {

	private static final String MEMCACHE_KEY_ALL_IDS = "all_ids";

	private MemcacheService memcache;

	public PositionServlet() {
		memcache = MemcacheServiceFactory.getMemcacheService();
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		Set<String> allIds = (Set<String>) memcache.get(MEMCACHE_KEY_ALL_IDS);
		if (allIds == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);

			return;
		}

		// TODO: use json
		for (String id : allIds) {
			Position position = (Position) memcache.get(id);
			response.getWriter().write(
					id + ";" + position.lat + ";" + position.lon + ";"
							+ position.from + ";" + position.to);
			response.getWriter().println();
		}
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		Position position = positionFromRequest(request);
		String id = idFromRequest(request);

		memcache.put(id, position);

		// TODO: this is not how memcache is supposed to be used
		// TODO: this set is growing infinitely until we run out of space!
		Set<String> allIds = (Set<String>) memcache.get(MEMCACHE_KEY_ALL_IDS);
		if (allIds == null) {
			allIds = new HashSet<String>();
		}
		allIds.add(id);

		memcache.put(MEMCACHE_KEY_ALL_IDS, allIds);
	}

	private Position positionFromRequest(HttpServletRequest request) {
		String from = request.getParameter("from");
		String to = request.getParameter("to");

		String lat = request.getParameter("lat");
		String lon = request.getParameter("lon");

		Position position = new Position();
		position.lat = Double.parseDouble(lat);
		position.lon = Double.parseDouble(lon);
		position.from = Long.parseLong(from);
		position.to = Long.parseLong(to);

		return position;
	}

	private String idFromRequest(HttpServletRequest request) {
		return request.getParameter("id");
	}
}
