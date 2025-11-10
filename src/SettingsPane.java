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
    
    
	
}
