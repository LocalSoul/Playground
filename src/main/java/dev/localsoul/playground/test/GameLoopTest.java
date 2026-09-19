package dev.localsoul.playground.test;

import dev.localsoul.playground.asset.Asset;
import dev.localsoul.playground.asset.AssetManager;
import dev.localsoul.playground.input.InputDispatcher;
import dev.localsoul.playground.loop.GameLoop;
import dev.localsoul.playground.loop.LoopListener;
import dev.localsoul.playground.math.Vector2f;
import dev.localsoul.playground.math.rect.RectTransform;
import dev.localsoul.playground.ui.Button;
import dev.localsoul.playground.ui.RootWidget;
import dev.localsoul.playground.ui.anchor.Anchor;
import dev.localsoul.playground.ui.swing.UICanvas;

import javax.swing.*;
import java.awt.*;

/**
 * Manueller Fenster-Test für das Zusammenspiel aus {@link GameLoop}, {@link InputDispatcher}
 * und {@link UICanvas} (bewusst keine JUnit-Testklasse, da ein sichtbares Swing-Fenster nötig ist).
 *
 * <p>Startet eine Button-Szene und treibt die {@link GameLoop} über einen Swing-{@code Timer}
 * mit ~60 Hz an. Die Logik läuft als fester Schritt bei 20 Hz ({@code tick}), das Rendern
 * dagegen variabel mit jedem Timer-Tick ({@code frame}: Input-Update, UI-Update, Repaint).
 * {@code catchUp} prüft das Verhalten der Schleife bei großen Frame-Lücken.</p>
 */
public class GameLoopTest {

    /**
     * Einstiegspunkt (per IDE ausführbar, analog zu den übrigen {@code *Test}-Klassen).
     */
    static void main() {

        // Eigenes GameLoopTest-Objekt für die LoopListener-Callbacks: Diese delegieren nur
        // an die Instanzmethoden tick()/catchUp(), die die echte Spiellogik später aufnehmen.
        final GameLoopTest game = new GameLoopTest();

        final AssetManager assetManager = new AssetManager();
        assetManager.loadAllAssets(
                new Asset("Button", "assets/ui/button.png", 6, 6, 6, 6)
        );

        // UI-Aufbau auf dem Swing-Event-Dispatch-Thread; der Timer setzt danach die Schleife in Gang.
        SwingUtilities.invokeLater(() -> {

            final RootWidget root = new RootWidget(1280, 720);

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

            // Dispatcher separat erzeugen und an Canvas UND Schleife geben, damit die
            // Hover-Erkennung auch ohne Mausbewegung pro Frame aktualisiert werden kann.
            final InputDispatcher input = new InputDispatcher(root);
            final UICanvas canvas = new UICanvas(root, input);
            canvas.setPreferredSize(new Dimension(1280, 720));

            final JFrame frame = new JFrame("Idle Clicker - Render Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(canvas);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // Feste Logik bei 20 Hz (50 ms), Rendern variabel über den Timer (~60 Hz).
            // Nur die frame()-Methode ist im Test verdrahtet (Input, UI-Update, Repaint);
            // tick()/catchUp() sind Stubs und warten auf die echte Spiellogik.
            final GameLoop loop = new GameLoop(System::nanoTime, 50_000_000L /* 20 Hz */, new LoopListener() {
                @Override
                public void tick(final double dt) {
                    game.tick(dt);
                }

                @Override
                public void catchUp(final long nanos) {
                    game.catchUp(nanos);
                }

                @Override
                public void frame(final float dt) {
                    input.update();
                    root.update(dt);
                    canvas.repaint();
                }
            });

            new javax.swing.Timer(16, e -> loop.advance()).start();

        });

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
