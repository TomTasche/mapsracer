package at.tomtasche.mapsracer.math;

import org.jdesktop.swingx.mapviewer.GeoPosition;

public class CoordinateTest {

	public static void main(String[] args) {
		GeoPosition position = new GeoPosition(53.32055555555556,
				-1.7297222222222224);

		GeoPosition result = CoordinateUtil.positionAt(position, 0, 124.8);

		GeoPosition correctResult = new GeoPosition(54.44290891974192,
				-1.729722222222266);

		assert result.equals(correctResult);
		System.out.println("success!");
	}
}
