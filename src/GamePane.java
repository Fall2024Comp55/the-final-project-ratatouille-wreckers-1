import acm.graphics.*;

import javax.swing.Timer;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GamePane extends GraphicsPane {

    // Layout
    private static final int ROWS = 3;
    private static final int COLS = 4;
    private static final double GRID_WIDTH = 520;
    private static final double GRID_HEIGHT = 260;
    private static final double HOLE_RADIUS = 30;

    // Timing
    private static final int TICK_MS = 40;
    private static final double SPAWN_CHANCE = 0.05;
    private static final int MAX_ACTIVE_RATS = 5;
    private static final int GAME_DURATION_MS = 1*60 * 1000;  // 1 minutes (level 1)
    private static final int BOSS_DURATION_MS = 1* 60 * 1000;      // 1 Minute(boss level)

    // HP / boss
    private static final int PLAYER_MAX_HP = 100;
    private static final int BOSS_MAX_HP = 300;
    private static final int BOSS_ATTACK_INTERVAL_MS = 1500;
    private static final int BOSS_ATTACK_DAMAGE = 10;
    private static final int BOSS_HIT_DAMAGE = 25;

    private enum Phase { NORMAL, BOSS, FINISHED }
    private Phase phase = Phase.NORMAL;

    private int timeRemainingMs = GAME_DURATION_MS;

    private int playerHp = PLAYER_MAX_HP;
    private int bossHp = BOSS_MAX_HP;
    private int bossAttackTimerMs = 0;

    private final List<Hole> holes = new ArrayList<>();
    private final List<Ratdg> activeRats = new ArrayList<>();
    private final Random rng = new Random();

    private Timer timer;
    private GObject hammer;
    private GRect topBar;
    private GLabel backLabel;
    private GLabel timerLabel;
    private GLabel gameOverLabel;
    private GLabel phaseLabel;

    // HP bars
    private GRect playerHpBack, playerHpFill;
    private GLabel playerHpLabel;
    private GRect bossHpBack, bossHpFill;
    private GLabel bossHpLabel;

    private boolean scoreRecorded = false;
    private BossRat bossRat = null;

    // Pause menu
    private boolean paused = false;
    private GRect pauseOverlay;
    private GRect pausePanel;
    private GRect resumeButtonRect;
    private GRect settingsButtonRect;
    private GRect exitButtonRect;
    private GLabel resumeLabel;
    private GLabel settingsLabel;
    private GLabel exitLabel;
    private GLabel pauseTitleLabel;

    // NEW: when true this pane is the dedicated boss level
    private final boolean bossModeOnly;

    // Level 1 constructor (normal rats, no boss UI)
    public GamePane(MainApplication mainScreen) {
        this(mainScreen, false);
    }

    // Level 2 constructor (boss-only)
    public GamePane(MainApplication mainScreen, boolean bossModeOnly) {
        this.mainScreen = mainScreen;
        this.bossModeOnly = bossModeOnly;
        setupTimer();
    }

    @Override
    public void showContent() {
        // clear previous drawings
        for (GObject obj : contents) {
            mainScreen.remove(obj);
        }
        contents.clear();
        holes.clear();
        activeRats.clear();
        bossRat = null;

        // draw board + UI
        buildBoard();

        // reset state
        phase = Phase.NORMAL;
        timeRemainingMs = bossModeOnly ? BOSS_DURATION_MS : GAME_DURATION_MS;
        playerHp = PLAYER_MAX_HP;
        bossHp = BOSS_MAX_HP;
        bossAttackTimerMs = 0;
        scoreRecorded = false;
        paused = false;

        updateTimerLabel();
        updatePlayerHpBar();
        updateBossHpBar();
        mainScreen.setScore(0);

        // boss level starts directly in boss phase
        if (bossModeOnly) {
            startBossPhase();
        }

        if (timer != null) timer.start();
    }

    @Override
    public void hideContent() {
        if (timer != null) timer.stop();

        for (Ratdg r : activeRats) {
            r.despawn();
        }
        activeRats.clear();
        bossRat = null;

        for (GObject obj : contents) {
            mainScreen.remove(obj);
        }
        contents.clear();

        paused = false;
    }

    // --------------------------------------------------------
    // Build board + UI
    // --------------------------------------------------------
    private void buildBoard() {
        double w = mainScreen.getWidth();
        double h = mainScreen.getHeight();

        // Sky & ground
        GRect sky = new GRect(0, 0, w, h * 0.6);
        sky.setFilled(true);
        sky.setFillColor(new Color(180, 210, 255));
        sky.setColor(Color.BLACK);
        contents.add(sky);
        mainScreen.add(sky);

        GRect ground = new GRect(0, h * 0.6, w, h * 0.4);
        ground.setFilled(true);
        ground.setFillColor(new Color(105, 180, 100));
        ground.setColor(Color.BLACK);
        contents.add(ground);
        mainScreen.add(ground);

        // Top bar
        topBar = new GRect(0, 0, w, 60);
        topBar.setFilled(true);
        topBar.setFillColor(new Color(35, 35, 35));
        topBar.setColor(Color.BLACK);
        contents.add(topBar);
        mainScreen.add(topBar);

        // French flag title
        double flagW = 260;
        double flagH = 32;
        double flagX = (w - flagW) / 2.0;
        double flagY = 14;

        Color blue = new Color(58, 91, 170);
        Color white = new Color(245, 245, 245);
        Color red = new Color(203, 58, 74);

        GRect stripeBlue = new GRect(flagX, flagY, flagW / 3.0, flagH);
        stripeBlue.setFilled(true);
        stripeBlue.setFillColor(blue);
        stripeBlue.setColor(blue);
        contents.add(stripeBlue);
        mainScreen.add(stripeBlue);

        GRect stripeWhite = new GRect(flagX + flagW / 3.0, flagY, flagW / 3.0, flagH);
        stripeWhite.setFilled(true);
        stripeWhite.setFillColor(white);
        stripeWhite.setColor(white);
        contents.add(stripeWhite);
        mainScreen.add(stripeWhite);

        GRect stripeRed = new GRect(flagX + 2 * flagW / 3.0, flagY, flagW / 3.0, flagH);
        stripeRed.setFilled(true);
        stripeRed.setFillColor(red);
        stripeRed.setColor(red);
        contents.add(stripeRed);
        mainScreen.add(stripeRed);

        GLabel title = new GLabel("WRECK-IT RATZ");
        title.setFont(new Font("Monospaced", Font.BOLD, 20));
        title.setColor(Color.BLACK);
        title.setLocation((w - title.getWidth()) / 2.0, flagY + flagH * 0.7);
        contents.add(title);
        mainScreen.add(title);

        // Timer
        timerLabel = new GLabel("03:00");
        timerLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        timerLabel.setColor(Color.WHITE);
        timerLabel.setLocation(20, 36);
        contents.add(timerLabel);
        mainScreen.add(timerLabel);

        // Back
        backLabel = new GLabel("< BACK");
        backLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        backLabel.setColor(new Color(220, 220, 220));
        backLabel.setLocation(120, 36);
        contents.add(backLabel);
        mainScreen.add(backLabel);

        // Player HP (only in boss level)
        playerHpBack = new GRect(20, 50, 160, 10);
        playerHpBack.setFilled(true);
        playerHpBack.setFillColor(new Color(80, 80, 80));
        playerHpBack.setColor(Color.BLACK);

        playerHpFill = new GRect(20, 50, 160, 10);
        playerHpFill.setFilled(true);
        playerHpFill.setFillColor(new Color(60, 200, 80));
        playerHpFill.setColor(Color.BLACK);

        playerHpLabel = new GLabel("HP");
        playerHpLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        playerHpLabel.setColor(Color.WHITE);
        playerHpLabel.setLocation(20, 48);

        if (bossModeOnly) {
            contents.add(playerHpBack);
            contents.add(playerHpFill);
            contents.add(playerHpLabel);
            mainScreen.add(playerHpBack);
            mainScreen.add(playerHpFill);
            mainScreen.add(playerHpLabel);
        }

        // Boss HP (only in boss level)
        bossHpBack = new GRect(w / 2.0 - 180, 10, 360, 8);
        bossHpBack.setFilled(true);
        bossHpBack.setFillColor(new Color(90, 90, 90));
        bossHpBack.setColor(Color.BLACK);

        bossHpFill = new GRect(w / 2.0 - 180, 10, 360, 8);
        bossHpFill.setFilled(true);
        bossHpFill.setFillColor(new Color(220, 60, 60));
        bossHpFill.setColor(Color.BLACK);

        bossHpLabel = new GLabel("BOSS HP");
        bossHpLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        bossHpLabel.setColor(Color.WHITE);
        bossHpLabel.setLocation(w / 2.0 - bossHpLabel.getWidth() / 2.0, 9);

        if (bossModeOnly) {
            contents.add(bossHpBack);
            contents.add(bossHpFill);
            contents.add(bossHpLabel);
            mainScreen.add(bossHpBack);
            mainScreen.add(bossHpFill);
            mainScreen.add(bossHpLabel);
        }

        // Board wood
        double boardW = GRID_WIDTH + 80;
        double boardH = GRID_HEIGHT + 80;
        double boardX = (w - boardW) / 2.0;
        double boardY = 120;

        GRect boardOuter = new GRect(boardX, boardY, boardW, boardH);
        boardOuter.setFilled(true);
        boardOuter.setFillColor(new Color(130, 96, 60));
        boardOuter.setColor(new Color(80, 55, 35));
        contents.add(boardOuter);
        mainScreen.add(boardOuter);

        GRect boardInner = new GRect(boardX + 10, boardY + 10, boardW - 20, boardH - 20);
        boardInner.setFilled(true);
        boardInner.setFillColor(new Color(156, 120, 80));
        boardInner.setColor(new Color(90, 65, 40));
        contents.add(boardInner);
        mainScreen.add(boardInner);

        // wood grain
        for (int i = 0; i < 6; i++) {
            double lx = boardInner.getX() + 20 + i * (boardInner.getWidth() / 6.0);
            GLine grain = new GLine(
                    lx,
                    boardInner.getY() + 5,
                    lx,
                    boardInner.getY() + boardInner.getHeight() - 5
            );
            grain.setColor(new Color(150, 115, 75));
            contents.add(grain);
            mainScreen.add(grain);
        }

        // Holes
        double startX = boardX + (boardW - GRID_WIDTH) / 2.0;
        double startY = boardY + (boardH - GRID_HEIGHT) / 2.0;
        double cellW = GRID_WIDTH / (COLS - 1);
        double cellH = GRID_HEIGHT / (ROWS - 1);

        holes.clear();
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                double cx = startX + col * cellW;
                double cy = startY + row * cellH;

                GOval shadow = new GOval(cx - HOLE_RADIUS, cy - HOLE_RADIUS + 6,
                        HOLE_RADIUS * 2, HOLE_RADIUS * 2);
                shadow.setFilled(true);
                shadow.setFillColor(new Color(45, 35, 25));
                shadow.setColor(new Color(25, 18, 10));
                contents.add(shadow);
                mainScreen.add(shadow);

                GOval rim = new GOval(cx - HOLE_RADIUS, cy - HOLE_RADIUS,
                        HOLE_RADIUS * 2, HOLE_RADIUS * 2);
                rim.setFilled(true);
                rim.setFillColor(new Color(80, 55, 35));
                rim.setColor(Color.BLACK);
                contents.add(rim);
                mainScreen.add(rim);

                GOval inner = new GOval(cx - HOLE_RADIUS + 4, cy - HOLE_RADIUS + 4,
                        HOLE_RADIUS * 2 - 8, HOLE_RADIUS * 2 - 8);
                inner.setFilled(true);
                inner.setFillColor(new Color(60, 42, 28));
                inner.setColor(new Color(40, 28, 18));
                contents.add(inner);
                mainScreen.add(inner);

                holes.add(new Hole(cx, cy));
            }
        }

        // Hammer
        GOval hammerShape = new GOval(0, 0, 40, 40);
        hammerShape.setFilled(true);
        hammerShape.setFillColor(new Color(230, 70, 70));
        hammerShape.setColor(Color.BLACK);
        hammer = hammerShape;
        contents.add(hammer);
        mainScreen.add(hammer);
    }

    private void setupTimer() {
        timer = new Timer(TICK_MS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tick(TICK_MS);
            }
        });
    }

    // --------------------------------------------------------
    // Game loop
    // --------------------------------------------------------
    private void tick(int deltaMs) {
        if (paused) return;

        if (timeRemainingMs > 0) {
            timeRemainingMs -= deltaMs;
            if (timeRemainingMs <= 0) {
                timeRemainingMs = 0;
                onTimeExpired();
            }
            updateTimerLabel();
        }

        for (Ratdg r : activeRats) {
            r.onTick(deltaMs);
        }
        activeRats.removeIf(r -> !r.isActive());

        // Only spawn rats in level 1 (normal mode)
        if (!bossModeOnly && phase == Phase.NORMAL && timeRemainingMs > 0) {
            maybeSpawn();
        }

        if (phase == Phase.BOSS && bossRat != null && bossRat.isVisible()) {
            bossAttackTimerMs += deltaMs;
            if (bossAttackTimerMs >= BOSS_ATTACK_INTERVAL_MS) {
                bossAttackTimerMs = 0;
                playerHp -= BOSS_ATTACK_DAMAGE;
                if (playerHp < 0) playerHp = 0;
                updatePlayerHpBar();
                if (playerHp == 0) {
                    phase = Phase.FINISHED;
                    if (!scoreRecorded) {
                        scoreRecorded = true;
                        mainScreen.recordScore(mainScreen.getScore());
                    }
                    showGameOverOverlay("YOU WERE WRECKED!");
                }
            }
        }
    }

    private void onTimeExpired() {
        if (phase == Phase.NORMAL) {
            // Level 1 finished -> go to dedicated boss screen
            phase = Phase.FINISHED;
            if (timer != null) timer.stop();
            mainScreen.switchToBossScreen();
        } else if (phase == Phase.BOSS) {
            phase = Phase.FINISHED;
            if (!scoreRecorded) {
                scoreRecorded = true;
                mainScreen.recordScore(mainScreen.getScore());
            }
            showGameOverOverlay("BOSS ESCAPED!");
        }
    }

    private void startBossPhase() {
        phase = Phase.BOSS;
        timeRemainingMs = BOSS_DURATION_MS;
        updateTimerLabel();

        for (Ratdg r : activeRats) r.despawn();
        activeRats.clear();

        double w = mainScreen.getWidth();
        phaseLabel = new GLabel("BOSS FIGHT!");
        phaseLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        phaseLabel.setColor(new Color(230, 230, 80));
        phaseLabel.setLocation((w - phaseLabel.getWidth()) / 2.0, 85);
        contents.add(phaseLabel);
        mainScreen.add(phaseLabel);

        double centerX = mainScreen.getWidth() / 2.0;
        double centerY = 120 + 200;
        bossRat = new BossRat(mainScreen);
        activeRats.add(bossRat);
        Hole temp = new Hole(centerX, centerY);
        temp.spawn(bossRat);

        bossHp = BOSS_MAX_HP;
        bossAttackTimerMs = 0;
        updateBossHpBar();
    }

    private void updateTimerLabel() {
        int totalSeconds = timeRemainingMs / 1000;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        timerLabel.setLabel(String.format("%02d:%02d", minutes, seconds));
    }

    private void updatePlayerHpBar() {
        if (playerHpFill == null) return;
        double ratio = playerHp / (double) PLAYER_MAX_HP;
        double fullWidth = 160;
        playerHpFill.setSize(fullWidth * ratio, playerHpFill.getHeight());

        if (ratio > 0.6) playerHpFill.setFillColor(new Color(60, 200, 80));
        else if (ratio > 0.3) playerHpFill.setFillColor(new Color(230, 190, 60));
        else playerHpFill.setFillColor(new Color(220, 60, 60));
    }

    private void updateBossHpBar() {
        if (bossHpFill == null) return;
        double ratio = bossHp / (double) BOSS_MAX_HP;
        double fullWidth = 360;
        bossHpFill.setSize(fullWidth * ratio, bossHpFill.getHeight());
    }

    private void maybeSpawn() {
        int visible = 0;
        for (Hole h : holes) {
            if (h.isOccupied()) visible++;
        }
        if (visible >= MAX_ACTIVE_RATS) return;

        if (rng.nextDouble() > SPAWN_CHANCE) return;

        List<Hole> empty = new ArrayList<>();
        for (Hole h : holes) {
            if (!h.isOccupied()) empty.add(h);
        }
        if (empty.isEmpty()) return;

        Hole hole = empty.get(rng.nextInt(empty.size()));

        Ratdg rat;
        double p = rng.nextDouble();
        if (p < 0.65) rat = new NormalRat(mainScreen);
        else if (p < 0.9) rat = new BonusRat(mainScreen);
        else rat = new TrapRat(mainScreen);

        activeRats.add(rat);
        hole.spawn(rat);
    }

    private void showGameOverOverlay(String text) {
        if (timer != null) timer.stop();

        double w = mainScreen.getWidth();
        double h = mainScreen.getHeight();

        gameOverLabel = new GLabel(text);
        gameOverLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        gameOverLabel.setColor(Color.WHITE);
        gameOverLabel.setLocation((w - gameOverLabel.getWidth()) / 2.0, h * 0.45);

        GRect shadow = new GRect(
                gameOverLabel.getX() - 30,
                gameOverLabel.getY() - gameOverLabel.getAscent() - 15,
                gameOverLabel.getWidth() + 60,
                gameOverLabel.getAscent() + gameOverLabel.getDescent() + 35
        );
        shadow.setFilled(true);
        shadow.setFillColor(new Color(0, 0, 0, 180));
        shadow.setColor(Color.BLACK);

        contents.add(shadow);
        contents.add(gameOverLabel);
        mainScreen.add(shadow);
        mainScreen.add(gameOverLabel);

        shadow.sendToFront();
        gameOverLabel.sendToFront();
        if (hammer != null) hammer.sendToFront();

        // NEW: after 2 seconds, go back to main menu
        new Timer(2000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((Timer) e.getSource()).stop();
                mainScreen.switchToWelcomeScreen();
            }
        }).start();
    }


    // --------------------------------------------------------
    // Pause menu
    // --------------------------------------------------------
    private void togglePause() {
        if (phase == Phase.FINISHED) return;
        if (paused) hidePauseMenu(); else showPauseMenu();
    }

    private void showPauseMenu() {
        if (paused) return;
        paused = true;
        if (timer != null) timer.stop();

        double w = mainScreen.getWidth();
        double h = mainScreen.getHeight();

        pauseOverlay = new GRect(0, 0, w, h);
        pauseOverlay.setFilled(true);
        pauseOverlay.setFillColor(new Color(0, 0, 0, 120));
        pauseOverlay.setColor(new Color(0, 0, 0, 0));
        contents.add(pauseOverlay);
        mainScreen.add(pauseOverlay);

        double panelW = 320;
        double panelH = 230;
        double panelX = (w - panelW) / 2.0;
        double panelY = (h - panelH) / 2.0;

        pausePanel = new GRect(panelX, panelY, panelW, panelH);
        pausePanel.setFilled(true);
        pausePanel.setFillColor(new Color(245, 245, 245));
        pausePanel.setColor(new Color(60, 60, 60));
        contents.add(pausePanel);
        mainScreen.add(pausePanel);

        pauseTitleLabel = new GLabel("PAUSED");
        pauseTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        pauseTitleLabel.setColor(new Color(40, 40, 40));
        pauseTitleLabel.setLocation(
                panelX + (panelW - pauseTitleLabel.getWidth()) / 2.0,
                panelY + 40
        );
        contents.add(pauseTitleLabel);
        mainScreen.add(pauseTitleLabel);

        int btnW = 200;
        int btnH = 40;
        int gap = 15;
        double btnX = panelX + (panelW - btnW) / 2.0;
        double firstY = panelY + 70;

        resumeButtonRect = createPauseButton(btnX, firstY, btnW, btnH);
        resumeLabel = createPauseLabel("RESUME", resumeButtonRect);

        settingsButtonRect = createPauseButton(btnX, firstY + btnH + gap, btnW, btnH);
        settingsLabel = createPauseLabel("SETTINGS", settingsButtonRect);

        exitButtonRect = createPauseButton(btnX, firstY + 2 * (btnH + gap), btnW, btnH);
        exitLabel = createPauseLabel("EXIT TO MENU", exitButtonRect);

        bringPauseToFront();
    }

    private GRect createPauseButton(double x, double y, double w, double h) {
        GRect r = new GRect(x, y, w, h);
        r.setFilled(true);
        r.setFillColor(new Color(230, 230, 230));
        r.setColor(new Color(90, 90, 90));
        contents.add(r);
        mainScreen.add(r);
        return r;
    }

    private GLabel createPauseLabel(String text, GRect buttonRect) {
        GLabel lbl = new GLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 16));
        lbl.setColor(new Color(40, 40, 40));
        lbl.setLocation(
                buttonRect.getX() + (buttonRect.getWidth() - lbl.getWidth()) / 2.0,
                buttonRect.getY() + buttonRect.getHeight() * 0.65
        );
        contents.add(lbl);
        mainScreen.add(lbl);
        return lbl;
    }

    private void bringPauseToFront() {
        if (pauseOverlay != null) pauseOverlay.sendToFront();
        if (pausePanel != null) pausePanel.sendToFront();
        if (pauseTitleLabel != null) pauseTitleLabel.sendToFront();
        if (resumeButtonRect != null) resumeButtonRect.sendToFront();
        if (settingsButtonRect != null) settingsButtonRect.sendToFront();
        if (exitButtonRect != null) exitButtonRect.sendToFront();
        if (resumeLabel != null) resumeLabel.sendToFront();
        if (settingsLabel != null) settingsLabel.sendToFront();
        if (exitLabel != null) exitLabel.sendToFront();
        if (hammer != null) hammer.sendToFront();
    }

    private void hidePauseMenu() {
        if (!paused) return;
        paused = false;

        removeIfNotNull(pauseOverlay);
        removeIfNotNull(pausePanel);
        removeIfNotNull(pauseTitleLabel);
        removeIfNotNull(resumeButtonRect);
        removeIfNotNull(settingsButtonRect);
        removeIfNotNull(exitButtonRect);
        removeIfNotNull(resumeLabel);
        removeIfNotNull(settingsLabel);
        removeIfNotNull(exitLabel);

        pauseOverlay = pausePanel = null;
        resumeButtonRect = settingsButtonRect = exitButtonRect = null;
        resumeLabel = settingsLabel = exitLabel = null;
        pauseTitleLabel = null;

        if (phase != Phase.FINISHED && timer != null) timer.start();
    }

    private void removeIfNotNull(GObject obj) {
        if (obj == null) return;
        mainScreen.remove(obj);
        contents.remove(obj);
    }

    private void handlePauseClick(double x, double y) {
        if (pausePanel != null && !pausePanel.contains(x, y)) {
            hidePauseMenu();
            return;
        }

        GObject clicked = mainScreen.getElementAt(x, y);
        if (clicked == null) return;

        if (clicked == resumeButtonRect || clicked == resumeLabel) {
            hidePauseMenu();
        } else if (clicked == settingsButtonRect || clicked == settingsLabel) {
            hidePauseMenu();
            mainScreen.switchToSettingsFromGame();
        } else if (clicked == exitButtonRect || clicked == exitLabel) {
            hidePauseMenu();
            mainScreen.switchToWelcomeScreen();
        }
    }

    // --------------------------------------------------------
    // Input
    // --------------------------------------------------------
    @Override
    public void mouseMoved(MouseEvent e) {
        if (paused) return;
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
        if (paused) {
            handlePauseClick(e.getX(), e.getY());
            return;
        }

        GObject obj = mainScreen.getElementAt(e.getX(), e.getY());
        if (obj == backLabel || obj == topBar) {
            mainScreen.switchToWelcomeScreen();
            return;
        }
        handleWhack(e.getX(), e.getY());
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            togglePause();
        }
    }

    private void handleWhack(double x, double y) {
        // boss click
        if (phase == Phase.BOSS && bossRat != null && bossRat.isVisible()
                && bossRat.containsPoint(x, y)) {

            bossHp -= BOSS_HIT_DAMAGE;
            if (bossHp < 0) bossHp = 0;
            updateBossHpBar();
            mainScreen.addToScore(250);

            if (bossHp == 0) {
                bossRat.despawn();
                activeRats.remove(bossRat);
                bossRat = null;
                phase = Phase.FINISHED;
                timeRemainingMs = 0;
                updateTimerLabel();

                if (!scoreRecorded) {
                    scoreRecorded = true;
                    mainScreen.recordScore(mainScreen.getScore());
                }
                showGameOverOverlay("BOSS DEFEATED!");
            }
            return;
        }

        // normal rats
        boolean hit = false;
        for (Ratdg r : activeRats) {
            if (r == bossRat) continue;
            if (r.isVisible() && r.containsPoint(x, y)) {
                hit = true;
                switch (r.getType()) {
                    case NORMAL:
                        mainScreen.addToScore(50);
                        break;
                    case BONUS:
                        mainScreen.addToScore(100);
                        break;
                    case TRAP:
                        mainScreen.addToScore(-25);
                        break;
                    default:
                        break;
                }
                r.despawn();
                break;
            }
        }

        if (!hit && phase == Phase.NORMAL && timeRemainingMs > 0) {
            mainScreen.addToScore(-1);
        }
    }
}
