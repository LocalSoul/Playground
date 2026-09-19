package dev.localsoul.playground.test;

import dev.localsoul.playground.core.asset.Asset;
import dev.localsoul.playground.core.asset.AssetManager;
import dev.localsoul.playground.core.loop.GameLoop;
import dev.localsoul.playground.core.math.Vector2f;
import dev.localsoul.playground.core.math.rect.RectTransform;
import dev.localsoul.playground.ui.Button;
import dev.localsoul.playground.ui.RootWidget;
import dev.localsoul.playground.ui.anchor.Anchor;
import dev.localsoul.playground.ui.swing.SwingLoopDriver;

/**
 * Manueller Fenster-Test für das Zusammenspiel aus {@link GameLoop} und {@link SwingLoopDriver}
 * (bewusst keine JUnit-Testklasse, da ein sichtbares Swing-Fenster nötig ist).
 *
 * <p>Startet eine Button-Szene und treibt sie über den {@link SwingLoopDriver} mit fixem 20-Hz-Schritt
 * ({@code tick}) und ~60-Hz-Repaint ({@code frame}). {@code catchUp} prüft das Verhalten der Schleife
 * bei großen Frame-Lücken.</p>
 */
public class GameLoopTest {
    /**
     * Einstiegspunkt (per IDE ausführbar, analog zu den übrigen {@code *Test}-Klassen).
     */
    static void main() {

        // Eigenes GameLoopTest-Objekt für die Logik-Callbacks: Diese delegieren nur an die
        // Instanzmethoden tick()/catchUp(), die die echte Spiellogik später aufnehmen.
        final GameLoopTest game = new GameLoopTest();

        final AssetManager assetManager = new AssetManager();
        assetManager.loadAllAssets(
                new Asset("Button", "assets/ui/button.png", 6, 6, 6, 6)
        );

        final SwingLoopDriver driver = new SwingLoopDriver("Idle Clicker - Render Test", 1280, 720, new SwingLoopDriver.Logic() {
            @Override
            public void tick(final double dt) {
                game.tick(dt);
            }

            @Override
            public void catchUp(final long nanos) {
                game.catchUp(nanos);
            }
        });

        final RootWidget root = driver.root();

        final RectTransform testTransform = new RectTransform(
                Anchor.CENTER,
                new Vector2f(-32, -16), //offsetMin: halbe Breite/Höhe nach links/oben da Center-Anker
                new Vector2f(0, 0), //offsetMax: gleich bei nicht-stretch
                new Vector2f(128, 64) //size
        );

        // Bewusst deutlich längerer Text als der Button (128 px breit), um das
        // Überschreiten der Ränder und das Text-Clipping in Button.drawSelf zu prüfen.
        final Button button = new Button(testTransform, assetManager.getNineSlice("Button"), "test Button das ist ein sehr langer text um zu gucken ob clip funktioniert");

        // Eingabe-Test: Der UICanvas verdrahtet die Maus-Events über den InputDispatcher
        // mit der Widget-Hierarchie. Die Callbacks landen hier nur auf stdout, um das
        // Zusammenspiel von Hover (Enter/Exit) und Klick sichtbar zu prüfen.
        button.setClickCallback(() -> System.out.println("Clicked!"));
        button.setHoverEnterCallback(() -> System.out.println("Hover Entered!"));
        button.setHoverExitCallback(() -> System.out.println("Hover Exited!"));

        root.addChild(button);

        driver.start();
    }

    /**
     * Grobes Nachsimulieren großer Zeitlücken (nur bei Frames über 250 ms aufgerufen).
     * Aktuell ein Stub – hier müsste die Simulation die übergebene Zeit grob nachholen,
     * damit simulierte und echte Zeit bei langen Frames (z. B. Fenster ziehen) nicht driften.
     *
     * @param nanos die nachzusimulierende Zeit in Nanosekunden
     */
    private void catchUp(final long nanos) {

    }

    /**
     * Spiellogik im festen 20-Hz-Schritt. Aktuell ein Stub – hier entsteht später die
     * deterministische Gamestate-Update-Logik. {@code dt} ist konstant (0.05 s).
     *
     * @param dt die feste Schrittlänge in Sekunden
     */
    private void tick(final double dt) {

    }
}
