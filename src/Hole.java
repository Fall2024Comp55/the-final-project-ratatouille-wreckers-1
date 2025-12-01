public class Hole {
    private final double x;
    private final double y;
    private Ratdg currentRat;  // null when empty

    public Hole(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() { return x; }
    public double getY() { return y; }

    public boolean isOccupied() { return currentRat != null; }

    public void spawn(Ratdg rat) {
        if (isOccupied()) return;
        currentRat = rat;
        rat.onSpawn(this);
    }

    public void clearRat(Ratdg rat) {
        if (currentRat == rat) {
            currentRat = null;
        }
    }
}
