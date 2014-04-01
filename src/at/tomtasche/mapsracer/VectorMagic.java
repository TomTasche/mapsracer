package at.tomtasche.mapsracer;

public class VectorMagic {
    
    public static double angle(Vector2d a, Vector2d b) {
        return MathUtil.atan2(a) - MathUtil.atan2(b);
    }
    
    public static int crossing(Vector2d[] crossing, int originIndex,
            double direction) {
        Vector2d origin = crossing[originIndex].negate();
        int index = -1;
        double minDistance = Double.MAX_VALUE;
        
        for (int i = 0; i < crossing.length; i++) {
            double angle = angle(origin, crossing[i]);
            double distance = Math.abs(direction - angle);
            if (minDistance > distance) {
                index = i;
                minDistance = distance;
            }
        }
        
        return index;
    }
    
    public static void main(String[] args) {
        Vector2d a = new Vector2d(0, 1);
        Vector2d b = new Vector2d(1, 0);
        
        System.out.println(angle(a, b));
        
        System.out.println();
        
        Vector2d[] crossing = { new Vector2d(1, 0), new Vector2d(0, 1),
                new Vector2d(0, -1), new Vector2d(-1, 0) };
        int originIndex = 2;
        double direction = -90 * MathUtil.DEG2RAD;
        System.out.println(crossing[originIndex]);
        System.out.println(direction * MathUtil.RAD2DEG);
        int destinationIndex = crossing(crossing, originIndex, direction);
        System.out.println(crossing[destinationIndex]);
    }
    
}