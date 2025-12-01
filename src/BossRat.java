public class BossRat extends Ratdg {
    public BossRat(MainApplication app) {
        // BIG points, long life (we control real timer in GamePane)
        super(app, RatType.BOSS, 250, 30000);
        // bigger sprite (or placeholder gray circle if image missing)
        setSpriteFromFile("rat_boss.png", -50, -60, 100, 100);
    }
}
