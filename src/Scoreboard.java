import acm.graphics.*;
import java.awt.*;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/** Compact scoreboard overlay for the main game screen. */
public class Scoreboard {
    public enum Anchor { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }

    // --- Fields ---
    private final MainApplication mainScreen;
    private final GCompound panel = new GCompound();
    private final GLabel scoreLabel;
    private final GRect bg;

    private int displayedScore = 0;
    private boolean visible = false;

    // layout
    private Anchor anchor = Anchor.TOP_LEFT;
    private int padding = 10;       // distance from edges
    private int hPad = 10, vPad = 6; // inner padding around text

    // --- Constructor ---
    public Scoreboard(MainApplication mainScreen) {
        this.mainScreen = mainScreen;

        // Label
        scoreLabel = new GLabel("Score: 0");
        scoreLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        scoreLabel.setColor(Color.WHITE);

        // Background (opaque for readability)
        bg = new GRect(0, 0, 1, 1);
        bg.setFilled(true);
        bg.setFillColor(new Color(0, 0, 0)); // use alpha if your setup supports it: new Color(0,0,0,170)
        bg.setColor(new Color(0, 0, 0));

        // Build compact panel
        panel.add(bg, 0, 0);
        panel.add(scoreLabel, hPad, vPad + scoreLabel.getAscent());

        // size background to fit text
        layoutPanel();
        placePanel();
    }

    // --- Public API ---
    public void update(int score) {
        displayedScore = score;
        scoreLabel.setLabel("Score: " + displayedScore);
        layoutPanel();
        placePanel();
    }

    /** Show and keep it visible until hide() is called. */
    public void show() {
        if (!visible) {
            placePanel();
            mainScreen.add(panel);
            visible = true;
        }
    }

    /** Show it for N seconds, then auto-hide. */
    public void showForSeconds(int seconds) {
        show();
        // auto-hide with Swing timer
        new Timer(seconds * 1000, new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                hide();
                ((Timer) e.getSource()).stop();
            }
        }).start();
    }

    public void hide() {
        if (visible) {
            mainScreen.remove(panel);
            visible = false;
        }
    }

    /** Pin to a different corner (call before show, or anytime). */
    public void setAnchor(Anchor anchor) {
        this.anchor = anchor;
        placePanel();
    }

    /** Change edge padding (distance from window edges). */
    public void setPadding(int padding) {
        this.padding = Math.max(0, padding);
        placePanel();
    }

    public int getDisplayedScore() {
        return displayedScore;
    }

    // --- Layout helpers ---
    private void layoutPanel() {
        // Resize background to snugly wrap the label
        double w = scoreLabel.getWidth() + hPad * 2;
        double h = scoreLabel.getAscent() + scoreLabel.getDescent() + vPad * 2;
        bg.setSize(w, h);
    }

    private void placePanel() {
        if (mainScreen == null) return;
        double canvasW = mainScreen.getWidth();
        double canvasH = mainScreen.getHeight();
        double pw = bg.getWidth();
        double ph = bg.getHeight();

        double x = padding, y = padding;
        switch (anchor) {
            case TOP_LEFT:
                x = padding; y = padding; break;
            case TOP_RIGHT:
                x = canvasW - pw - padding; y = padding; break;
            case BOTTOM_LEFT:
                x = padding; y = canvasH - ph - padding; break;
            case BOTTOM_RIGHT:
                x = canvasW - pw - padding; y = canvasH - ph - padding; break;
        }
        panel.setLocation(x, y);
    }
}