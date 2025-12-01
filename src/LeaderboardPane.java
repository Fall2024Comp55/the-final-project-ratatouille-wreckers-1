import acm.graphics.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.util.List;

public class LeaderboardPane extends GraphicsPane {

    private GLabel backLabel;

    public LeaderboardPane(MainApplication mainScreen) {
        this.mainScreen = mainScreen;
    }

    @Override
    public void showContent() {
        addBackground();
        addTitle();
        addScores();
        addBackButton();
    }

    @Override
    public void hideContent() {
        for (GObject obj : contents) {
            mainScreen.remove(obj);
        }
        contents.clear();
    }

    private void addBackground() {
        double w = mainScreen.getWidth();
        double h = mainScreen.getHeight();

        GRect bg = new GRect(0, 0, w, h);
        bg.setFilled(true);
        bg.setFillColor(new Color(225, 232, 255));
        bg.setColor(new Color(180, 190, 220));
        contents.add(bg);
        mainScreen.add(bg);

        GRect card = new GRect(w * 0.20, h * 0.12, w * 0.60, h * 0.70);
        card.setFilled(true);
        card.setFillColor(Color.WHITE);
        card.setColor(new Color(200, 200, 200));
        contents.add(card);
        mainScreen.add(card);
    }

    private void addTitle() {
        double w = mainScreen.getWidth();

        GLabel title = new GLabel("LEADERBOARD");
        title.setFont(new Font("Serif", Font.BOLD, 30));
        title.setColor(new Color(40, 40, 40));
        title.setLocation(
                (w - title.getWidth()) / 2.0,
                130
        );

        contents.add(title);
        mainScreen.add(title);
    }

    private void addScores() {
        List<Integer> scores = mainScreen.getLeaderboardScores();

        double w = mainScreen.getWidth();
        double startY = 180;
        double lineGap = 35;

        if (scores.isEmpty()) {
            GLabel empty = new GLabel("No scores yet. Wreck some rats first!");
            empty.setFont(new Font("SansSerif", Font.PLAIN, 18));
            empty.setColor(new Color(80, 80, 80));
            empty.setLocation(
                    (w - empty.getWidth()) / 2.0,
                    startY + 40
            );
            contents.add(empty);
            mainScreen.add(empty);
            return;
        }

        int rank = 1;
        for (int score : scores) {
            String text = String.format("%d.  %d pts", rank, score);
            GLabel line = new GLabel(text);
            line.setFont(new Font("Monospaced", Font.BOLD, 20));
            line.setColor(new Color(50, 50, 80));
            line.setLocation(
                    (w - line.getWidth()) / 2.0,
                    startY + rank * lineGap
            );
            contents.add(line);
            mainScreen.add(line);
            rank++;
            if (rank > 10) break;
        }
    }

    private void addBackButton() {
        backLabel = new GLabel("< BACK");
        backLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        backLabel.setColor(new Color(40, 40, 40));
        backLabel.setLocation(30, 40);
        contents.add(backLabel);
        mainScreen.add(backLabel);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        GObject clicked = mainScreen.getElementAtLocation(e.getX(), e.getY());
        if (clicked == null) return;

        if (clicked == backLabel) {
            mainScreen.switchToWelcomeScreen();
        }
    }
}
