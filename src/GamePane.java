import acm.graphics.*;

import javax.swing.Timer;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GamePane extends GraphicsPane {

    // Grid settings
    private static final int ROWS = 3;
    private static final int COLS = 4;
    private static final double GRID_WIDTH = 520;
    private static final double GRID_HEIGHT = 260;
    private static final double HOLE_RADIUS = 30;

    // Timing
    private static final int TICK_MS = 40;                 // ~25 FPS
    private static final double SPAWN_CHANCE = 0.05;
    private static final int MAX_ACTIVE_RATS = 5;

    // Game duration: 3 minutes
    private static final int GAME_DURATION_MS = 3 * 60 * 1000;
    private int timeRemainingMs = GAME_DURATION_MS;

    private final List<Hole> holes = new ArrayList<>();
    private final List<Ratdg> activeRats = new ArrayList<>();
    private final Random rng = new Random();

    private Timer timer;
    private GObject hammer;
    private GRect topBar;
    private GLabel backLabel;
    private GLabel timerLabel;

    public GamePane(MainApplication mainScreen) {
        this.mainScreen = mainScreen;
        buildBoard();
        setupTimer();
    }

    // ---------- lifecycle ----------

    @Override
    public void showContent() {
        for (GObject obj : contents) {
            mainScreen.add(obj);
        }
        if (hammer != null) {
            hammer.sendToFront();
        }
        if (timer != null) {
            // reset game state
            timeRemainingMs = GAME_DURATION_MS;
            updateTimerLabel();
            mainScreen.setScore(0);
            timer.start();
        }
    }

    @Override
    public void hideContent() {
        if (timer != null) {
            timer.stop();
        }

        for (Ratdg r : activeRats) {
            r.despawn();
        }
        activeRats.clear();

        for (GObject obj : contents) {
            mainScreen.remove(obj);
        }
        contents.clear();

        buildBoard();
    }

    // ---------- setup visuals ----------

    private void buildBoard() {
        double w = mainScreen.getWidth();
        double h = mainScreen.getHeight();

        // sky
        GRect sky = new GRect(0, 0, w, h * 0.6);
        sky.setFilled(true);
        sky.setFillColor(new Color(155, 204, 255));
        sky.setColor(Color.BLACK);
        contents.add(sky);

        // ground
        GRect ground = new GRect(0, h * 0.6, w, h * 0.4);
        ground.setFilled(true);
        ground.setFillColor(new Color(90, 160, 80));
        ground.setColor(Color.BLACK);
        contents.add(ground);

        // top bar
        topBar = new GRect(0, 0, w, 60);
        topBar.setFilled(true);
        topBar.setFillColor(new Color(40, 40, 40));
        topBar.setColor(Color.BLACK);
        contents.add(topBar);

        // title
        GLabel title = new GLabel("WRECK IT RATS");
        title.setFont(new Font("Monospaced", Font.BOLD, 22));
        title.setColor(Color.WHITE);
        title.setLocation(
                (w - title.getWidth()) / 2.0,
                38
        );
        contents.add(title);

        // back button (top-left)
        backLabel = new GLabel("< BACK");
        backLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        backLabel.setColor(new Color(220, 220, 220));
        backLabel.setLocation(15, 36);
        contents.add(backLabel);

        // timer label (top-right)
        timerLabel = new GLabel("03:00");
        timerLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        timerLabel.setColor(Color.WHITE);
        timerLabel.setLocation(
                w - timerLabel.getWidth() - 20,
                36
        );
        contents.add(timerLabel);

        // wooden board for holes
        double boardW = GRID_WIDTH + 80;
        double boardH = GRID_HEIGHT + 80;
        double boardX = (w - boardW) / 2.0;
        double boardY = 120;

        GRect board = new GRect(boardX, boardY, boardW, boardH);
        board.setFilled(true);
        board.setFillColor(new Color(139, 115, 85));
        board.setColor(new Color(90, 65, 40));
        contents.add(board);

        // hole grid
        double startX = boardX + (boardW - GRID_WIDTH) / 2.0;
        double startY = boardY + (boardH - GRID_HEIGHT) / 2.0;
        double cellW = GRID_WIDTH / (COLS - 1);
        double cellH = GRID_HEIGHT / (ROWS - 1);

        holes.clear();

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                double cx = startX + col * cellW;
                double cy = startY + row * cellH;

                GOval shadow = new GOval(
                        cx - HOLE_RADIUS, cy - HOLE_RADIUS + 5,
                        HOLE_RADIUS * 2, HOLE_RADIUS * 2
                );
                shadow.setFilled(true);
                shadow.setFillColor(new Color(40, 30, 20));
                shadow.setColor(new Color(30, 20, 10));
                contents.add(shadow);

                GOval holeShape = new GOval(
                        cx - HOLE_RADIUS, cy - HOLE_RADIUS,
                        HOLE_RADIUS * 2, HOLE_RADIUS * 2
                );
                holeShape.setFilled(true);
                holeShape.setFillColor(new Color(70, 50, 30));
                holeShape.setColor(Color.BLACK);
                contents.add(holeShape);

                Hole hHole = new Hole(cx, cy);
                holes.add(hHole);
            }
        }

        // hammer (placeholder)
        GOval hammerShape = new GOval(0, 0, 40, 40);
        hammerShape.setFilled(true);
        hammerShape.setFillColor(new Color(220, 60, 60));
        hammerShape.setColor(Color.BLACK);
        hammer = hammerShape;
        contents.add(hammer);
    }

    private void setupTimer() {
        timer = new Timer(TICK_MS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tick(TICK_MS);
            }
        });
    }

    // ---------- game loop ----------

    private void tick(int deltaMs) {
        // update timer
        if (timeRemainingMs > 0) {
            timeRemainingMs -= deltaMs;
            if (timeRemainingMs < 0) timeRemainingMs = 0;
            updateTimerLabel();
        }

        // update rats
        for (Ratdg r : activeRats) {
            r.onTick(deltaMs);
        }

        activeRats.removeIf(r -> !r.isActive());

        // only spawn new rats while there is time left
        if (timeRemainingMs > 0) {
            maybeSpawn();
        }
    }

    private void updateTimerLabel() {
        int totalSeconds = timeRemainingMs / 1000;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        String text = String.format("%02d:%02d", minutes, seconds);
        timerLabel.setLabel(text);

        // keep right-aligned
        double w = mainScreen.getWidth();
        timerLabel.setLocation(
                w - timerLabel.getWidth() - 20,
                36
        );
    }

    private void maybeSpawn() {
        int currentlyVisible = 0;
        for (Hole h : holes) {
            if (h.isOccupied()) currentlyVisible++;
        }
        if (currentlyVisible >= MAX_ACTIVE_RATS) return;

        if (rng.nextDouble() > SPAWN_CHANCE) return;

        List<Hole> empty = new ArrayList<>();
        for (Hole h : holes) {
            if (!h.isOccupied()) empty.add(h);
        }
        if (empty.isEmpty()) return;

        Hole hole = empty.get(rng.nextInt(empty.size()));

        Ratdg rat;
        double p = rng.nextDouble();
        if (p < 0.65) {
            rat = new NormalRat(mainScreen);
        } else if (p < 0.9) {
            rat = new BonusRat(mainScreen);
        } else {
            rat = new TrapRat(mainScreen);
        }

        activeRats.add(rat);
        hole.spawn(rat);
    }

    // ---------- input ----------

    @Override
    public void mouseMoved(MouseEvent e) {
        if (hammer != null) {
            double hw = hammer.getWidth();
            double hh = hammer.getHeight();
            hammer.setLocation(e.getX() - hw / 2.0, e.getY() - hh / 2.0);
            hammer.sendToFront();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        GObject obj = mainScreen.getElementAt(e.getX(), e.getY());
        if (obj == backLabel || obj == topBar) {
            mainScreen.switchToWelcomeScreen();
            return;
        }
        handleWhack(e.getX(), e.getY());
    }

    private void handleWhack(double x, double y) {
        boolean hit = false;

        for (Ratdg r : activeRats) {
            if (r.isVisible() && r.containsPoint(x, y)) {
                hit = true;
                switch (r.getType()) {
                    case NORMAL:
                        mainScreen.addToScore(10);
                        break;
                    case BONUS:
                        mainScreen.addToScore(25);
                        break;
                    case TRAP:
                        mainScreen.addToScore(-15);
                        break;
                }
                r.despawn();
                break;
            }
        }

        if (!hit && timeRemainingMs > 0) {
            mainScreen.addToScore(-1);
        }
    }
}
