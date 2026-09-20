package dev.localsoul.playground.ui.swing;

import dev.localsoul.playground.core.input.InputDispatcher;
import dev.localsoul.playground.core.loop.GameLoop;
import dev.localsoul.playground.core.loop.LoopListener;
import dev.localsoul.playground.ui.RootWidget;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * Treibt eine {@link RootWidget}-Szene in einem Swing-Fenster über einen {@link GameLoop} an.
 *
 * <p>Kapselt die Boilerplate, die die manuellen Fenster-Tests (z. B.
 * {@code dev.localsoul.playground.test.ButtonTest}) sonst einzeln wiederholen:</p>
 * <ul>
 *     <li>Erzeugung von {@link RootWidget}, {@link InputDispatcher} und {@link UICanvas} in den
 *     übergebenen Abmessungen,</li>
 *     <li>ein {@link JFrame} samt Canvas und Exit-on-Close,</li>
 *     <li>die {@link GameLoop} mit festem Logik-Schritt (Standard 20 Hz) und variablem
 *     Frame-Takt,</li>
 *     <li>einen Swing-{@code Timer} mit ~60 Hz, der die Schleife pro Frame anstößt.</li>
 * </ul>
 *
 * <p>Der Standard-Frame-Schritt der Schleife (Eingabe-Update, Hierarchie-Update, Repaint) läuft
 * ohne Zutun des Aufrufers. Über die {@link Logic}-Callbacks kann der Test eigene Logik in die
 * drei Zeit-Ebenen des {@link GameLoop} einhängen (siehe {@link LoopListener}). Die Szene wird
 * zwischen Konstruktion und {@link #start()} über {@link #root()} aufgebaut; {@code start()}
 * zeigt das Fenster und startet den Timer auf dem Event-Dispatch-Thread.</p>
 */
public final class SwingLoopDriver {

    /**
     * Standard-Schrittlänge der festen Spiellogik (50 ms = 20 Hz).
     */
    private static final long DEFAULT_TICK_NANOS = 50_000_000L;
    /**
     * Standard-Takt des Swing-{@code Timer}s (16 ms ≈ 60 Hz).
     */
    private static final int DEFAULT_FRAME_MILLIS = 16;

    /**
     * Einhängepunkte für die Logik eines manuellen Tests. Alle Methoden sind No-op-Defaults,
     * sodass ein Test nur die Ebenen überschreiben muss, die er nutzt. Die Semantik der
     * drei Methoden entspricht exakt der des {@link LoopListener}.
     */
    public interface Logic {
        /**
         * Spiellogik in einem festen Logik-Schritt (standardmäßig 20 Hz).
         *
         * @param dt die feste Schrittlänge in Sekunden
         */
        default void tick(final double dt) {
        }

        /**
         * Grobes Nachsimulieren einer großen Zeitlücke.
         *
         * @param nanos die grob nachzusimulierende Zeit in Nanosekunden
         */
        default void catchUp(final long nanos) {
        }

        /**
         * Zusätzliche Logik pro Frame, nach dem Standard-Frame-Step des Treibers
         * (Eingabe, Hierarchie-Update, Repaint).
         *
         * @param dt die vergangene Frame-Zeit in Sekunden als {@code float}
         */
        default void frame(final float dt) {
        }
    }

    /**
     * Wurzel der zu treibenden Szene; über {@link #root()} für den Szenenaufbau erreichbar.
     */
    private final RootWidget root;
    /**
     * Verdrahtet die Maus-Events des Canvases mit der Hierarchie.
     */
    private final InputDispatcher input;
    /**
     * Zeichnet die Szene und liefert die Swing-Fensterfläche.
     */
    private final UICanvas canvas;
    /**
     * Feste Takt-Schleife, die von einem Swing-{@code Timer} angetrieben wird.
     */
    private final GameLoop loop;
    /**
     * Swing-{@code Timer}, der die Schleife mit ~60 Hz anstößt.
     */
    private final Timer timer;
    /**
     * Fenstertitel, der beim Aufbau des {@link JFrame}s in {@link #start()} verwendet wird.
     */
    private final String title;

    /**
     * Erzeugt einen Treiber mit Standard-Takten (20 Hz Logik, ~60 Hz Frame).
     *
     * @param title  Fenstertitel; darf nicht {@code null} sein
     * @param width  Breite der Szene in Pixeln
     * @param height Höhe der Szene in Pixeln
     */
    public SwingLoopDriver(final @Nonnull String title, final int width, final int height) {
        this(title, width, height, new Logic() {
        });
    }

    /**
     * Erzeugt einen Treiber mit Standard-Takten und übergebenen Logik-Callbacks.
     *
     * @param title  Fenstertitel; darf nicht {@code null} sein
     * @param width  Breite der Szene in Pixeln
     * @param height Höhe der Szene in Pixeln
     * @param logic  Callbacks für die drei Zeit-Ebenen; darf nicht {@code null} sein
     */
    public SwingLoopDriver(final @Nonnull String title, final int width, final int height, final @Nonnull Logic logic) {
        this(title, width, height, DEFAULT_TICK_NANOS, DEFAULT_FRAME_MILLIS, logic);
    }

    /**
     * Erzeugt einen Treiber mit expliziten Takten und Logik-Callbacks.
     *
     * <p>Da die Szene erst nach dem Konstruktor über {@link #root()} aufgebaut wird, dürfen die
     * Widgets hier ohne Sorge um den Event-Dispatch-Thread erzeugt werden; das Fenster öffnet
     * sich erst bei {@link #start()}.</p>
     *
     * @param title       Fenstertitel; darf nicht {@code null} sein
     * @param width       Breite der Szene in Pixeln
     * @param height      Höhe der Szene in Pixeln
     * @param tickNanos   Länge eines Logik-Schritts in ns (größer 0)
     * @param frameMillis Takt des Swing-{@code Timer}s in ms (größer 0)
     * @param logic       Callbacks für die drei Zeit-Ebenen; darf nicht {@code null} sein
     */
    public SwingLoopDriver(final @Nonnull String title, final int width, final int height,
                           final long tickNanos, final int frameMillis, final @Nonnull Logic logic) {
        this.title = title;

        root = new RootWidget(width, height);
        input = new InputDispatcher(root);
        canvas = new UICanvas(root, input);
        canvas.setPreferredSize(new Dimension(width, height));

        Objects.requireNonNull(logic, "Logic must not be null");
        if(tickNanos <= 0){
            throw new IllegalArgumentException("tickNanos must be > 0");
        }

        if(frameMillis <= 0){
            throw new IllegalArgumentException("frameMillis must be > 0");
        }

        // Der Treiber übernimmt den Standard-Frame-Schritt (Eingabe, Hierarchie-Update,
        // Repaint); trotzdem bleibt der gesamte LoopListener sichtbar, damit tick/catchUp
        // an die Logik des Aufrufers durchgereicht werden.
        loop = new GameLoop(System::nanoTime, tickNanos, new LoopListener() {
            @Override
            public void tick(final double dt) {
                logic.tick(dt);
            }

            @Override
            public void catchUp(final long nanos) {
                logic.catchUp(nanos);
            }

            @Override
            public void frame(final float dt) {
                input.update();
                root.update(dt);
                canvas.repaint();
                logic.frame(dt);
            }
        });

        timer = new Timer(frameMillis, e -> loop.advance());
    }

    /**
     * Zeigt das Fenster und startet den Frame-Takt auf dem Event-Dispatch-Thread.
     *
     * <p>Vorher die Szene über {@link #root()} aufbauen. Nach dem Aufruf läuft die Schleife,
     * bis das Fenster geschlossen wird.</p>
     */
    public void start() {
        SwingUtilities.invokeLater(() -> {
            final JFrame window = new JFrame(title);
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.add(canvas);
            window.pack();
            window.setLocationRelativeTo(null);
            window.setVisible(true);

            timer.start();
        });
    }

    /**
     * Die Wurzel der Szene, auf der der Aufrufer seine Widgets aufbaut.
     *
     * @return die {@link RootWidget}-Wurzel dieses Treibers
     */
    public @Nonnull RootWidget root() {
        return root;
    }

    /**
     * Das Canvas, das die Szene zeichnet (z. B. für direkte Manipulationen am Swing-Component).
     *
     * @return der {@link UICanvas} dieses Treibers
     */
    public @Nonnull UICanvas canvas() {
        return canvas;
    }
}
