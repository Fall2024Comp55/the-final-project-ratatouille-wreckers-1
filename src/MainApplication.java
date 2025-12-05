import acm.graphics.GObject;
import acm.program.GraphicsProgram;

import javax.swing.JOptionPane;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class MainApplication extends GraphicsProgram {

    public static final int WINDOW_WIDTH = 800;
    public static final int WINDOW_HEIGHT = 600;

    private int mainVolume = 50;
    private int sfxVolume = 50;
    private int musicVolume = 50;
    private int brightness = 50;

    // Leaderboard entry
    public static class ScoreEntry {
        public final String name;
        public final int score;
        public ScoreEntry(String name, int score) {
            this.name = name;
            this.score = score;
        }
    }

    private final List<ScoreEntry> leaderboard = new ArrayList<>();

    // store score before boss level
    private int preBossScore = 0;

    // Screens
    private WelcomePane welcomePane;
    private DescriptionPane descriptionPane;
    private GraphicsPane currentScreen;
    private SettingsPane settingsPane;
    private GamePane gamePane; // Level 1
    private GamePane bossPane; // Boss level
    private LeaderboardPane leaderboardPane;
    private InfoPane infoPane;

    // Score overlay
    private Scoreboard scoreboard;

    public MainApplication() {
        super();
    }

    protected void setupInteractions() {
        requestFocus();
        addKeyListeners();
        addMouseListeners();
    }

    @Override
    public void init() {
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);

        // --- Apply the custom cursor (hammer) ---
        setCustomCursor();
    }

    @Override
    public void run() {
        System.out.println("Let's Begin!");
        setupInteractions();

        welcomePane = new WelcomePane(this);
        descriptionPane = new DescriptionPane(this);
        settingsPane = new SettingsPane(this);
        gamePane = new GamePane(this);          // normal level
        bossPane = new GamePane(this, true);    // boss-only level
        leaderboardPane = new LeaderboardPane(this);
        infoPane = new InfoPane(this);

        scoreboard = new Scoreboard(this);
        scoreboard.update(0);

        switchToScreen(welcomePane);
    }

    public static void main(String[] args) {
        new MainApplication().start();
    }

    // ---------- Custom Cursor ----------
    private void setCustomCursor() {
        try {
            BufferedImage img = ImageIO.read(new File("Media/hammer.png"));

            // Hotspot positioned on center of hammer's striking face
            Point hotspot = new Point(18, 28);

            Cursor c = Toolkit.getDefaultToolkit().createCustomCursor(
                    img,
                    hotspot,
                    "HammerCursor"
            );

            // Set cursor on the ACM canvas (GraphicsProgram doesn't expose setCursor directly)
            if (getGCanvas() != null) {
                getGCanvas().setCursor(c);
            }
        } catch (Exception e) {
            System.out.println("Failed to load custom cursor: " + e.getMessage());
        }
    }

    // ---------- SOUND EFFECT METHOD ----------
    public void playSound(String filePath) {
        try {
            File soundFile = new File(filePath);
            if (!soundFile.exists()) {
                System.out.println("Sound not found: " + filePath);
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);

            // --- Apply volume (SFX Volume 0–100 converted to decibels) ---
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float volumeDb = (float)(Math.log10(sfxVolume / 100.0) * 20.0);
            gain.setValue(volumeDb);

            clip.start();

        } catch (Exception e) {
            System.out.println("ERROR playing sound: " + e.getMessage());
        }
    }

    // ---------- Screen switching ----------

    public void switchToWelcomeScreen() {
        switchToScreen(welcomePane);
    }

    public void switchToDescriptionScreen() {
        switchToScreen(descriptionPane);
    }

    public void switchToGameScreen() {
        switchToScreen(gamePane);
    }

    public void switchToBossScreen() {
        switchToScreen(bossPane);
    }

    public void switchToLeaderboardScreen() {
        switchToScreen(leaderboardPane);
    }

    // Settings from non-game screens
    public void switchToSettingsScreen() {
        settingsPane.setReturnToGame(false);
        switchToScreen(settingsPane);
    }

    // Settings specifically from the in-game pause menu
    public void switchToSettingsFromGame() {
        settingsPane.setReturnToGame(true);
        switchToScreen(settingsPane);
    }

    public void switchToInfoScreen() {
        switchToScreen(infoPane);
    }

    protected void switchToScreen(GraphicsPane newScreen) {
        if (currentScreen != null) {
            currentScreen.hideContent();
        }
        newScreen.showContent();
        currentScreen = newScreen;

        // Score only in game screens
        if (newScreen == gamePane || newScreen == bossPane) {
            showScoreboard();
        } else {
            hideScoreboard();
        }
    }

    // ---------- Scoreboard ----------
    public void showScoreboard() {
        if (scoreboard != null) scoreboard.show();
    }

    public void hideScoreboard() {
        if (scoreboard != null) scoreboard.hide();
    }

    public void setScore(int score) {
        if (scoreboard != null) scoreboard.update(score);
    }

    public int getScore() {
        return (scoreboard == null) ? 0 : scoreboard.getDisplayedScore();
    }

    public void addToScore(int delta) {
        setScore(getScore() + delta);
    }

    public GObject getElementAtLocation(double x, double y) {
        return getElementAt(x, y);
    }

    // ---------- Settings ----------
    public int getMainVolume() {
        return mainVolume;
    }

    public void setMainVolume(int mainVolume) {
        this.mainVolume = clamp01(mainVolume);
    }

    public int getSfxVolume() {
        return sfxVolume;
    }

    public void setSfxVolume(int sfxVolume) {
        this.sfxVolume = clamp01(sfxVolume);
    }

    public int getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(int musicVolume) {
        this.musicVolume = clamp01(musicVolume);
    }

    public int getBrightness() {
        return brightness;
    }

    public void setBrightness(int brightness) {
        this.brightness = clamp01(brightness);
    }

    private int clamp01(int v) {
        if (v < 0) return 0;
        if (v > 100) return 100;
        return v;
    }

    // ---------- Pre-boss score ----------
    public void setPreBossScore(int score) {
        this.preBossScore = score;
    }

    public int getPreBossScore() {
        return preBossScore;
    }

    // ---------- Leaderboard ----------
    public void recordScore(int score) {
        if (score < 0) score = 0;

        String name = JOptionPane.showInputDialog(
                getGCanvas(),
                "Enter your name for score " + score + ":",
                "New High Score",
                JOptionPane.PLAIN_MESSAGE
        );
        if (name == null) name = "Player";
        name = name.trim();
        if (name.isEmpty()) name = "Player";

        leaderboard.add(new ScoreEntry(name, score));
        leaderboard.sort((a, b) -> Integer.compare(b.score, a.score));
        if (leaderboard.size() > 10) {
            leaderboard.subList(10, leaderboard.size()).clear();
        }
    }

    public List<ScoreEntry> getLeaderboardEntries() {
        return new ArrayList<>(leaderboard);
    }

    // ---------- Event forwarding ----------
    @Override
    public void mousePressed(MouseEvent e) {
        if (currentScreen != null) currentScreen.mousePressed(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (currentScreen != null) currentScreen.mouseReleased(e);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (currentScreen != null) currentScreen.mouseClicked(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (currentScreen != null) currentScreen.mouseDragged(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (currentScreen != null) currentScreen.mouseMoved(e);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (currentScreen != null) currentScreen.keyPressed(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (currentScreen != null) currentScreen.keyReleased(e);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (currentScreen != null) currentScreen.keyTyped(e);
    }
}
