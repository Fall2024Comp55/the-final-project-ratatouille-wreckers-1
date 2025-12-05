import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.util.List;

import acm.graphics.GImage;
import acm.graphics.GLabel;
import acm.graphics.GObject;
import acm.graphics.GRect;

public class InfoPane extends GraphicsPane {

    private GLabel backLabel;

    public InfoPane(MainApplication mainScreen) {
        this.mainScreen = mainScreen;
    }

    @Override
    public void showContent() {
        double w = mainScreen.getWidth();
        double h = mainScreen.getHeight();

        // Background
        GRect bg = new GRect(0, 0, w, h);
        bg.setFilled(true);
        bg.setFillColor(new Color(20, 24, 32));
        bg.setColor(Color.BLACK);
        contents.add(bg);
        mainScreen.add(bg);

        // Title
        GLabel title = new GLabel("RAT GUIDE");
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        title.setColor(Color.WHITE);
        title.setLocation((w - title.getWidth()) / 2.0, 70);
        contents.add(title);
        mainScreen.add(title);

        // Back
        backLabel = new GLabel("< BACK");
        backLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        backLabel.setColor(Color.WHITE);
        backLabel.setLocation(30, 40);
        contents.add(backLabel);
        mainScreen.add(backLabel);

        // --- Card layout ---
        double cardW = 260;
        double cardH = 300;
        double gap = 25;
        double totalW = 3 * cardW + 2 * gap;
        double startX = (w - totalW) / 2.0;
        double topY = 130;

        // Normal
        addRatCard(startX,
                   topY,
                   "Normal Rat",
                   "+50 points\nSafe to hit.",
                   "rat_normal.png");

        // Bonus
        addRatCard(startX + (cardW + gap),
                   topY,
                   "Bonus Rat",
                   "+100 points\nRare, super good.",
                   "rat_bonus.png");

        // Trap
        addRatCard(startX + 2 * (cardW + gap),
                   topY,
                   "Trap Rat",
                   "-25 points\nAvoid hitting this one.",
                   "rat_trap.png");
    }

    private void addRatCard(double x, double y,
                            String name, String desc, String imageFile) {

        double cardW = 260;
        double cardH = 300;

        // Card background
        GRect card = new GRect(x, y, cardW, cardH);
        card.setFilled(true);
        card.setFillColor(new Color(40, 48, 65));
        card.setColor(new Color(180, 180, 180));
        contents.add(card);
        mainScreen.add(card);

        double imgTopY = y + 18;
        double imgMaxH = 110;

        try {
            GImage img = new GImage(imageFile);
            double scale = imgMaxH / img.getHeight();
            if (scale < 1.0) {
                img.scale(scale);
            }
            img.setLocation(
                    x + (cardW - img.getWidth()) / 2.0,
                    imgTopY
            );

            contents.add(img);
            mainScreen.add(img);
        } catch (Exception ex) {
            GRect placeholder = new GRect(
                    x + (cardW - 80) / 2.0,
                    imgTopY + 10,
                    80,
                    80
            );
            placeholder.setFilled(true);
            placeholder.setFillColor(new Color(90, 90, 90));
            placeholder.setColor(Color.WHITE);
            contents.add(placeholder);
            mainScreen.add(placeholder);
        }

        // Name
        double nameY = y + 150;
        GLabel nameLbl = new GLabel(name);
        nameLbl.setFont(new Font("SansSerif", Font.BOLD, 18));
        nameLbl.setColor(Color.WHITE);
        nameLbl.setLocation(
                x + (cardW - nameLbl.getWidth()) / 2.0,
                nameY
        );
        contents.add(nameLbl);
        mainScreen.add(nameLbl);

        // Description lines
        String[] lines = desc.split("\n");
        double baseY = nameY + 28;
        double lineGap = 20;
        for (int i = 0; i < lines.length; i++) {
            GLabel line = new GLabel(lines[i]);
            line.setFont(new Font("SansSerif", Font.PLAIN, 13));
            line.setColor(Color.LIGHT_GRAY);
            line.setLocation(x + 18, baseY + i * lineGap);
            contents.add(line);
            mainScreen.add(line);
        }
    }

    @Override
    public void hideContent() {
        for (GObject obj : contents) {
            mainScreen.remove(obj);
        }
        contents.clear();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        GObject hit = mainScreen.getElementAt(e.getX(), e.getY());
        if (hit == backLabel) {
            mainScreen.switchToWelcomeScreen();
        }
    }
}
