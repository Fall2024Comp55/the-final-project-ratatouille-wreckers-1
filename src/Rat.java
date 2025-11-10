import acm.graphics.*;

public abstract class Rat {
    protected final MainApplication app;
    protected final RatType type;
    protected final int points;
    protected final int lifetimeMs;

    protected int ageMs = 0;
    protected boolean visible = false;

    // where we draw the rat
    protected final GCompound node = new GCompound();
    //if a Hole class, keep a ref
    
    protected Hole hole = null;

    protected Rat(MainApplication app, RatType type, int points, int lifetimeMs) {
        this.app = app;
        this.type = type;
        this.points = points;
        this.lifetimeMs = lifetimeMs;
    }

    /** Put this rat at a location (or from a Hole) and show it. */
    public void onSpawn(Hole h) {
        this.hole = h;
        double x = h.getX();
        double y = h.getY();
        node.setLocation(x, y);
        show();
    }

    public void onSpawnAt(double x, double y) {
        this.hole = null;
        node.setLocation(x, y);
        show();
    }

    public void onTick(int deltaMs) {
        ageMs += deltaMs;
        if (ageMs >= lifetimeMs) {
            despawn();
        }
    }

    public void onMouseClick(int x, int y) {
        if (!visible) return;
        if (containsPoint(x, y)) {
            onHit();
        }
    }

    protected abstract void onHit();

    public void despawn() {
        hide();
        if (hole != null) {
            hole.clearRat(this); 
            hole = null;
        }
        ageMs = 0;
    }

    public void show() {
        if (visible) return;
        app.add(node);
        visible = true;
    }

    public void hide() {
        if (!visible) return;
        app.remove(node);
        visible = false;
    }

    public boolean containsPoint(double px, double py) {
        GObject at = app.getElementAtLocation(px, py);
        return at != null && (at == node || node.contains(at));
    }

    public GCompound getNode() { return node; }
    public int getPoints() { return points; }
    public RatType getType() { return type; }
}
