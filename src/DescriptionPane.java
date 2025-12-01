import java.awt.Color;
import java.awt.event.MouseEvent;

import acm.graphics.GImage;
import acm.graphics.GLabel;
import acm.graphics.GObject;

public class DescriptionPane extends GraphicsPane {

    public DescriptionPane(MainApplication mainScreen) {
        this.mainScreen = mainScreen;
    }

    @Override
    public void showContent() {
        addText();
        addBackButton();
    }

    @Override
    public void hideContent() {
        for (GObject item : contents) {
            mainScreen.remove(item);
        }
        contents.clear();
    }

    private void addText() {
        GLabel text = new GLabel("This is an example description screen.", 100, 70);
        text.setColor(Color.BLUE);
        text.setFont("DialogInput-PLAIN-24");
        text.setLocation((mainScreen.getWidth() - text.getWidth()) / 2, 70);

        contents.add(text);
        mainScreen.add(text);
    }

    private void addBackButton() {
        GImage backButton = new GImage("back.jpg", 200, 400);
        backButton.scale(0.3, 0.3);
        backButton.setLocation((mainScreen.getWidth() - backButton.getWidth()) / 2, 400);

        contents.add(backButton);
        mainScreen.add(backButton);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        GObject clicked = mainScreen.getElementAtLocation(e.getX(), e.getY());
        if (clicked != null && contents.contains(clicked)) {
            mainScreen.switchToWelcomeScreen();
        }
    }
}
