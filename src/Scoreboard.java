import acm.graphics.GCompound;
import acm.graphics.GLabel;
import acm.graphics.GRect;

import java.awt.Color;
import java.awt.Font;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Scoreboard {
    public enum Anchor { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }

    private final MainApplication mainScreen;
    private final GCompound panel = new GCompound();
    private final GLabel scoreLabel;
    private final GRect bg;

    private int displayedScore = 0;
    private boolean visible = false;

    // DEFAULT: top-right now
    private Anchor anchor = Anchor.TOP_RIGHT;
    private int padding = 10;
    private int hPad = 10, vPad = 6;

    public Scoreboard(MainApplication mainScreen) {
        this.mainScreen = mainScreen;

        scoreLabel = new GLabel("Score: 0");
        scoreLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        scoreLabel.setColor(Color.WHITE);

        bg = new GRect(0, 0, 1, 1);
        bg.setFilled(true);
        bg.setFillColor(new Color(0, 0, 0));
        bg.setColor(new Color(0, 0, 0));

        panel.add(bg, 0, 0);
        panel.add(scoreLabel, hPad, vPad + scoreLabel.getAscent());

        layoutPanel();
        placePanel();
    }

    public void update(int score) {
        displayedScore = score;
        scoreLabel.setLabel("Score: " + displayedScore);
        layoutPanel();
        placePanel();
    }

    public void show() {
        if (!visible) {
            placePanel();
            mainScreen.add(panel);
            visible = true;
        }
    }

    public void showForSeconds(int seconds) {
        show();
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

    public void setAnchor(Anchor anchor) {
        this.anchor = anchor;
        placePanel();
    }

    public void setPadding(int padding) {
        this.padding = Math.max(0, padding);
        placePanel();
    }

    public int getDisplayedScore() {
        return displayedScore;
    }

    private void layoutPanel() {
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
