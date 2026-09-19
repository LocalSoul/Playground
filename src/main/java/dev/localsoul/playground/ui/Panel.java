package dev.localsoul.playground.ui;

import dev.localsoul.playground.core.math.rect.Rect;
import dev.localsoul.playground.core.math.rect.RectTransform;
import dev.localsoul.playground.ui.slice.NineSlice;
import dev.localsoul.playground.ui.slice.NineSliceRenderer;

import java.awt.*;

/**
 * Ein Container-Panel mit 9-Slice-Hintergrund.
 *
 * <p>Die Größe ergibt sich aus den vom {@link Widget} aufgelösten Bounds; der {@link NineSlice}
 * wird über {@link NineSliceRenderer} ohne Verzerrung der Ecken auf diese Größe gestreckt.
 * Im Gegensatz zum {@link Button} besitzt ein Panel keine Beschriftung.</p>
 */
public class Panel extends Widget {

    /**
     * Die 9-Slice-Textur, die als Hintergrund des Panels dient.
     */
    private final NineSlice nineSlice;

    /**
     * Erzeugt ein Panel mit Transformation und Hintergrund-Textur.
     *
     * @param transform die Anker-/Offset-/Größen-Definition für dieses Panel; darf nicht {@code null} sein
     * @param nineSlice die 9-Slice-Textur für den Hintergrund; darf nicht {@code null} sein
     */
    public Panel(final RectTransform transform, final NineSlice nineSlice) {
        super(transform, false);
        this.nineSlice = nineSlice;
    }

    /**
     * Zeichnet den 9-Slice-Hintergrund über die volle Breite/Höhe der Bounds.
     *
     * @param g      der bereits in lokale Koordinaten übersetzte Grafik-Kontext
     * @param bounds die aufgelösten lokalen Bounds; verwendet werden nur Breite/Höhe
     */
    @Override
    protected void drawSelf(final Graphics2D g, final Rect bounds) {
        NineSliceRenderer.draw(g, nineSlice, (int) bounds.width(), (int) bounds.height());
    }


}
