package dev.localsoul.playground.ui.slice;

import javax.annotation.Nonnull;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Zeichnet ein {@link NineSlice} in eine beliebige Zielgröße.
 *
 * <p><b>Prinzip:</b> Das Quellbild wird gedanklich in ein 3×3-Raster geschnitten.
 * Die vier Ecken werden unverzerrt übernommen, die vier Kanten werden nur in eine Richtung
 * gestreckt und das mittlere Segment wird in beide Richtungen (bzw. in einen Cursor- oder
 * Füllbereich) gestreckt. So bleiben abgerundete Ecken und Rahmen-Texuren scharf, egal
 * wie groß oder klein das Ziel wird.</p>
 *
 * <p>Die Ränder ({@code borderLeft/Right/Top/Bottom}) geben an, wie viele Pixel vom jeweiligen
 * Texturrand in die Ecken/Seiten wandern. Wird das Ziel kleiner als die Summe der gegenüberliegenden
 * Ränder, wird das zentrale Segment auf 0 Pixel geklemmt statt negativ zu werden.</p>
 *
 * <p>Diese Klasse ist bewusst nur eine statische Zeichenhilfe und hält keinen eigenen Zustand.</p>
 */
public final class NineSliceRenderer {

    private NineSliceRenderer() {
        // reine Utility-Klasse: keine Instanzen erlaubt
    }

    /**
     * Zeichnet das übergebene {@code nineSlice} skaliert auf {@code targetWidth × targetHeight}.
     *
     * <p>Es werden die 9 Segmente (3×3) aus der Quelle entnommen und jeweils in den korrespondierenden
     * Zielbereich kopiert. Für die Streckung der Ränder reicht dabei das einfache
     * {@link Graphics#drawImage(BufferedImage, int, int, int, int, int, int, int, int, ImageObserver)}:
     * AWT skaliert die jeweilige Quellregion automatisch auf den Zielbereich.</p>
     *
     * @param g            der Grafik-Kontext, in den gezeichnet wird
     * @param nineSlice    die 9-slice-textur samt Randangaben; darf nicht {@code null} sein
     * @param targetWidth  gewünschte Breite des Ergebnisses in Pixeln (Kleiner-als-Randszenarien werden geklemmt)
     * @param targetHeight gewünschte Höhe des Ergebnisses in Pixeln (Kleiner-als-Randszenarien werden geklemmt)
     */
    public static void draw(final @Nonnull Graphics2D g, final @Nonnull NineSlice nineSlice, final int targetWidth, final int targetHeight) {
        final int textureWidth = nineSlice.textureWidth();
        final int textureHeight = nineSlice.textureHeight();

        final int borderLeft = nineSlice.borderLeft();
        final int borderRight = nineSlice.borderRight();
        final int borderTop = nineSlice.borderTop();
        final int borderBottom = nineSlice.borderBottom();

        // Quell-Koordinaten aus Original-Textur: Größe des mittleren Segments in der Quelle.
        final int srcCenterWidth = textureWidth - borderLeft - borderRight;
        final int srcCenterHeight = textureHeight - borderTop - borderBottom;

        // Ziel-Koordinaten (im Zielbereich, ggf. geklemmt falls Ziel kleiner als Border-Summe).
        // Math.max(0, …) verhindert negative Ziel-Breiten für das Zentrum.
        final int destCenterWidth = Math.max(0, targetWidth - borderLeft - borderRight);
        final int destCenterHeight = Math.max(0, targetHeight - borderTop - borderBottom);

        // 3 Spalten-/Zeilen-Grenzen, Quelle: {links, Mitte-links, Mitte-rechts, rechts}
        final int[] srcX = {0, borderLeft, borderLeft + srcCenterWidth, textureWidth};
        final int[] srcY = {0, borderTop, borderTop + srcCenterHeight, textureHeight};

        // 3 Spalten-/Zeilen-Grenzen, Ziel: analog zur Quelle, aber mit den geklemmten Zielgrößen
        final int[] destX = {0, borderLeft, borderLeft + destCenterWidth, targetWidth};
        final int[] destY = {0, borderTop, borderTop + destCenterHeight, targetHeight};

        // Alle 9 Segmente zeichnen: 3 Zeilen × 3 Spalten.
        // drawImage(…, destRect, srcRect, …) kopiert und skaliert dabei automatisch.
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                g.drawImage(nineSlice.texture(),
                        destX[col], destY[row], destX[col + 1], destY[row + 1],
                        srcX[col], srcY[row], srcX[col + 1], srcY[row + 1],
                        null);
            }
        }
    }
}