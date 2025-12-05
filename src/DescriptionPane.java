import acm.graphics.*;
import java.awt.Color;
import java.awt.event.MouseEvent;

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
        GLabel text = new GLabel("This is an extra info screen!", 100, 70);
        text.setColor(Color.BLUE);
        text.setFont("DialogInput-PLAIN-24");
        text.setLocation((mainScreen.getWidth() - text.getWidth()) / 2, 120);

        contents.add(text);
        mainScreen.add(text);
    }

    private void addBackButton() {
        GLabel back = new GLabel("< BACK");
        back.setFont("DialogInput-PLAIN-20");
        back.setColor(Color.BLACK);
        back.setLocation(40, mainScreen.getHeight() - 40);

        contents.add(back);
        mainScreen.add(back);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        mainScreen.switchToWelcomeScreen();
    }
}
