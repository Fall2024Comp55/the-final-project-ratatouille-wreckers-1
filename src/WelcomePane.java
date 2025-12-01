import acm.graphics.GImage;
import acm.graphics.GObject;

import java.awt.event.MouseEvent;

public class WelcomePane extends GraphicsPane {

    public WelcomePane(MainApplication mainScreen) {
        this.mainScreen = mainScreen;
    }

    @Override
    public void showContent() {
        addPicture();
        addStartButton();
        addLeaderboardsButton();
        addSettingsButton();
        addExitButton();
    }

    @Override
    public void hideContent() {
        for (GObject item : contents) {
            mainScreen.remove(item);
        }
        contents.clear();
    }

    private void addPicture() {
        // replace "start.png" with your own title PNG later
        GImage startImage = new GImage("start.png", 200, 100);
        startImage.scale(0.5, 0.5);
        startImage.setLocation(
                (mainScreen.getWidth() - startImage.getWidth()) / 2,
                60
        );
        contents.add(startImage);
        mainScreen.add(startImage);
    }

    private void addStartButton() {
        GImage startButton = new GImage("StartButton.png");
        startButton.scale(0.15, 0.15);
        startButton.setLocation(
                (mainScreen.getWidth() - startButton.getWidth()) / 2,
                320
        );
        contents.add(startButton);
        mainScreen.add(startButton);
    }

    private void addLeaderboardsButton() {
        GImage btn = new GImage("LeaderboardsButton.png");
        btn.scale(0.1, 0.1);
        btn.setLocation(
                (mainScreen.getWidth() - btn.getWidth()) / 2,
                370
        );
        contents.add(btn);
        mainScreen.add(btn);
    }

    private void addSettingsButton() {
        GImage btn = new GImage("SettingsButton.png");
        btn.scale(0.1, 0.1);
        btn.setLocation(
                (mainScreen.getWidth() - btn.getWidth()) / 2,
                420
        );
        contents.add(btn);
        mainScreen.add(btn);
    }

    private void addExitButton() {
        GImage btn = new GImage("ExitButton.png");
        btn.scale(0.1, 0.1);
        btn.setLocation(
                (mainScreen.getWidth() - btn.getWidth()) / 2,
                470
        );
        contents.add(btn);
        mainScreen.add(btn);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        GObject clicked = mainScreen.getElementAtLocation(e.getX(), e.getY());
        if (clicked == null) return;

        if (clicked == contents.get(1)) {          // Start
            mainScreen.switchToGameScreen();
        } else if (clicked == contents.get(2)) {   // Leaderboards
            mainScreen.switchToLeaderboardScreen();
        } else if (clicked == contents.get(3)) {   // Settings
            mainScreen.switchToSettingsScreen();
        } else if (clicked == contents.get(4)) {   // Exit
            System.exit(0);
        }
    }
}
