import acm.graphics.GObject;
import acm.program.*;


import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class MainApplication extends GraphicsProgram {
	// Settings
	public static final int WINDOW_WIDTH = 800;
	public static final int WINDOW_HEIGHT = 600;

	// List of all the full screen panes
	private WelcomePane welcomePane;
	private DescriptionPane descriptionPane;
	private GraphicsPane currentScreen;

	// --- Add this line ---
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

		// --- Add this part ---
		scoreboard = new Scoreboard(this);
		scoreboard.update(0);   // Start at 0
		// scoreboard.show();    // Uncomment if you want it visible from the start

		// The Default Pane
		switchToScreen(welcomePane);
	}

	public static void main(String[] args) {
		new MainApplication().start();
	}

	// a switch to screen for every full screen 
	public void switchToDescriptionScreen() {
		switchToScreen(descriptionPane);
	}

	public void switchToWelcomeScreen() {
		switchToScreen(welcomePane);
	}

	protected void switchToScreen(GraphicsPane newScreen) {
		if (currentScreen != null) {
			currentScreen.hideContent();
		}
		newScreen.showContent();
		currentScreen = newScreen;

		// show scoreboard only on certain screens ---
		if (newScreen == descriptionPane) {
			showScoreboard();
		} else {
			hideScoreboard();
		}
	}
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