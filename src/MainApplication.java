import acm.graphics.GObject;
import acm.program.GraphicsProgram;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class MainApplication extends GraphicsProgram {
    // Settings
    public static final int WINDOW_WIDTH = 800;
    public static final int WINDOW_HEIGHT = 600;

    // Stored settings values (0–100)
    private int mainVolume = 50;
    private int sfxVolume = 50;
    private int musicVolume = 50;
    private int brightness = 50;

    // List of all the full screen panes
    private WelcomePane welcomePane;
    private DescriptionPane descriptionPane;
    private GraphicsPane currentScreen;
    private SettingsPane settingsPane;
    private GamePane gamePane;       // game screen

    // Scoreboard
    private Scoreboard scoreboard;

    public MainApplication() {
        super();
    }

    protected void setupInteractions() {
        requestFocus();
        addKeyListeners();
        addMouseListeners();
    }

    public void init() {
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    public void run() {
        System.out.println("Lets' Begin!");
        setupInteractions();

        // Initialize all of your full screens
        welcomePane = new WelcomePane(this);
        descriptionPane = new DescriptionPane(this);
        settingsPane = new SettingsPane(this);
        gamePane = new GamePane(this);

        // Scoreboard
        scoreboard = new Scoreboard(this);
        scoreboard.update(0);   // Start at 0

        // The Default Pane
        switchToScreen(welcomePane);
    }

    public static void main(String[] args) {
        new MainApplication().start();
    }

    // ----- screen switches -----

    public void switchToDescriptionScreen() {
        switchToScreen(descriptionPane);
    }

    public void switchToWelcomeScreen() {
        switchToScreen(welcomePane);
    }

    public void switchToSettingsScreen() {
        switchToScreen(settingsPane);
    }

    public void switchToGameScreen() {
        switchToScreen(gamePane);
    }

    protected void switchToScreen(GraphicsPane newScreen) {
        if (currentScreen != null) {
            currentScreen.hideContent();
        }
        newScreen.showContent();
        currentScreen = newScreen;

        // show scoreboard only on game screen
        if (newScreen == gamePane) {
            showScoreboard();
        } else {
            hideScoreboard();
        }
    }

    // ----- scoreboard -----

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

    // ----- settings getters/setters -----

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

    // ----- event forwarding -----

    @Override
    public void mousePressed(MouseEvent e) {
        if (currentScreen != null) {
            currentScreen.mousePressed(e);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (currentScreen != null) {
            currentScreen.mouseReleased(e);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (currentScreen != null) {
            currentScreen.mouseClicked(e);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (currentScreen != null) {
            currentScreen.mouseDragged(e);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (currentScreen != null) {
            currentScreen.mouseMoved(e);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (currentScreen != null) {
            currentScreen.keyPressed(e);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (currentScreen != null) {
            currentScreen.keyReleased(e);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (currentScreen != null) {
            currentScreen.keyTyped(e);
        }
    }
}
