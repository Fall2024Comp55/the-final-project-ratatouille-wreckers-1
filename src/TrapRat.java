import java.awt.Color;

public class TrapRat extends Ratdg {
    public TrapRat(MainApplication app) {
        super(app, RatType.TRAP, -15, 1500); // longer, negative impact
        setSpriteFromFile("rat_trap.png", -28, -38, 56, 56);
    }
}
