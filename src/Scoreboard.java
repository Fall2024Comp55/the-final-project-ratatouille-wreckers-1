import acm.graphics.*;
import java.awt.Font;

public class Scoreboard {
    // --- Fields ---
    private MainApplication mainScreen;
    private int displayedScore;
    private GLabel scoreLabel;
    private boolean visible = false;

    // --- Constructor ---
    public Scoreboard(MainApplication mainScreen) {
        this.mainScreen = mainScreen;
        this.displayedScore = 0;

        // Create a label to show the score
        scoreLabel = new GLabel("Score: " + displayedScore);
        scoreLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        scoreLabel.setColor(java.awt.Color.WHITE);
        scoreLabel.setLocation(10, 25); // top-left corner
    }

    // --- Methods ---
    public void update(int score) {
        displayedScore = score;
        scoreLabel.setLabel("Score: " + displayedScore);
    }

    public void show() {
        if (!visible) {
            mainScreen.add(scoreLabel);
            visible = true;
        }
    }

    public void hide() {
        if (visible) {
            mainScreen.remove(scoreLabel);
            visible = false;
        }
    }

    public int getDisplayedScore() {
        return displayedScore;
    }
}