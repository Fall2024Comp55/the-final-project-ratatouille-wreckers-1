import acm.graphics.*;

import javax.swing.Timer;
import java.awt.Color;
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
    private static final double GRID_WIDTH = 450;
    private static final double GRID_HEIGHT = 250;
    private static final double HOLE_RADIUS = 30;

    // Timing
    private static final int TICK_MS = 40;          // ~25 FPS
    private static final double SPAWN_CHANCE = 0.04; // chance per tick
    private static final int MAX_ACTIVE_RATS = 4;

    private final List<Hole> holes = new ArrayList<>();
    private final List<Ratdg> activeRats = new ArrayList<>();
    private final Random rng = new Random();

    private Timer timer;
    private GObject hammer;

    public GamePane(MainApplication mainScreen) {
        this.mainScreen = mainScreen;
        buildBoard();
        setupTimer();
    }

    // ----------------- lifecycle -----------------
    @Override
    public void showContent() {
        // Add all static UI elements (board + hammer)
        for (GObject obj : contents) {
            mainScreen.add(obj);
        }
        if (hammer != null) {
            hammer.sendToFront();
        }
        if (timer != null) {
            timer.start();
        }

        // reset score each time we enter game (optional)
        mainScreen.setScore(0);
    }

    @Override
    public void hideContent() {
        if (timer != null) {
            timer.stop();
        }

        // Despawn all rats
        for (Ratdg r : activeRats) {
            r.despawn();
        }
        activeRats.clear();

        for (GObject obj : contents) {
            mainScreen.remove(obj);
        }
        contents.clear();

        // Rebuild for next time
        buildBoard();
    }

    // ----------------- board setup -----------------
    private void buildBoard() {
        // Simple background panel for game area
        GRect bg = new GRect(0, 0, mainScreen.getWidth(), mainScreen.getHeight());
        bg.setFilled(true);
        bg.setFillColor(new Color(180, 220, 255)); // light blue backdrop
        bg.setColor(Color.BLACK);
        contents.add(bg);

        // Title
        GLabel title = new GLabel("WHACK-A-RAT", 0, 0);
        title.setFont("Monospaced-BOLD-32");
        title.setColor(Color.DARK_GRAY);
        title.setLocation(
                (mainScreen.getWidth() - title.getWidth()) / 2.0,
                70
        );
        contents.add(title);

        // Hole grid
        double startX = (mainScreen.getWidth() - GRID_WIDTH) / 2.0;
        double startY = 140;
        double cellW = GRID_WIDTH / (COLS - 1);
        double cellH = GRID_HEIGHT / (ROWS - 1);

        holes.clear();

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                double cx = startX + col * cellW;
                double cy = startY + row * cellH;

                // Visual hole
                GOval holeShape = new GOval(
                        cx - HOLE_RADIUS,
                        cy - HOLE_RADIUS,
                        HOLE_RADIUS * 2,
                        HOLE_RADIUS * 2
                );
                holeShape.setFilled(true);
                holeShape.setFillColor(new Color(60, 40, 20)); // dark brown
                holeShape.setColor(Color.BLACK);
                contents.add(holeShape);

                Hole h = new Hole(cx, cy);
                holes.add(h);
            }
        }

        // Hammer cursor (simple circle for now)
        GOval hammerShape = new GOval(0, 0, 40, 40);
        hammerShape.setFilled(true);
        hammerShape.setFillColor(new Color(200, 50, 50));
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

    // ----------------- game loop -----------------
    private void tick(int deltaMs) {
        // update rats
        for (Ratdg r : activeRats) {
            r.onTick(deltaMs);
        }

        // remove fully inactive rats
        activeRats.removeIf(r -> !r.isActive());

        // spawn new ones
        maybeSpawn();
    }

    private void maybeSpawn() {
        // limit how many
        int currentlyVisible = 0;
        for (Hole h : holes) {
            if (h.isOccupied()) currentlyVisible++;
        }
        if (currentlyVisible >= MAX_ACTIVE_RATS) return;

        if (rng.nextDouble() > SPAWN_CHANCE) return;

        // pick a random empty hole
        List<Hole> empty = new ArrayList<>();
        for (Hole h : holes) {
            if (!h.isOccupied()) empty.add(h);
        }
        if (empty.isEmpty()) return;

        Hole hole = empty.get(rng.nextInt(empty.size()));

        Ratdg rat;
        double p = rng.nextDouble();
        if (p < 0.7) {
            rat = new NormalRat(mainScreen);
        } else if (p < 0.9) {
            rat = new BonusRat(mainScreen);
        } else {
            rat = new TrapRat(mainScreen);
        }

        activeRats.add(rat);
        hole.spawn(rat);
    }

    // ----------------- input handling -----------------
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

        // Miss penalty
        if (!hit) {
            mainScreen.addToScore(-1);
        }
    }
}
