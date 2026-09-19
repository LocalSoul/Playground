package dev.localsoul.playground.loop;

import java.util.function.LongSupplier;

/**
 * Eine feste Takt-Schleife („fixed timestep") mit Schutz vor dem Spiral-of-Death-Effekt.
 *
 * <p>Die Schleife wird nicht selbst getrieben, sondern erwartet, dass ein externer Treiber
 * (z. B. ein Swing-{@code Timer}) einmal pro Frame {@link #advance()} aufruft. Die Spiellogik
 * läuft dabei niemals direkt in Echtzeit, sondern in festen, konfigurierbaren Schritten
 * ({@code tickNanos}); zurückliegende Zeit wird im {@code accumulator} gesammelt und so
 * lange in komplette Steps umgesetzt, wie genug Zeit angesammelt wurde. Das Rendern/
 * Updaten des Frames passiert dagegen variabel pro {@code advance()}-Aufruf.</p>
 *
 * <p>Wird ein Frame ungewöhnlich lange blockiert (z. B. Fenster ziehen, Debugger-Pause),
 * wäre es gefährlich, die gesamte Lücke in Einzelschritten nachzuholen: Das würde mehrere
 * Sekunden Berechnung im laufenden Frame bedeuten und die Schleife weiter verlangsamen.
 * Deshalb wird jedes Frame auf {@link #MAX_FRAME_NANOS} begrenzt; die darüber hinaus
 * liegende Zeit wird nicht verworfen, sondern über {@link LoopListener#catchUp(long)}
 * an den Listener gereicht, der sie grob (z. B. über eine reine Zeitrechnung) nachsimulieren
 * kann.</p>
 *
 * <p>Eingabepunkte für den Listener (siehe {@link LoopListener}):</p>
 * <ul>
 *     <li>{@link LoopListener#tick(double)} – Spiellogik in festen Schritten.</li>
 *     <li>{@link LoopListener#catchUp(long)} – grobes Nachsimulieren großer Lücken.</li>
 *     <li>{@link LoopListener#frame(float)} – variabler Per-Frame-Update (Input, UI, Repaint).</li>
 * </ul>
 */
public final class GameLoop {

    /**
     * Obergrenze für die Zeitspanne, die pro {@code advance()}-Aufruf in Einzelschritten
     * nachgespielt wird (250 ms). Alles darüber wird an {@code catchUp} weitergegeben,
     * damit ein einzelner, langer Frame die Schleife nicht zum Stillstand bringt.
     */
    private static final long MAX_FRAME_NANOS = 250_000_000L; // 250 ms

    /**
     * Liefert die aktuelle Zeit (in ns) – in der Praxis {@code System::nanoTime}.
     */
    private final LongSupplier clock;
    /**
     * Länge eines Logik-Schritts in ns (z. B. 50 ms für eine 20-Hz-Logik).
     */
    private final long tickNanos;
    /**
     * Länge eines Logik-Schritts in Sekunden – vorberechnet, statt pro Tick zu teilen.
     */
    private final double tickSeconds;
    /**
     * Empfänger der Tick-, CatchUp- und Frame-Aufrufe.
     */
    private final LoopListener listener;
    /**
     * Zeitstempel des letzten {@code advance()}-Aufrufs (zur Delta-Berechnung).
     */
    private long last;
    /**
     * Sammelt überschüssige Zeit, die noch nicht in komplette Logik-Schritte umgesetzt
     * wurde. Reste bleiben hier liegen und gehen nicht verloren (Drift-Kompensation).
     */
    private long accumulator;
    /**
     * Ob der erste {@code advance()}-Aufruf bereits die Zeitbasis gesetzt hat.
     */
    private boolean started;

    /**
     * Erzeugt eine feste Takt-Schleife.
     *
     * <p>Der {@code clock} muss monoton steigende Nanosekunden liefern (empfohlen:
     * {@code System::nanoTime}), da alle Deltas auf Differenzen beruhen.</p>
     *
     * @param clock     Zeitquelle in ns; darf nicht {@code null} sein
     * @param tickNanos Länge eines Logik-Schritts in ns (größer 0)
     * @param listener  Empfänger der Logik-/Frame-Aufrufe; darf nicht {@code null} sein
     */
    public GameLoop(final LongSupplier clock, final long tickNanos, final LoopListener listener) {
        this.clock = clock;
        this.tickNanos = tickNanos;
        this.tickSeconds = tickNanos / 1e9;
        this.listener = listener;
    }

    /**
     * Treibt die Schleife um einen Frame an. Einmal pro gerendertem Frame aufrufen
     * (z. B. aus einem Swing-{@code Timer}).
     *
     * <p>Ablauf pro Aufruf:</p>
     * <ol>
     *     <li>Zeit seit dem letzten Aufruf messen (beim allerersten Aufruf wird nur die
     *         Zeitbasis gesetzt und sofort zurückgekehrt, damit keine künstliche Lücke
     *         entsteht).</li>
     *     <li>Das Delta auf {@link #MAX_FRAME_NANOS} begrenzen; der Rest wandert zu
     *         {@link LoopListener#catchUp(long)} (grob nachsimulieren, nicht verwerfen).</li>
     *     <li>Die angesammelte Zeit so lange in feste Schritte umsetzen, bis weniger als
     *         ein Schritt übrig ist ({@link LoopListener#tick(double)}). Ein Rest bleibt
     *         für den nächsten Aufruf stehen.</li>
     *     <li>{@link LoopListener#frame(float)} mit dem variablen Frame-Delta aufrufen
     *         (Input-Update, UI-Update, Repaint).</li>
     * </ol>
     */
    public void advance() {
        final long now = clock.getAsLong();
        // Erster Aufruf: nur die Zeitbasis setzen – ohne Delta würde das Rendern
        // sonst mit einer scheinbar riesigen Lücke starten.
        if (!started) {
            last = now;
            started = true;
            return;
        }

        final long delta = now - last;
        last = now; // vor der Verarbeitung merken, damit die Verarbeitungszeit im nächsten Delta mitgezählt wird

        // Nur so viel nachspielen, wie ein Frame verkraftet; den Rest nicht wegwerfen,
        // sondern an catchUp geben, damit simulierte und echte Zeit konsistent bleiben.
        final long frame = Math.min(delta, MAX_FRAME_NANOS);
        if (delta > frame) {
            listener.catchUp(delta - frame); // große Lücke: grob nachsimulieren
        }

        // Feste Schritte: angesammelte Zeit in komplette Ticks umsetzen,
        // Restliche Teil-Schritte bleiben im Accumulator für den nächsten Frame.
        accumulator += frame;
        while (accumulator >= tickNanos) {
            listener.tick(tickSeconds);
            accumulator -= tickNanos;
        }

        // Variabler Per-Frame-Update mit dem (ggf. begrenzten) Frame-Delta.
        listener.frame(frame / 1e9f);
    }
}