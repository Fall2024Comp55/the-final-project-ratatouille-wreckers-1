public class TrapRat extends Ratdg {
    public TrapRat(MainApplication app) {
        super(app, RatType.TRAP, -15, 1500);
        setSpriteFromFile("rat_trap.png", -28, -38, 56, 56);
    }
}
