public class BonusRat extends Ratdg {
    public BonusRat(MainApplication app) {
        super(app, RatType.BONUS, 25, 900);
        setSpriteFromFile("rat_bonus.png", -32, -42, 64, 64);
    }
}
