import java.awt.event.MouseEvent;

import acm.graphics.GImage;
import acm.graphics.GObject;

public class WelcomePane extends GraphicsPane{
	
	//every full screen constructor 
	public WelcomePane(MainApplication mainScreen) {
		this.mainScreen = mainScreen;
	}
	
	@Override
	public void showContent() {
		addPicture();
		addDescriptionButton();
		addStartButton();
		addLeaderboardsButton();
		addSettingsButton();
	}

	//keep the same and add more if needed
	@Override
	public void hideContent() {
		for(GObject item : contents) {
			mainScreen.remove(item);
		}
		contents.clear();
	}
	
	private void addPicture(){
		GImage startImage = new GImage("start.png", 200, 100);
		startImage.scale(0.5, 0.5);
		startImage.setLocation((mainScreen.getWidth() - startImage.getWidth())/ 2, 70);
		
		//have these two for everything you want to show on screen
		contents.add(startImage);
		mainScreen.add(startImage);
	}
	
	private void addStartButton() {
		GImage startButton = new GImage("StartButton.png", 300, 300);
		startButton.scale(0.15, 0.15);
		startButton.setLocation((mainScreen.getWidth() - startButton.getWidth())/ 2, 500);
		
		contents.add(startButton);
		mainScreen.add(startButton);
	}
	
	private void addLeaderboardsButton() {
		
	}
	
	private void addSettingsButton() {
		
	}
	
	private void addDescriptionButton() {
		GImage moreButton = new GImage("more.jpeg", 200, 400);
		moreButton.scale(0.3, 0.3);
		moreButton.setLocation((mainScreen.getWidth() - moreButton.getWidth())/ 2, 400);
		
		contents.add(moreButton);
		mainScreen.add(moreButton);

	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if (mainScreen.getElementAtLocation(e.getX(), e.getY()) == contents.get(1)) {
			mainScreen.switchToDescriptionScreen();
		}
		if (mainScreen.getElementAtLocation(e.getX(), e.getY()) == contents.get(2)) {
			System.out.println("Start Page");
		}
	}

}
