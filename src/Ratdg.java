import acm.graphics.*;

import java.awt.Color;

public abstract class Ratdg {
    protected RatType type;
    protected int points;
    protected int lifetimeMs;
    protected boolean visible;
    protected Hole hole;

    protected final GCompound node = new GCompound();
    protected final MainApplication app;

    private int ageMs = 0;

    protected Ratdg(MainApplication app, RatType type, int points, int lifetimeMs) {
        this.app = app;
        this.type = type;
        this.points = points;
        this.lifetimeMs = lifetimeMs;
        this.visible = false;
        this.hole = null;
    }

    public void onSpawn(Hole h) {
        this.hole = h;
        node.setLocation(h.getX(), h.getY());
        show();
        ageMs = 0;
    }

    public void onTick(int deltaMs) {
        ageMs += deltaMs;
        if (ageMs >= lifetimeMs) {
            despawn();
        }
    }

    public void despawn() {
        hide();
        if (hole != null) {
            hole.clearRat(this);
            hole = null;
        }
        ageMs = 0;
    }

    public void show() {
        if (!visible) {
            app.add(node);
            visible = true;
        }
    }

    public void hide() {
        if (visible) {
            app.remove(node);
            visible = false;
        }
    }

    public void onMouseMove(int x, int y) { }

    public void onMouseClick(int x, int y) { }

    protected void setSpriteFromFile(String path, double offsetX, double offsetY, double width, double height) {
        try {
            GImage img = new GImage(path);
            if (width > 0 && height > 0) {
                img.setSize(width, height);
            }
            node.add(img, offsetX, offsetY);
        } catch (Exception ex) {
            GOval fallback = new GOval(0, 0, (width > 0 ? width : 40), (height > 0 ? height : 40));
            fallback.setFilled(true);
            fallback.setFillColor(new Color(160, 160, 160));
            fallback.setColor(Color.BLACK);
            node.add(fallback, offsetX, offsetY);
        }
    }

    // helpers
    public boolean containsPoint(double x, double y) {
        return node.contains(x, y);
    }

    public boolean isActive() {
        return visible || hole != null;
    }

    public boolean isVisible() {
        return visible;
    }

    public RatType getType() {
        return type;
    }

    public int getPoints() {
        return points;
    }
}
