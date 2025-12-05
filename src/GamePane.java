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
    private static final int GAME_DURATION_MS = 25 * 1000; // level 1
    private static final int BOSS_DURATION_MS = 45 * 1000; // boss level

    // HP / boss
    private static final int PLAYER_MAX_HP = 100;
    private static final int BOSS_MAX_HP = 300;
    private static final int BOSS_ATTACK_INTERVAL_MS = 1500;
    private static final int BOSS_ATTACK_DAMAGE = 10;
    private static final int BOSS_HIT_DAMAGE = 25;

    // Power-ups
    private static final int POWERUP_DURATION_MS = 5000;   // 5 seconds active
    private static final int POWERUP_COOLDOWN_MS = 10000;  // 10 seconds cooldown

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
    private GObject bossBackdrop;
    private GLabel backLabel;
    private GLabel timerLabel;
    private GLabel gameOverLabel;
    private GLabel phaseLabel;

    // HP bars (only used in boss mode)
    private GRect playerHpBack, playerHpFill;
    private GLabel playerHpLabel;
    private GRect bossHpBack, bossHpFill;
    private GLabel bossHpLabel;

    private boolean scoreRecorded = false;
    private BossRat bossRat = null;

    // Boss movement
    private double bossX, bossY;
    private double bossVX = 0.12;   // pixels per ms
    private double bossVY = 0.09;   // pixels per ms

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

    // Boss-focus overlay
    private GRect bossOverlay;

    // Power-up buttons + status
    private GRect freezeButtonRect;
    private GRect x2PointsButtonRect;
    private GRect x2CritButtonRect;
    private GLabel freezeLabel;
    private GLabel x2PointsLabel;
    private GLabel x2CritLabel;
    private GLabel powerupStatusLabel;

    // Power-up state
    private boolean freezeActive = false;
    private int freezeRemainingMs = 0;
    private int freezeCooldownRemainingMs = 0;

    private boolean doublePointsActive = false;
    private int doublePointsRemainingMs = 0;
    private int doublePointsCooldownRemainingMs = 0;

    private boolean critActive = false;
    private int critRemainingMs = 0;
    private int critCooldownRemainingMs = 0;

    // When true, this pane is the dedicated boss level
    private final boolean bossModeOnly;

    // Level 1 constructor
    public GamePane(MainApplication mainScreen) {
        this(mainScreen, false);
    }

    // Boss-only constructor
    public GamePane(MainApplication mainScreen, boolean bossModeOnly) {
        this.mainScreen = mainScreen;
        this.bossModeOnly = bossModeOnly;
        setupTimer();
    }

    @Override
    public void showContent() {
        // wipe previous drawings
        for (GObject obj : contents) {
            mainScreen.remove(obj);
        }
        contents.clear();
        holes.clear();
        activeRats.clear();
        bossRat = null;

        // draw board/UI
        buildBoard();

        // reset game state
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

        resetPowerups();  // all power-ups fresh at start

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

        // French-flag title bar
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

        // Player HP bar (only added in boss mode)
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

        // Boss HP bar (only added in boss mode)
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

        // Darken background for boss mode
        if (bossModeOnly) {
            bossOverlay = new GRect(0, 0, w, h);
            bossOverlay.setFilled(true);
            bossOverlay.setFillColor(new Color(0, 0, 0, 90)); // semi-transparent
            bossOverlay.setColor(new Color(0, 0, 0, 0));
            contents.add(bossOverlay);
            mainScreen.add(bossOverlay);
        }

        // --- Power-up buttons along the bottom (higher) ---
        double btnW = 140;
        double btnH = 32;
        double btnGap = 12;
        double totalBtnW = 3 * btnW + 2 * btnGap;
        double btnStartX = (w - totalBtnW) / 2.0;
        double btnY = h - 110;

        Color btnColor = new Color(30, 30, 30);

        // Freeze
        freezeButtonRect = new GRect(btnStartX, btnY, btnW, btnH);
        freezeButtonRect.setFilled(true);
        freezeButtonRect.setFillColor(btnColor);
        freezeButtonRect.setColor(Color.BLACK);
        contents.add(freezeButtonRect);
        mainScreen.add(freezeButtonRect);

        freezeLabel = new GLabel("FREEZE");
        freezeLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        freezeLabel.setColor(Color.WHITE);
        freezeLabel.setLocation(
                btnStartX + (btnW - freezeLabel.getWidth()) / 2.0,
                btnY + btnH * 0.65
        );
        contents.add(freezeLabel);
        mainScreen.add(freezeLabel);

        // x2 Points
        double x2X = btnStartX + btnW + btnGap;
        x2PointsButtonRect = new GRect(x2X, btnY, btnW, btnH);
        x2PointsButtonRect.setFilled(true);
        x2PointsButtonRect.setFillColor(btnColor);
        x2PointsButtonRect.setColor(Color.BLACK);
        contents.add(x2PointsButtonRect);
        mainScreen.add(x2PointsButtonRect);

        x2PointsLabel = new GLabel("x2 POINTS");
        x2PointsLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        x2PointsLabel.setColor(Color.WHITE);
        x2PointsLabel.setLocation(
                x2X + (btnW - x2PointsLabel.getWidth()) / 2.0,
                btnY + btnH * 0.65
        );
        contents.add(x2PointsLabel);
        mainScreen.add(x2PointsLabel);

        // x2 Crit
        double critX = btnStartX + 2 * (btnW + btnGap);
        x2CritButtonRect = new GRect(critX, btnY, btnW, btnH);
        x2CritButtonRect.setFilled(true);
        x2CritButtonRect.setFillColor(btnColor);
        x2CritButtonRect.setColor(Color.BLACK);
        contents.add(x2CritButtonRect);
        mainScreen.add(x2CritButtonRect);

        x2CritLabel = new GLabel("x2 CRIT");
        x2CritLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        x2CritLabel.setColor(Color.WHITE);
        x2CritLabel.setLocation(
                critX + (btnW - x2CritLabel.getWidth()) / 2.0,
                btnY + btnH * 0.65
        );
        contents.add(x2CritLabel);
        mainScreen.add(x2CritLabel);

        // Status message on the right
        powerupStatusLabel = new GLabel("");
        powerupStatusLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        powerupStatusLabel.setColor(Color.WHITE);
        powerupStatusLabel.setLocation(w - 260, btnY - 8);
        contents.add(powerupStatusLabel);
        mainScreen.add(powerupStatusLabel);

        // Hammer – using custom cursor now, so commented out
        // GOval hammerShape = new GOval(0, 0, 40, 40);
        // hammerShape.setFilled(true);
        // hammerShape.setFillColor(new Color(230, 70, 70));
        // hammerShape.setColor(Color.BLACK);
        // hammer = hammerShape;
        // contents.add(hammer);
        // mainScreen.add(hammer);
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

        // update power-up timers
        updatePowerups(deltaMs);

        // scaled time for rats / boss (freeze slows these down a lot)
        double timeScale = freezeActive ? 0.3 : 1.0;
        int scaledDelta = (int) Math.max(1, Math.round(deltaMs * timeScale));

        // global game timer is NOT slowed by freeze
        if (timeRemainingMs > 0) {
            timeRemainingMs -= deltaMs;
            if (timeRemainingMs <= 0) {
                timeRemainingMs = 0;
                onTimeExpired();
            }
            updateTimerLabel();
        }

        // rat lifetimes
        for (Ratdg r : activeRats) {
            r.onTick(scaledDelta);
        }
        activeRats.removeIf(r -> !r.isActive());

        // Only spawn rats in level 1
        double spawnScale = freezeActive ? 0.3 : 1.0; // fewer spawns while frozen
        if (!bossModeOnly && phase == Phase.NORMAL && timeRemainingMs > 0) {
            maybeSpawn(spawnScale);
        }

        // boss logic (attacks + movement)
        if (phase == Phase.BOSS && bossRat != null && bossRat.isVisible()) {
            bossAttackTimerMs += scaledDelta;
            if (bossAttackTimerMs >= BOSS_ATTACK_INTERVAL_MS) {
                bossAttackTimerMs = 0;
                playerHp -= BOSS_ATTACK_DAMAGE;
                if (playerHp < 0) playerHp = 0;
                updatePlayerHpBar();
                if (playerHp == 0) {
                    phase = Phase.FINISHED;
                    if (!scoreRecorded) {
                        scoreRecorded = true;
                        int savedScore = bossModeOnly ? mainScreen.getPreBossScore()
                                                      : mainScreen.getScore();
                        mainScreen.recordScore(savedScore);
                    }
                    showGameOverOverlay("YOU WERE WRECKED!");
                }
            }

            moveBoss(scaledDelta);   // <--- NEW: boss movement
        }
    }

    // Move boss & bounce inside a central rectangle
    private void moveBoss(int deltaMs) {
        if (bossRat == null) return;

        double w = mainScreen.getWidth();
        double h = mainScreen.getHeight();

        // movement based on velocity (px per ms)
        bossX += bossVX * deltaMs;
        bossY += bossVY * deltaMs;

        // bounds for movement (stay inside dark area)
        double marginX = 120;
        double marginTop = 130;
        double marginBottom = 120;

        // bounce horizontally
        if (bossX < marginX) {
            bossX = marginX;
            bossVX = -bossVX;
        } else if (bossX > w - marginX) {
            bossX = w - marginX;
            bossVX = -bossVX;
        }

        // bounce vertically
        if (bossY < marginTop) {
            bossY = marginTop;
            bossVY = -bossVY;
        } else if (bossY > h - marginBottom) {
            bossY = h - marginBottom;
            bossVY = -bossVY;
        }

        bossRat.setPosition(bossX, bossY);
    }

    // power-up timer / cooldown logic
    private void updatePowerups(int deltaMs) {
        // Freeze
        if (freezeActive) {
            freezeRemainingMs -= deltaMs;
            if (freezeRemainingMs <= 0) {
                freezeRemainingMs = 0;
                freezeActive = false;
            }
        }
        if (freezeCooldownRemainingMs > 0) {
            freezeCooldownRemainingMs -= deltaMs;
            if (freezeCooldownRemainingMs <= 0) {
                freezeCooldownRemainingMs = 0;
            }
        }

        // x2 points
        if (doublePointsActive) {
            doublePointsRemainingMs -= deltaMs;
            if (doublePointsRemainingMs <= 0) {
                doublePointsRemainingMs = 0;
                doublePointsActive = false;
            }
        }
        if (doublePointsCooldownRemainingMs > 0) {
            doublePointsCooldownRemainingMs -= deltaMs;
            if (doublePointsCooldownRemainingMs <= 0) {
                doublePointsCooldownRemainingMs = 0;
            }
        }

        // x2 crit
        if (critActive) {
            critRemainingMs -= deltaMs;
            if (critRemainingMs <= 0) {
                critRemainingMs = 0;
                critActive = false;
            }
        }
        if (critCooldownRemainingMs > 0) {
            critCooldownRemainingMs -= deltaMs;
            if (critCooldownRemainingMs <= 0) {
                critCooldownRemainingMs = 0;
            }
        }

        updatePowerupButtonVisuals();
    }

    private void resetPowerups() {
        freezeActive = false;
        freezeRemainingMs = 0;
        freezeCooldownRemainingMs = 0;

        doublePointsActive = false;
        doublePointsRemainingMs = 0;
        doublePointsCooldownRemainingMs = 0;

        critActive = false;
        critRemainingMs = 0;
        critCooldownRemainingMs = 0;

        if (powerupStatusLabel != null) {
            powerupStatusLabel.setLabel("");
        }
        updatePowerupButtonVisuals();
    }

    private void updatePowerupButtonVisuals() {
        Color activeOrCd = new Color(80, 80, 80);
        Color normal = new Color(30, 30, 30);

        if (freezeButtonRect != null) {
            freezeButtonRect.setFillColor(
                    (freezeActive || freezeCooldownRemainingMs > 0) ? activeOrCd : normal
            );
        }
        if (x2PointsButtonRect != null) {
            x2PointsButtonRect.setFillColor(
                    (doublePointsActive || doublePointsCooldownRemainingMs > 0) ? activeOrCd : normal
            );
        }
        if (x2CritButtonRect != null) {
            x2CritButtonRect.setFillColor(
                    (critActive || critCooldownRemainingMs > 0) ? activeOrCd : normal
            );
        }
    }

    private void showCooldownMessage() {
        if (powerupStatusLabel == null) return;

        powerupStatusLabel.setLabel("powerup cooling wait 10s");

        Timer t = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                powerupStatusLabel.setLabel("");
                ((Timer) e.getSource()).stop();
            }
        });
        t.setRepeats(false);
        t.start();
    }

    // bring power-up UI in front of boss backdrop (for boss level)
    private void bringPowerupsToFront() {
        if (freezeButtonRect != null) freezeButtonRect.sendToFront();
        if (x2PointsButtonRect != null) x2PointsButtonRect.sendToFront();
        if (x2CritButtonRect != null) x2CritButtonRect.sendToFront();
        if (freezeLabel != null) freezeLabel.sendToFront();
        if (x2PointsLabel != null) x2PointsLabel.sendToFront();
        if (x2CritLabel != null) x2CritLabel.sendToFront();
        if (powerupStatusLabel != null) powerupStatusLabel.sendToFront();
    }

    private void createBossBackdrop() {
        double w = mainScreen.getWidth();
        double h = mainScreen.getHeight();

        try {
            GImage img = new GImage("boss_bg.png");
            img.setSize(w, h - 60); // below the top bar
            img.setLocation(0, 60);
            bossBackdrop = img;
        } catch (Exception ex) {
            // Fallback: dark "old classroom" style chalkboard
            GRect dark = new GRect(0, 60, w, h - 60);
            dark.setFilled(true);
            dark.setFillColor(new Color(20, 20, 28));
            dark.setColor(Color.BLACK);
            bossBackdrop = dark;

            // Wood frame
            GRect frame = new GRect(80, 90, w - 160, h - 190);
            frame.setFilled(true);
            frame.setFillColor(new Color(90, 60, 40));
            frame.setColor(Color.BLACK);
            contents.add(frame);
            mainScreen.add(frame);

            // Green chalkboard inside
            GRect chalk = new GRect(95, 105, w - 190, h - 220);
            chalk.setFilled(true);
            chalk.setFillColor(new Color(20, 60, 40));
            chalk.setColor(new Color(10, 25, 15));
            contents.add(chalk);
            mainScreen.add(chalk);
        }

        contents.add(bossBackdrop);
        mainScreen.add(bossBackdrop);

        // Make sure UI stays on top of the dark background
        if (topBar != null) topBar.sendToFront();
        if (timerLabel != null) timerLabel.sendToFront();
        if (backLabel != null) backLabel.sendToFront();

        if (playerHpBack != null) {
            playerHpBack.sendToFront();
            playerHpFill.sendToFront();
            playerHpLabel.sendToFront();
        }
        if (bossHpBack != null) {
            bossHpBack.sendToFront();
            bossHpFill.sendToFront();
            bossHpLabel.sendToFront();
        }

        // also power-ups on top
        bringPowerupsToFront();
    }

    private void onTimeExpired() {
        if (phase == Phase.NORMAL) {
            // END OF LEVEL 1 → go to boss screen
            phase = Phase.FINISHED;
            if (timer != null) timer.stop();

            // store score BEFORE boss
            mainScreen.setPreBossScore(mainScreen.getScore());

            mainScreen.switchToBossScreen();
        } else if (phase == Phase.BOSS) {
            phase = Phase.FINISHED;
            if (!scoreRecorded) {
                scoreRecorded = true;
                int savedScore = bossModeOnly ? mainScreen.getPreBossScore()
                                              : mainScreen.getScore();
                mainScreen.recordScore(savedScore);
            }
            showGameOverOverlay("BOSS ESCAPED!");
        }
    }

    private void startBossPhase() {
        phase = Phase.BOSS;
        timeRemainingMs = BOSS_DURATION_MS;
        updateTimerLabel();

        // Clear all normal rats
        for (Ratdg r : activeRats) {
            r.despawn();
        }
        activeRats.clear();

        // Create dark / classroom boss background
        createBossBackdrop();

        double w = mainScreen.getWidth();

        // "BOSS FIGHT" label
        phaseLabel = new GLabel("BOSS FIGHT!");
        phaseLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        phaseLabel.setColor(new Color(230, 230, 80));
        phaseLabel.setLocation((w - phaseLabel.getWidth()) / 2.0, 85);
        contents.add(phaseLabel);
        mainScreen.add(phaseLabel);

        // Spawn boss in the center
        double centerX = w / 2.0;
        double centerY = 120 + 200;
        bossRat = new BossRat(mainScreen);
        activeRats.add(bossRat);
        Hole temp = new Hole(centerX, centerY);
        temp.spawn(bossRat);

        // init movement position
        bossX = centerX;
        bossY = centerY;
        // (bossVX, bossVY already set with defaults)

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

    private void maybeSpawn(double spawnScale) {
        int visible = 0;
        for (Hole h : holes) {
            if (h.isOccupied()) visible++;
        }
        if (visible >= MAX_ACTIVE_RATS) return;

        if (rng.nextDouble() > SPAWN_CHANCE * spawnScale) return;

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

        // Auto-return to main menu after 2 seconds
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
        if (obj == null) return;

        // Back or top bar
        if (obj == backLabel || obj == topBar) {
            mainScreen.switchToWelcomeScreen();
            return;
        }

        // Power-up buttons first
        if (obj == freezeButtonRect || obj == freezeLabel) {
            handleFreezeButton();
            return;
        } else if (obj == x2PointsButtonRect || obj == x2PointsLabel) {
            handleX2PointsButton();
            return;
        } else if (obj == x2CritButtonRect || obj == x2CritLabel) {
            handleX2CritButton();
            return;
        }

        // Otherwise, whack rats / boss
        handleWhack(e.getX(), e.getY());
    }

    private void handleFreezeButton() {
        if (freezeActive || freezeCooldownRemainingMs > 0) {
            showCooldownMessage();
            return;
        }
        freezeActive = true;
        freezeRemainingMs = POWERUP_DURATION_MS;
        freezeCooldownRemainingMs = POWERUP_COOLDOWN_MS;
        updatePowerupButtonVisuals();
    }

    private void handleX2PointsButton() {
        if (doublePointsActive || doublePointsCooldownRemainingMs > 0) {
            showCooldownMessage();
            return;
        }
        doublePointsActive = true;
        doublePointsRemainingMs = POWERUP_DURATION_MS;
        doublePointsCooldownRemainingMs = POWERUP_COOLDOWN_MS;
        updatePowerupButtonVisuals();
    }

    private void handleX2CritButton() {
        if (critActive || critCooldownRemainingMs > 0) {
            showCooldownMessage();
            return;
        }
        critActive = true;
        critRemainingMs = POWERUP_DURATION_MS;
        critCooldownRemainingMs = POWERUP_COOLDOWN_MS;
        updatePowerupButtonVisuals();
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

            int dmg = BOSS_HIT_DAMAGE;
            if (critActive) dmg *= 2; // x2 crit damage on boss

            bossHp -= dmg;
            if (bossHp < 0) bossHp = 0;
            updateBossHpBar();

            mainScreen.addToScore(250); // boss hit score

            if (bossHp == 0) {
                bossRat.despawn();
                activeRats.remove(bossRat);
                bossRat = null;
                phase = Phase.FINISHED;
                timeRemainingMs = 0;
                updateTimerLabel();

                if (!scoreRecorded) {
                    scoreRecorded = true;
                    int savedScore = bossModeOnly ? mainScreen.getPreBossScore()
                                                  : mainScreen.getScore();
                    mainScreen.recordScore(savedScore);
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

                int delta = 0;
                switch (r.getType()) {
                    case NORMAL:
                        delta = 50;
                        break;
                    case BONUS:
                        delta = 100;
                        break;
                    case TRAP:
                        delta = -25;
                        break;
                    default:
                        break;
                }

                // apply x2 points logic
                if (doublePointsActive) {
                    if (delta > 0) {
                        delta *= 2;
                    } else if (delta < 0) {
                        // trap rat becomes -50 while x2 is on
                        delta = -50;
                    }
                }

                mainScreen.addToScore(delta);
                r.despawn();
                break;
            }
        }

        // misclick penalty in level 1
        if (!hit && phase == Phase.NORMAL && timeRemainingMs > 0) {
            mainScreen.addToScore(-25);
        }
    }
}
