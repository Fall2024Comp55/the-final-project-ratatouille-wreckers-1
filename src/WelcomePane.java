import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;

import acm.graphics.GImage;
import acm.graphics.GLabel;
import acm.graphics.GObject;

public class WelcomePane extends GraphicsPane {

    private GImage startButtonImg;
    private GImage leaderboardsButtonImg;
    private GImage settingsButtonImg;
    private GImage exitButtonImg;
    private GImage infoButtonImg;        // extra button at the bottom

    private GImage backgroundImg;

    public WelcomePane(MainApplication mainScreen) {
        this.mainScreen = mainScreen;
    }

    @Override
    public void showContent() {
        addBackgroundImage();
        addTitle();
        addButtons();
    }

    @Override
    public void hideContent() {
        for (GObject item : contents) {
            mainScreen.remove(item);
        }
        contents.clear();
    }

    // ---------- Background Image ----------
    private void addBackgroundImage() {
        double w = mainScreen.getWidth();
        double h = mainScreen.getHeight();

        backgroundImg = new GImage("eiffel_tower_background.png");
        backgroundImg.setSize(w, h);     // Resize to window
        backgroundImg.setLocation(0, 0);

        contents.add(backgroundImg);
        mainScreen.add(backgroundImg);
    }

    // ---------- Title ----------
    private void addTitle() {
        double w = mainScreen.getWidth();

        GLabel title = new GLabel("WRECK IT RATS");
        title.setFont(new Font("Serif", Font.BOLD, 32));
        title.setColor(new Color(40, 40, 40));
        // Slightly higher so stack of buttons fits nicer
        title.setLocation((w - title.getWidth()) / 2.0, 150);

        contents.add(title);
        mainScreen.add(title);
    }

    // ---------- Buttons ----------
    private void addButtons() {
        double w = mainScreen.getWidth();
        double h = mainScreen.getHeight();

        // Start buttons a bit higher so we can fit INFO at the bottom
        double startY = h * 0.50;   // around middle of the screen (was 370)
        double gap    = 52;         // vertical gap between buttons

        // START
        startButtonImg = new GImage("StartButton.png");
        startButtonImg.scale(0.15);
        startButtonImg.setLocation(
                (w - startButtonImg.getWidth()) / 2.0,
                startY
        );
        contents.add(startButtonImg);
        mainScreen.add(startButtonImg);

        // LEADERBOARDS
        leaderboardsButtonImg = new GImage("LeaderboardsButton.png");
        leaderboardsButtonImg.scale(0.10);
        leaderboardsButtonImg.setLocation(
                (w - leaderboardsButtonImg.getWidth()) / 2.0,
                startY + gap
        );
        contents.add(leaderboardsButtonImg);
        mainScreen.add(leaderboardsButtonImg);

        // SETTINGS
        settingsButtonImg = new GImage("SettingsButton.png");
        settingsButtonImg.scale(0.10);
        settingsButtonImg.setLocation(
                (w - settingsButtonImg.getWidth()) / 2.0,
                startY + 2 * gap
        );
        contents.add(settingsButtonImg);
        mainScreen.add(settingsButtonImg);

        // EXIT
        exitButtonImg = new GImage("ExitButton.png");
        exitButtonImg.scale(0.10);
        exitButtonImg.setLocation(
                (w - exitButtonImg.getWidth()) / 2.0,
                startY + 3 * gap
        );
        contents.add(exitButtonImg);
        mainScreen.add(exitButtonImg);

        // INFO (new) – using more.jpeg, tucked nicely under EXIT
        infoButtonImg = new GImage("more.jpeg");
        infoButtonImg.scale(0.22); // slightly smaller so it fits the stack
        infoButtonImg.setLocation(
                (w - infoButtonImg.getWidth()) / 2.0,
                startY + 4 * gap
        );
        contents.add(infoButtonImg);
        mainScreen.add(infoButtonImg);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        GObject clicked = mainScreen.getElementAtLocation(e.getX(), e.getY());
        if (clicked == null) return;

        if (clicked == startButtonImg) {
            mainScreen.playSound("Media/hammer_hit.wav");
            mainScreen.switchToGameScreen();
        } else if (clicked == leaderboardsButtonImg) {
            mainScreen.playSound("Media/hammer_hit.wav");
            mainScreen.switchToLeaderboardScreen();
        } else if (clicked == settingsButtonImg) {
            mainScreen.playSound("Media/hammer_hit.wav");
            mainScreen.switchToSettingsScreen();
        } else if (clicked == exitButtonImg) {
            mainScreen.playSound("Media/hammer_hit.wav");
            System.exit(0);
        } else if (clicked == infoButtonImg) {
            mainScreen.playSound("Media/hammer_hit.wav");
            mainScreen.switchToInfoScreen();
        }
    }
}
