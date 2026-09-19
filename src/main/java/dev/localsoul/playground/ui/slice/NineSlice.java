package dev.localsoul.playground.ui.slice;

import java.awt.image.BufferedImage;

/**
 * Eine 9-Slice-Textur samt Rahmenbreiten – die Datenbasis für das skalierbare Zeichnen
 * von UI-Oberflächen ohne Verzerrung der Ecken (siehe {@link NineSliceRenderer}).
 *
 * <p>Die Textur wird gedanklich in ein 3×3-Raster zerlegt:</p>
 * <ul>
 *     <li>Die vier Ecken werden unverändert übernommen.</li>
 *     <li>Die vier Kanten werden nur entlang ihrer Achse gestreckt.</li>
 *     <li>Das mittlere Segment füllt die restliche Fläche.</li>
 * </ul>
 *
 * <p>Die Border-Werte geben die Dicke der Ränder in Pixeln der Original-Textur an.
 * {@code borderLeft}/{@code borderRight} bzw. {@code borderTop}/{@code borderBottom}
 * wandern in die seitlichen bzw. oberen/unteren Segmentbereiche.</p>
 */
public record NineSlice(BufferedImage texture, int borderLeft, int borderRight, int borderTop, int borderBottom) {

    /**
     * Liefert die Breite der zugrunde liegenden Textur in Pixeln.
     *
     * @return {@code texture.getWidth()}
     */
    public int textureWidth() {
        return texture.getWidth();
    }

    /**
     * Liefert die Höhe der zugrunde liegenden Textur in Pixeln.
     *
     * @return {@code texture.getHeight()}
     */
    public int textureHeight() {
        return texture.getHeight();
    }

}
