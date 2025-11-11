import acm.graphics.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class SettingsPane extends GraphicsPane {
	public SettingsPane(MainApplication mainScreen) {
		this.mainScreen = mainScreen;
	}
	
	private class Slider {
		GRect track;
		GRect handle;
		GLabel label;
		int value;
		int x, y;
	}
	
	private Slider mainVol, sfxVol, musicVol, brightness;
	
	private GLabel title;
	private GRect saveButton, cancelButton;
    private GLabel saveLabel, cancelLabel;
    
    private Slider activeSlider = null;
    
    @Override
    public void showContent() {
        addUI();
    }

    @Override
    public void hideContent() {
    	for(GObject item : contents) {
			mainScreen.remove(item);
		}
		contents.clear();
    }
    
    private void addUI() {
    	// Title
        title = new GLabel("SETTINGS", 230, 80);
        title.setFont(new Font("Monospaced", Font.BOLD, 32));
        title.setColor(Color.BLACK);
        contents.add(title);
        mainScreen.add(title);

        // Sliders
        mainVol = createSlider("MAIN VOLUME:", 120, 150);
        sfxVol = createSlider("SFX:", 120, 230);
        musicVol = createSlider("MUSIC:", 120, 310);
        brightness = createSlider("BRIGHTNESS:", 120, 390);

        // Buttons
        cancelButton = createButton(150, 470, 150, 50, "CANCEL");
        saveButton = createButton(350, 470, 150, 50, "SAVE");
    }
    
    private Slider createSlider(String text, int x, int y) {
        Slider s = new Slider();
        s.x = x;
        s.y = y;
        s.value = 50; // default middle

        s.label = new GLabel(text, x, y);
        s.label.setFont(new Font("Monospaced", Font.BOLD, 20));
        s.label.setColor(Color.BLACK);
        contents.add(s.label);
        mainScreen.add(s.label);

        s.track = new GRect(x + 200, y - 15, 250, 8);
        s.track.setFilled(true);
        s.track.setColor(Color.DARK_GRAY);
        s.track.setFillColor(Color.GRAY);
        contents.add(s.track);
        mainScreen.add(s.track);

        s.handle = new GRect(0, 0, 15, 25);
        s.handle.setFilled(true);
        s.handle.setColor(Color.BLACK);
        updateSliderPosition(s);
        contents.add(s.handle);
        mainScreen.add(s.handle);

        return s;
    }

    private GRect createButton(int x, int y, int width, int height, String text) {
        GRect button = new GRect(x, y, width, height);
        button.setFilled(true);
        button.setFillColor(new Color(80, 80, 80));
        button.setColor(Color.BLACK);
        contents.add(button);
        mainScreen.add(button);

        GLabel label = new GLabel(text, x + 28, y + height * 0.65);
        label.setFont(new Font("Monospaced", Font.BOLD, 20));
        label.setColor(Color.BLACK);
        contents.add(label);
        mainScreen.add(label);

        if (text.equals("SAVE")) saveLabel = label;
        if (text.equals("CANCEL")) cancelLabel = label;

        return button;
    }
    
    private void updateSliderPosition(Slider s) {
        double pos = s.track.getX() + (s.value / 100.0) * (s.track.getWidth() - s.handle.getWidth());
        s.handle.setLocation(pos, s.track.getY() - 9);
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
    	 GObject obj = mainScreen.getElementAt(e.getX(), e.getY());
         if (obj == null) return;
        // Buttons
        if (obj == saveButton || obj == saveLabel) {
            System.out.println("Saved Values:");
            System.out.println("Main Volume: " + mainVol.value);
            System.out.println("SFX: " + sfxVol.value);
            System.out.println("Music: " + musicVol.value);
            System.out.println("Brightness: " + brightness.value);
            mainScreen.switchToWelcomeScreen();
        }

        if (obj == cancelButton || obj == cancelLabel) {
            mainScreen.switchToWelcomeScreen();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        GObject obj = mainScreen.getElementAt(e.getX(), e.getY());
        if (obj == null) return;

        // Determine if press is on a slider handle
        if (obj == mainVol.handle) activeSlider = mainVol;
        else if (obj == sfxVol.handle) activeSlider = sfxVol;
        else if (obj == musicVol.handle) activeSlider = musicVol;
        else if (obj == brightness.handle) activeSlider = brightness;
        else activeSlider = null;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (activeSlider != null) {
            double min = activeSlider.track.getX();
            double max = min + activeSlider.track.getWidth() - activeSlider.handle.getWidth();
            double newX = Math.max(min, Math.min(e.getX(), max));
            activeSlider.handle.setLocation(newX, activeSlider.handle.getY());

            double relative = (newX - min) / (activeSlider.track.getWidth() - activeSlider.handle.getWidth());
            activeSlider.value = (int)(relative * 100);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        activeSlider = null;
    }
}
