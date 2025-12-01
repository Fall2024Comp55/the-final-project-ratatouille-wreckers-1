import acm.graphics.GObject;
import acm.program.GraphicsProgram;

import javax.swing.JOptionPane;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class MainApplication extends GraphicsProgram {
    // Window
    public static final int WINDOW_WIDTH = 800;
    public static final int WINDOW_HEIGHT = 600;

    // Settings (0–100)
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

    // Leaderboard scores (top 10, highest first)
    private final List<ScoreEntry> leaderboard = new ArrayList<>();

    // Screens
    private WelcomePane welcomePane;
    private DescriptionPane descriptionPane;
    private GraphicsPane currentScreen;
    private SettingsPane settingsPane;
    private GamePane gamePane;
    private LeaderboardPane leaderboardPane;

    // Scoreboard overlay
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
    }

    @Override
    public void run() {
        System.out.println("Lets' Begin!");
        setupInteractions();

        // Init screens
        welcomePane = new WelcomePane(this);
        descriptionPane = new DescriptionPane(this);
        settingsPane = new SettingsPane(this);
        gamePane = new GamePane(this);
        leaderboardPane = new LeaderboardPane(this);

        // Scoreboard
        scoreboard = new Scoreboard(this);
        scoreboard.update(0);

        // Default screen
        switchToScreen(welcomePane);
    }

    public static void main(String[] args) {
        new MainApplication().start();
    }

    // ---------- Screen switching ----------

    public void switchToDescriptionScreen() {
        switchToScreen(descriptionPane);
    }

    public void switchToWelcomeScreen() {
        switchToScreen(welcomePane);
    }

    // Settings from main menu (or anywhere non-game)
    public void switchToSettingsScreen() {
        settingsPane.setReturnToGame(false);
        switchToScreen(settingsPane);
    }

    // Settings specifically from in-game pause menu
    public void switchToSettingsFromGame() {
        settingsPane.setReturnToGame(true);
        switchToScreen(settingsPane);
    }

    public void switchToGameScreen() {
        switchToScreen(gamePane);
    }

    public void switchToLeaderboardScreen() {
        switchToScreen(leaderboardPane);
    }

    protected void switchToScreen(GraphicsPane newScreen) {
        if (currentScreen != null) {
            currentScreen.hideContent();
        }
        newScreen.showContent();
        currentScreen = newScreen;

        // scoreboard only visible in game
        if (newScreen == gamePane) {
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

    // ---------- Settings get/set ----------

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

    // ---------- Leaderboard API ----------

    /** Record a new score with player's name, keep top 10 highest. */
    public void recordScore(int score) {
        if (score < 0) score = 0;

        String name = JOptionPane.showInputDialog(
                getGCanvas(),
                "Enter your name for score " + score + ":",
                "New High Score",
                JOptionPane.PLAIN_MESSAGE
        );
        if (name == null) {
            // cancelled
            name = "Player";
        }
        name = name.trim();
        if (name.isEmpty()) name = "Player";

        leaderboard.add(new ScoreEntry(name, score));
        leaderboard.sort((a, b) -> Integer.compare(b.score, a.score)); // descending
        if (leaderboard.size() > 10) {
            leaderboard.subList(10, leaderboard.size()).clear();
        }
        System.out.println("Recorded score: " + score + " by " + name);
    }

    /** Get copy of leaderboard, highest first. */
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
