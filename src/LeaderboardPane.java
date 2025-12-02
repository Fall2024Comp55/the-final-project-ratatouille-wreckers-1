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
        double w = mainScreen.getWidth();
        double h = mainScreen.getHeight();

        GRect bg = new GRect(0, 0, w, h);
        bg.setFilled(true);
        bg.setFillColor(new Color(30, 30, 40));
        bg.setColor(Color.BLACK);
        contents.add(bg);
        mainScreen.add(bg);

        GLabel title = new GLabel("LEADERBOARD");
        title.setFont(new Font("Monospaced", Font.BOLD, 32));
        title.setColor(Color.WHITE);
        title.setLocation((w - title.getWidth()) / 2.0, 80);
        contents.add(title);
        mainScreen.add(title);

        backLabel = new GLabel("< BACK");
        backLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        backLabel.setColor(Color.LIGHT_GRAY);
        backLabel.setLocation(30, 40);
        contents.add(backLabel);
        mainScreen.add(backLabel);

        GLabel rankHeader = new GLabel("#");
        rankHeader.setFont(new Font("Monospaced", Font.BOLD, 18));
        rankHeader.setColor(Color.YELLOW);
        rankHeader.setLocation(180, 130);
        contents.add(rankHeader);
        mainScreen.add(rankHeader);

        GLabel nameHeader = new GLabel("NAME");
        nameHeader.setFont(new Font("Monospaced", Font.BOLD, 18));
        nameHeader.setColor(Color.YELLOW);
        nameHeader.setLocation(230, 130);
        contents.add(nameHeader);
        mainScreen.add(nameHeader);

        GLabel scoreHeader = new GLabel("SCORE");
        scoreHeader.setFont(new Font("Monospaced", Font.BOLD, 18));
        scoreHeader.setColor(Color.YELLOW);
        scoreHeader.setLocation(480, 130);
        contents.add(scoreHeader);
        mainScreen.add(scoreHeader);

        List<MainApplication.ScoreEntry> entries = mainScreen.getLeaderboardEntries();
        int row = 0;
        int rowGap = 32;

        for (MainApplication.ScoreEntry e : entries) {
            int y = 170 + row * rowGap;

            GLabel rank = new GLabel(String.valueOf(row + 1));
            rank.setFont(new Font("Monospaced", Font.PLAIN, 18));
            rank.setColor(Color.WHITE);
            rank.setLocation(180, y);
            contents.add(rank);
            mainScreen.add(rank);

            GLabel name = new GLabel(e.name);
            name.setFont(new Font("Monospaced", Font.PLAIN, 18));
            name.setColor(Color.WHITE);
            name.setLocation(230, y);
            contents.add(name);
            mainScreen.add(name);

            GLabel score = new GLabel(String.valueOf(e.score));
            score.setFont(new Font("Monospaced", Font.PLAIN, 18));
            score.setColor(Color.WHITE);
            score.setLocation(480, y);
            contents.add(score);
            mainScreen.add(score);

            row++;
        }
    }

    @Override
    public void hideContent() {
        for (GObject g : contents) {
            mainScreen.remove(g);
        }
        contents.clear();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        GObject obj = mainScreen.getElementAt(e.getX(), e.getY());
        if (obj == backLabel) {
            mainScreen.switchToWelcomeScreen();
        }
    }
}
