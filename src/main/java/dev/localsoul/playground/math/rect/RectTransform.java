package dev.localsoul.playground.math.rect;

import dev.localsoul.playground.math.Vector2f;
import dev.localsoul.playground.ui.anchor.Anchor;

import javax.annotation.Nonnull;

/**
 * Beschreibt, wie ein Widget relativ zu seinem Eltern-Widget positioniert und skaliert wird –
 * das Pendant zur klassischen RectTransform aus UI-/Game-Engines (z. B. Unity).
 *
 * <p>Eine Transform besteht aus drei Teilen:</p>
 * <ul>
 *     <li><b>{@link Anchor}</b> – zwei normierte Ankerpunkte (0…1) im Eltern-Koordinatensystem,
 *         die als Referenzpunkte für die Ränder dienen.</li>
 *     <li><b>{@code offsetMin}/{@code offsetMax}</b> – die Abstände der beiden Ränder von den
 *         jeweiligen Ankerpunkten.</li>
 *     <li><b>{@code size}</b> – die Breite/Höhe des Widgets, wenn die Anker nicht gestreckt sind.</li>
 * </ul>
 *
 * <p>Ist {@code anchor.min == anchor.max} auf einer Achse (nicht gestreckt), geben
 * {@code offsetMin} die Position und {@code size} die Ausdehnung entlang dieser Achse an.
 * Sind die Anker auf einer Achse verschieden (gestreckt, z. B. {@link Anchor#TOP_STRETCH}),
 * werden beide Ränder unabhängig von ihren Ankerpunkten versetzt und {@code size} wird ignoriert –
 * das Widget füllt dann den Bereich zwischen den Ankerpunkten plus/minus den Offsets.</p>
 *
 * <p>Die Werte gelten immer relativ zum Eltern-Widget; die Auflösung in absolute Koordinaten
 * übernimmt {@link #resolve(float, float)} zusammen mit der Hierarchie in {@code Widget}.</p>
 */
public record RectTransform(Anchor anchor, Vector2f offsetMin, Vector2f offsetMax, Vector2f size) {

    /**
     * Löst diese Transform gegen die übergegebenen Eltern-Abmessungen in konkrete
     * {@link Rect}-Koordinaten auf.
     *
     * <p>Für jede Achse (X und Y) wird separat entschieden:</p>
     * <ul>
     *     <li><b>Nicht gestreckt</b> ({@code min == max}): Der Anker-Punkt markiert den linken/oberen
     *         Rand, {@code offsetMin} verschiebt ihn und {@code size} bestimmt die Ausdehnung.</li>
     *     <li><b>Gestreckt</b> ({@code min != max}): Der linke/obere Rand wird vom {@code min}-Anker,
     *         der rechte/untere vom {@code max}-Anker aus gemessen (jeweils plus dem zugehörigen
     *         Offset); {@code size} wird ignoriert.</li>
     * </ul>
     *
     * @param parentWidth  Breite des Eltern-Widgets in Pixeln (nicht negativ)
     * @param parentHeight Höhe des Eltern-Widgets in Pixeln (nicht negativ)
     * @return das aufgelöste Rechteck mit x, y, Breite und Höhe
     */
    public @Nonnull Rect resolve(final float parentWidth, final float parentHeight) {
        final float x1;
        final float x2;
        final float y1;
        final float y2;

        // X-Achse
        if (anchor.min().x() == anchor.max().x()) {
            // nicht gestreckt: offsetMin.x ist die Position, size gibt die Breite
            x1 = anchor.min().x() * parentWidth + offsetMin.x();
            x2 = x1 + size.x();
        } else {
            // gestreckt: beide Ränder werden vom jeweiligen Anker-Punkt aus versetzt
            x1 = anchor.min().x() * parentWidth + offsetMin.x();
            x2 = anchor.max().x() * parentWidth + offsetMax.x();
        }

        // Y-Achse, analog
        if (anchor.min().y() == anchor.max().y()) {
            y1 = anchor.min().y() * parentHeight + offsetMin.y();
            y2 = y1 + size.y();
        } else {
            y1 = anchor.min().y() * parentHeight + offsetMin.y();
            y2 = anchor.max().y() * parentHeight + offsetMax.y();
        }

        // Breite/Höhe als Differenz der beiden Ränder.
        return new Rect(x1, y1, x2 - x1, y2 - y1);
    }

}