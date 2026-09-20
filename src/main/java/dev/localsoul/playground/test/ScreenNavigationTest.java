package dev.localsoul.playground.test;

import dev.localsoul.playground.core.asset.Asset;
import dev.localsoul.playground.core.asset.AssetManager;
import dev.localsoul.playground.core.math.Vector2f;
import dev.localsoul.playground.core.math.rect.Rect;
import dev.localsoul.playground.core.math.rect.RectTransform;
import dev.localsoul.playground.ui.Button;
import dev.localsoul.playground.ui.RootWidget;
import dev.localsoul.playground.ui.Widget;
import dev.localsoul.playground.ui.anchor.Anchor;
import dev.localsoul.playground.ui.screen.Navigator;
import dev.localsoul.playground.ui.screen.Screen;
import dev.localsoul.playground.ui.screen.ScreenHost;
import dev.localsoul.playground.ui.slice.NineSlice;
import dev.localsoul.playground.ui.swing.SwingLoopDriver;

import java.awt.*;

/**
 * Sichtprobe für den Screen-Wechsel mit dauerhaften Leisten:
 *
 * <pre>
 * ┌───────────────────────────────┐
 * │ Ressourcenleiste (Gold)       │  bleibt beim Wechsel stehen und zählt weiter
 * ├───────────────────────────────┤
 * │                               │
 * │   ScreenHost: Quests /        │  wechselt
 * │   Charakter                   │
 * │                               │
 * ├───────────────────────────────┤
 * │ [Quests] [Charakter]          │  Navigationsleiste, aktiver Tab weiß, die anderen dunkel
 * └───────────────────────────────┘
 * </pre>
 *
 * <p>Jeder Screen wird einmal gebaut und gecacht. Zum Prüfen: Auf einem Screen den Button ein paar
 * Mal klicken, zum anderen wechseln und zurückkehren. Der Klickzähler bleibt erhalten, während
 * {@code onShow} mitzählt und die "sichtbar seit"-Zeit neu beginnt. Das Gold in der oberen Leiste
 * wächst durchgehend weiter, weil es aus der Spiellogik ({@code tick}) kommt und nicht von einem
 * Screen abhängt.</p>
 */
public class ScreenNavigationTest {

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    private static final float RESOURCE_BAR_HEIGHT = 56f;
    private static final float NAV_BAR_HEIGHT = 72f;

    private static final Color BAR_COLOR = new Color(30, 34, 44);
    private static final Color GOLD_COLOR = new Color(255, 205, 90);
    // Die Button-Textur ist orange: Weiß hebt den aktiven Tab ab, ein dunkles Braun bleibt bei den anderen gut lesbar.
    private static final Color TAB_ACTIVE = Color.WHITE;
    private static final Color TAB_INACTIVE = new Color(75, 40, 15);

    static void main() {

        final AssetManager assets = new AssetManager();
        assets.loadAllAssets(new Asset("Button", "assets/ui/button.png", 6, 6, 6, 6));

        final Wallet wallet = new Wallet();

        // Die Spiellogik läuft im festen 20-Hz-Tick, unabhängig von den Frames und vom Screen.
        final SwingLoopDriver driver = new SwingLoopDriver("Idle Clicker - Screen Test", WIDTH, HEIGHT,
                new SwingLoopDriver.Logic() {
                    @Override
                    public void tick(final double dt) {
                        wallet.gold++;
                    }
                });

        buildScene(driver.root(), assets, wallet).navigate(Tab.QUESTS);

        driver.start();
    }

    /**
     * Baut die gesamte Szene an die übergebene Wurzel. Bewusst getrennt vom Fensterstart, damit
     * sie sich auch ohne Fenster aufbauen und prüfen lässt.
     *
     * @return der Navigator, mit dem zwischen den Screens gewechselt wird
     */
    static Navigator<Tab> buildScene(final RootWidget root, final AssetManager assets, final Wallet wallet) {

        // Der Inhaltsbereich liegt zwischen den beiden Leisten (siehe Anchor.STRETCH).
        final ScreenHost host = new ScreenHost(new RectTransform(
                Anchor.STRETCH,
                new Vector2f(0, RESOURCE_BAR_HEIGHT),  // Abstand oben
                new Vector2f(0, -NAV_BAR_HEIGHT),      // Abstand unten (negativ = nach innen)
                new Vector2f()));

        final Navigator<Tab> navigator = new Navigator<>(host);
        for (final Tab tab : Tab.values()) {
            navigator.register(tab, new DemoScreen(tab, assets.getNineSlice("Button")));
        }

        // Der Host steht zuerst, die dauerhaften Leisten danach; die Bereiche überlappen sich nicht.
        root.addChild(host);
        root.addChild(new ResourceBar(wallet));
        root.addChild(new NavBar(navigator, assets.getNineSlice("Button")));

        return navigator;
    }

