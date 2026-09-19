package dev.localsoul.playground.core.time;

import java.time.Instant;

/**
 * Liefert die aktuelle <b>Wall-Clock-Zeit</b> des Spiels.
 *
 * <p>Diese Zeit ist für alles gedacht, was über einen Programmneustart hinweg gültig bleiben muss:
 * Endzeitpunkte von Quests, der Zeitstempel des letzten Speicherns und damit die Berechnung des
 * Offline-Fortschritts. Sie ist bewusst austauschbar:</p>
 * <ul>
 *     <li>{@link #system()} nutzt die Systemuhr des Rechners (Standard, für Einzelspieler).</li>
 *     <li>Eine eigene Implementierung kann die Zeit von einem Server holen. Dann lässt sich die
 *     Offline-Zeit nicht mehr durch Verstellen der Systemuhr erschleichen.</li>
 *     <li>In Tests genügt ein Lambda mit einer selbst gesteuerten Zeit.</li>
 * </ul>
 *
 * <p><b>Nicht verwechseln:</b> Der {@code GameLoop} misst die Zeit zwischen zwei Frames nicht
 * hiermit, sondern mit einer monotonen Uhr ({@code System.nanoTime()}). Die ist unabhängig von
 * der Systemuhr und kann nicht springen, ist aber nur innerhalb eines Programmlaufs vergleichbar.</p>
 */
@FunctionalInterface
public interface TimeSource {

    /**
     * Liefert den aktuellen Zeitpunkt.
     *
     * @return der aktuelle Zeitpunkt; nie {@code null}
     */
    Instant now();

    /**
     * Liefert eine {@code TimeSource}, die die Systemuhr des Rechners verwendet.
     *
     * @return eine Zeitquelle auf Basis von {@link Instant#now()}
     */
    static TimeSource system() {
        return Instant::now;
    }
}
