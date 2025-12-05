import acm.graphics.GLabel;
import acm.graphics.GObject;
import acm.graphics.GRect;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;

public class SettingsPane extends GraphicsPane {
    public SettingsPane(MainApplication mainScreen) {
        this.mainScreen = mainScreen;
    }

    private boolean returnToGame = false;

    public void setReturnToGame(boolean value) {
        this.returnToGame = value;
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
    private GRect panelBg;

    @Override
    public void showContent() {
        addUI();
    }

    @Override
    public void hideContent() {
        for (GObject item : contents) {
            mainScreen.remove(item);
        }
        contents.clear();
    }

    private void addUI() {
        double w = mainScreen.getWidth();
        double h = mainScreen.getHeight();

        panelBg = new GRect(120, 70, w - 240, h - 140);
        panelBg.setFilled(true);
        panelBg.setFillColor(new Color(240, 240, 240));
        panelBg.setColor(new Color(80, 80, 80));
        contents.add(panelBg);
        mainScreen.add(panelBg);

        title = new GLabel("SETTINGS");
        title.setFont(new Font("Monospaced", Font.BOLD, 30));
        title.setColor(new Color(40, 40, 40));
        title.setLocation(
                panelBg.getX() + (panelBg.getWidth() - title.getWidth()) / 2.0,
                panelBg.getY() + 50
        );
        contents.add(title);
        mainScreen.add(title);

        int baseX = (int) panelBg.getX() + 40;
        int row1Y = (int) panelBg.getY() + 110;
        int rowGap = 70;

        mainVol = createSlider("MAIN VOLUME", baseX, row1Y, mainScreen.getMainVolume());
        sfxVol = createSlider("SFX", baseX, row1Y + rowGap, mainScreen.getSfxVolume());
        musicVol = createSlider("MUSIC", baseX, row1Y + 2 * rowGap, mainScreen.getMusicVolume());
        brightness = createSlider("BRIGHTNESS", baseX, row1Y + 3 * rowGap, mainScreen.getBrightness());

        int btnY = (int) (panelBg.getY() + panelBg.getHeight() - 70);
        cancelButton = createButton(baseX + 30, btnY, 150, 45, "CANCEL");
        saveButton = createButton(baseX + 260, btnY, 150, 45, "SAVE");
    }

    private Slider createSlider(String text, int x, int y, int initialValue) {
        Slider s = new Slider();
        s.x = x;
        s.y = y;
        s.value = initialValue;

        s.label = new GLabel(text);
        s.label.setFont(new Font("Monospaced", Font.BOLD, 18));
        s.label.setColor(new Color(30, 30, 30));
        s.label.setLocation(x, y);
        contents.add(s.label);
        mainScreen.add(s.label);

        s.track = new GRect(x + 200, y - 18, 260, 10);
        s.track.setFilled(true);
        s.track.setFillColor(new Color(200, 200, 200));
        s.track.setColor(new Color(100, 100, 100));
        contents.add(s.track);
        mainScreen.add(s.track);

        s.handle = new GRect(0, 0, 18, 28);
        s.handle.setFilled(true);
        s.handle.setFillColor(new Color(80, 80, 80));
        s.handle.setColor(Color.BLACK);
        updateSliderPosition(s);
        contents.add(s.handle);
        mainScreen.add(s.handle);

        return s;
    }

    private GRect createButton(int x, int y, int width, int height, String text) {
        GRect button = new GRect(x, y, width, height);
        button.setFilled(true);
        button.setFillColor(new Color(60, 60, 60));
        button.setColor(Color.BLACK);
        contents.add(button);
        mainScreen.add(button);

        GLabel label = new GLabel(text);
        label.setFont(new Font("Monospaced", Font.BOLD, 18));
        label.setColor(Color.WHITE);
        label.setLocation(
                x + (width - label.getWidth()) / 2.0,
                y + height * 0.65
        );
        contents.add(label);
        mainScreen.add(label);

        if (text.equals("SAVE")) saveLabel = label;
        if (text.equals("CANCEL")) cancelLabel = label;

        return button;
    }

    private void updateSliderPosition(Slider s) {
        double min = s.track.getX();
        double range = s.track.getWidth() - s.handle.getWidth();
        double pos = min + (s.value / 100.0) * range;
        s.handle.setLocation(pos, s.track.getY() - 9);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        GObject obj = mainScreen.getElementAt(e.getX(), e.getY());
        if (obj == null) return;

        if (obj == saveButton || obj == saveLabel) {
            mainScreen.setMainVolume(mainVol.value);
            mainScreen.setSfxVolume(sfxVol.value);
            mainScreen.setMusicVolume(musicVol.value);
            mainScreen.setBrightness(brightness.value);

            if (returnToGame) {
            	mainScreen.playSound("Media/hammer_hit.wav");
                mainScreen.switchToGameScreen();
            } else {
            	mainScreen.playSound("Media/hammer_hit.wav");
                mainScreen.switchToWelcomeScreen();
            }
        } else if (obj == cancelButton || obj == cancelLabel || obj == panelBg) {
            if (returnToGame) {
            	mainScreen.playSound("Media/hammer_hit.wav");
                mainScreen.switchToGameScreen();
            } else {
            	mainScreen.playSound("Media/hammer_hit.wav");
                mainScreen.switchToWelcomeScreen();
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        GObject obj = mainScreen.getElementAt(e.getX(), e.getY());
        if (obj == null) return;

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

            double relative = (newX - min) /
                    (activeSlider.track.getWidth() - activeSlider.handle.getWidth());
            activeSlider.value = (int) (relative * 100);

            // --- Apply volume changes live ---
            if (activeSlider == sfxVol) {
                mainScreen.setSfxVolume(activeSlider.value);
            }
            if (activeSlider == mainVol) {
                mainScreen.setMainVolume(activeSlider.value);
            }
            if (activeSlider == musicVol) {
                mainScreen.setMusicVolume(activeSlider.value);
                mainScreen.updateMusicVolume();   // <--- add this
            }

            if (activeSlider == brightness) {
                mainScreen.setBrightness(activeSlider.value);
            }
        }
    }


    @Override
    public void mouseReleased(MouseEvent e) {
        activeSlider = null;
    }
}