    /**
     * Die Bezeichner der Screens.
     */
    enum Tab {
        QUESTS("Quests", new Color(46, 92, 72)),
        CHARACTER("Charakter", new Color(82, 64, 122));

        final String label;
        final Color background;

        Tab(final String label, final Color background) {
            this.label = label;
            this.background = background;
        }
    }

    /**
     * Das simulierte Spielmodell: nur ein Goldzähler, den die Spiellogik erhöht.
     */
    static final class Wallet {
        long gold;
    }

    /**
     * Dauerhafte obere Leiste: zeigt das Gold, gelesen aus dem Modell.
     */
    private static final class ResourceBar extends Widget {

        private final Wallet wallet;

        ResourceBar(final Wallet wallet) {
            super(new RectTransform(Anchor.TOP_STRETCH, new Vector2f(), new Vector2f(),
                    new Vector2f(0, RESOURCE_BAR_HEIGHT)));
            this.wallet = wallet;
        }

        @Override
        protected void drawSelf(final Graphics2D g, final Rect bounds) {
            g.setColor(BAR_COLOR);
            g.fillRect(0, 0, (int) bounds.width(), (int) bounds.height());

            g.setColor(GOLD_COLOR);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
            final FontMetrics fm = g.getFontMetrics();
            g.drawString("Gold: " + wallet.gold, 20, (bounds.height() - fm.getHeight()) / 2f + fm.getAscent());
        }
    }

    /**
     * Dauerhafte untere Leiste mit einem Button pro Tab.
     */
    private static final class NavBar extends Widget {

        NavBar(final Navigator<Tab> navigator, final NineSlice buttonSlice) {
            super(new RectTransform(Anchor.BOTTOM_STRETCH, new Vector2f(0, -NAV_BAR_HEIGHT), new Vector2f(),
                    new Vector2f(0, NAV_BAR_HEIGHT)));

            float x = 16;
            for (final Tab tab : Tab.values()) {
                addChild(new TabButton(new RectTransform(Anchor.TOP_LEFT, new Vector2f(x, 12), new Vector2f(),
                        new Vector2f(200, 48)), buttonSlice, tab, navigator));
                x += 212;
            }
        }

        @Override
        protected void drawSelf(final Graphics2D g, final Rect bounds) {
            g.setColor(BAR_COLOR);
            g.fillRect(0, 0, (int) bounds.width(), (int) bounds.height());
        }
    }

    /**
     * Button, der zu seinem Tab navigiert und seinen aktiven Zustand jedes Frame vom Navigator abliest.
     */
    private static final class TabButton extends Button {

        private final Tab tab;
        private final Navigator<Tab> navigator;

        TabButton(final RectTransform transform, final NineSlice slice, final Tab tab,
                  final Navigator<Tab> navigator) {
            super(transform, slice, tab.label);
            this.tab = tab;
            this.navigator = navigator;
            setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
            setClickCallback(() -> navigator.navigate(tab));
        }

        @Override
        protected void updateSelf(final float dt) {
            setTextColor(navigator.isCurrent(tab) ? TAB_ACTIVE : TAB_INACTIVE);
        }
    }

    /**
     * Ein Platzhalter-Screen, der seinen Lebenszyklus und den erhaltenen Zustand sichtbar macht.
     */
    private static final class DemoScreen extends Screen {

        private final Tab tab;
        private final Button clickButton;
        private int showCount;
        private float visibleSeconds;
        private int clicks;

        DemoScreen(final Tab tab, final NineSlice buttonSlice) {
            this.tab = tab;

            clickButton = new Button(new RectTransform(Anchor.CENTER, new Vector2f(-110, -24), new Vector2f(),
                    new Vector2f(220, 48)), buttonSlice, "Klick mich");
            clickButton.setClickCallback(() -> {
                clicks++;
                clickButton.setText("Klicks: " + clicks);
            });
            addChild(clickButton);
        }

        @Override
        protected void onShow() {
            showCount++;
            visibleSeconds = 0;
        }

        @Override
        protected void updateSelf(final float dt) {
            visibleSeconds += dt;
        }

        @Override
        protected void drawSelf(final Graphics2D g, final Rect bounds) {
            g.setColor(tab.background);
            g.fillRect(0, 0, (int) bounds.width(), (int) bounds.height());

            g.setColor(Color.WHITE);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 44));
            g.drawString(tab.label + "-Screen", 40, 80);

            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
            g.drawString("onShow() bisher: " + showCount + "x", 40, 130);
            g.drawString(String.format("sichtbar seit: %.1f s (beginnt bei jedem Wechsel neu)", visibleSeconds), 40, 158);
            g.drawString("Klicks bleiben beim Wechsel erhalten, der Screen wird nur ausgehängt und nicht zerstört.",
                    40, 186);
        }
    }
}
