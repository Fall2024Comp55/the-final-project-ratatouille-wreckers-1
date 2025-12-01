public class NormalRat extends Ratdg {
    public NormalRat(MainApplication app) {
        super(app, RatType.NORMAL, 50, 1200);  // points value not critical, we set in GamePane
        setSpriteFromFile("rat_normal.png", -30, -40, 60, 60);
    }
}