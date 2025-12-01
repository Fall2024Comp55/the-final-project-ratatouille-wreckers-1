import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;

import acm.graphics.GImage;
import acm.graphics.GLabel;
import acm.graphics.GObject;
import acm.graphics.GPolygon;
import acm.graphics.GRect;

public class WelcomePane extends GraphicsPane {

    private GImage startButtonImg;
    private GImage leaderboardsButtonImg;
    private GImage settingsButtonImg;
    private GImage exitButtonImg;

    public WelcomePane(MainApplication mainScreen) {
        this.mainScreen = mainScreen;
    }

    @Override
    public void showContent() {
        addBackground();
        addEiffelTowerSilhouette();
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

    // ---------------- BACKGROUND (French vibe) ----------------
    private void addBackground() {
        double w = mainScreen.getWidth();
        double h = mainScreen.getHeight();

        Color frenchBlue = new Color(58, 91, 170);
        Color frenchWhite = new Color(245, 245, 245);
        Color frenchRed = new Color(203, 58, 74);

        GRect sky = new GRect(0, 0, w, h);
        sky.setFilled(true);
        sky.setFillColor(new Color(220, 230, 255));
        sky.setColor(Color.WHITE);
        contents.add(sky);
        mainScreen.add(sky);

        GRect leftStrip = new GRect(0, 0, w * 0.20, h);
        leftStrip.setFilled(true);
        leftStrip.setFillColor(frenchBlue);
        leftStrip.setColor(frenchBlue);
        contents.add(leftStrip);
        mainScreen.add(leftStrip);

        GRect rightStrip = new GRect(w * 0.80, 0, w * 0.20, h);
        rightStrip.setFilled(true);
        rightStrip.setFillColor(frenchRed);
        rightStrip.setColor(frenchRed);
        contents.add(rightStrip);
        mainScreen.add(rightStrip);

        GRect centerPanel = new GRect(w * 0.20, h * 0.12, w * 0.60, h * 0.60);
        centerPanel.setFilled(true);
        centerPanel.setFillColor(frenchWhite);
        centerPanel.setColor(new Color(220, 220, 220));
        contents.add(centerPanel);
        mainScreen.add(centerPanel);
    }

    // ---------------- EIFFEL TOWER ----------------
    private void addEiffelTowerSilhouette() {
        double w = mainScreen.getWidth();
        double cx = w / 2.0;
        double baseY = 330;

        GPolygon tower = new GPolygon();
        tower.addVertex(cx, baseY - 180);
        tower.addVertex(cx - 30, baseY - 100);
        tower.addVertex(cx - 60, baseY);
        tower.addVertex(cx - 40, baseY);
        tower.addVertex(cx - 15, baseY - 60);
        tower.addVertex(cx + 15, baseY - 60);
        tower.addVertex(cx + 40, baseY);
        tower.addVertex(cx + 60, baseY);
        tower.addVertex(cx + 30, baseY - 100);
        tower.addVertex(cx, baseY - 180);

        tower.setFilled(true);
        tower.setFillColor(new Color(55, 55, 55));
        tower.setColor(new Color(30, 30, 30));

        contents.add(tower);
        mainScreen.add(tower);
    }

    // ---------------- TITLE ----------------
    private void addTitle() {
        double w = mainScreen.getWidth();

        GLabel title = new GLabel("WRECK IT RATS");
        title.setFont(new Font("Serif", Font.BOLD, 32));
        title.setColor(new Color(40, 40, 40));

        title.setLocation(
                (w - title.getWidth()) / 2.0,
                160
        );

        contents.add(title);
        mainScreen.add(title);
    }

    // ---------------- BUTTONS ----------------
    private void addButtons() {
        double w = mainScreen.getWidth();
        double startY = 370;
        double gap = 55;

        startButtonImg = new GImage("StartButton.png");
        startButtonImg.scale(0.15);
        startButtonImg.setLocation((w - startButtonImg.getWidth()) / 2.0, startY);
        contents.add(startButtonImg);
        mainScreen.add(startButtonImg);

        leaderboardsButtonImg = new GImage("LeaderboardsButton.png");
        leaderboardsButtonImg.scale(0.10);
        leaderboardsButtonImg.setLocation(
                (w - leaderboardsButtonImg.getWidth()) / 2.0,
                startY + gap
        );
        contents.add(leaderboardsButtonImg);
        mainScreen.add(leaderboardsButtonImg);

        settingsButtonImg = new GImage("SettingsButton.png");
        settingsButtonImg.scale(0.10);
        settingsButtonImg.setLocation(
                (w - settingsButtonImg.getWidth()) / 2.0,
                startY + 2 * gap
        );
        contents.add(settingsButtonImg);
        mainScreen.add(settingsButtonImg);

        exitButtonImg = new GImage("ExitButton.png");
        exitButtonImg.scale(0.10);
        exitButtonImg.setLocation(
                (w - exitButtonImg.getWidth()) / 2.0,
                startY + 3 * gap
        );
        contents.add(exitButtonImg);
        mainScreen.add(exitButtonImg);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        GObject clicked = mainScreen.getElementAtLocation(e.getX(), e.getY());
        if (clicked == null) return;

        if (clicked == startButtonImg) {
            mainScreen.switchToGameScreen();
        } else if (clicked == leaderboardsButtonImg) {
            System.out.println("Leaderboards Page");
        } else if (clicked == settingsButtonImg) {
            mainScreen.switchToSettingsScreen();
        } else if (clicked == exitButtonImg) {
            System.exit(0);
        }
    }
}
