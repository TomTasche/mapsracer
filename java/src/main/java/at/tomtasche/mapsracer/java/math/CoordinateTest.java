package at.tomtasche.mapsracer.java.math;

import java.util.Collection;
import java.util.LinkedList;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import at.tomtasche.mapsracer.java.map.BoundingBox;

public class CoordinateTest {

	public static void main(String[] args) {
		boolean result = testContains();
		if (!result) {
			throw new AssertionError();
		}

		result = testPositionAt();
		if (!result) {
			throw new AssertionError();
		}

		System.out.println("success!");
	}

	private static boolean testContains() {
		GeoPosition wrongPosition = new GeoPosition(48.14284166793809,
				16.3303111559133);

		GeoPosition correctPosition = new GeoPosition(48.13350235738502,
				16.323430989332824);

		return testContains(correctPosition) && !testContains(wrongPosition);
	}

	private static boolean testContains(GeoPosition position) {
		Collection<BoundingBox> boxes = new LinkedList<>();

		BoundingBox box1 = new BoundingBox(16.30470149143808,
				48.15178488990584, 16.32096290588379, 48.141219969331864);
		BoundingBox box2 = new BoundingBox(16.320962905883743,
				48.162634994789286, 16.337227821350098, 48.151786036739296);
		BoundingBox box3 = new BoundingBox(16.32096290588379,
				48.151786036739296, 16.337227821350098, 48.141219969331864);
		BoundingBox box4 = new BoundingBox(16.337227821350098,
				48.151786036739296, 16.353058545246387, 48.141218882398476);

		BoundingBox correctBox = new BoundingBox(16.32096290588379,
				48.141219969331864, 16.33722782135006, 48.13065617609175);

		// do not contain position
		boxes.add(box1);
		boxes.add(box2);
		boxes.add(box3);
		boxes.add(box4);

		// contains position!
		boxes.add(correctBox);

		for (BoundingBox box : boxes) {
			boolean fail = false;
			boolean contains = CoordinateUtil.contains(box,
					position.getLatitude(), position.getLongitude());
			if (box == correctBox) {
				fail = !contains;
			} else {
				fail = contains;
			}

			if (fail) {
				return false;
			}
		}

		return true;
	}

	private static boolean testPositionAt() {
		GeoPosition position = new GeoPosition(53.32055555555556,
				-1.7297222222222224);

		GeoPosition result = CoordinateUtil.positionAt(position, 0, 124800);

		GeoPosition correctResult = new GeoPosition(54.44290891974212,
				-1.729722222222266);

		return result.equals(correctResult);
	}
}
