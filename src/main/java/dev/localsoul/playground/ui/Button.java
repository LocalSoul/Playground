package dev.localsoul.playground.ui;

import dev.localsoul.playground.math.rect.Rect;
import dev.localsoul.playground.math.rect.RectTransform;
import dev.localsoul.playground.ui.slice.NineSlice;
import dev.localsoul.playground.ui.slice.NineSliceRenderer;

import javax.annotation.Nonnull;
import java.awt.*;

/**
 * Ein Button mit 9-Slice-Hintergrund und zentriertem Text.
 *
 * <p>Das Aussehen besteht aus zwei Layern:</p>
 * <ol>
 *     <li><b>Hintergrund</b> – der übergebene {@link NineSlice} wird über
 *         {@link NineSliceRenderer} auf die lokale Widget-Größe gestreckt.</li>
 *     <li><b>Text</b> – eine Beschriftung, die exakt in der Mitte des Buttons
 *         (horizontal und vertikal) platziert wird.</li>
 * </ol>
 *
 * <p>Hinweis: {@link #drawSelf(Graphics2D, Rect)} wird vom {@link Widget} bereits in lokale
 * Koordinaten übersetzt – die Origin liegt also in der linken oberen Ecke des Buttons. Die
 * aufgelösten Bounds liefert der {@link Widget#draw(Graphics2D)}-Aufruf als Parameter; deshalb
 * wird hier ausschließlich mit {@code width}/{@code height} gerechnet, nicht mit deren
 * {@code x}/{@code y}.</p>
 */
public class Button extends Widget {

    /**
     * Standardfarbe des Texts. Die aktuelle Button-Textur ist orange, daher Weiß.
     * Die Farbe ist pro Instanz über {@link #setTextColor(Color)} änderbar.
     */
    private static final Color DEFAULT_TEXT_COLOR = Color.WHITE;

    /**
     * Standardschriftart des Texts (Arial, normal, 12 pt).
     * Kann pro Instanz über {@link #setFont(Font)} geändert werden.
     */
    private static final Font DEFAULT_FONT = new Font("Arial", Font.PLAIN, 12);

    /**
     * Die 9-Slice-Textur, die als Hintergrund des Buttons dient.
     */
    @Nonnull
    private final NineSlice slice;
    /**
     * Die Beschriftung, die mittig auf dem Button gezeichnet wird.
     */
    @Nonnull
    private String text;
    /**
     * Farbe der Beschriftung. Initial {@link #DEFAULT_TEXT_COLOR}.
     */
    @Nonnull
    private Color textColor;
    /**
     * Schriftart der Beschriftung. Initial {@link #DEFAULT_FONT}.
     */
    @Nonnull
    private Font font;

    /**
     * Erzeugt einen Button mit Hintergrund-Textur, Beschriftung und Transformation.
     *
     * @param transform die Anker-/Offset-/Größen-Definition für diesen Button; darf nicht {@code null} sein
     * @param slice     die 9-Slice-Textur für den Hintergrund; darf nicht {@code null} sein
     * @param text      die Beschriftung; darf nicht {@code null} sein
     */
    public Button(final @Nonnull RectTransform transform, final @Nonnull NineSlice slice, final @Nonnull String text) {
        super(transform);
        this.slice = slice;
        this.text = text;
        this.textColor = DEFAULT_TEXT_COLOR;
        this.font = DEFAULT_FONT;
    }

    /**
     * Setzt die Farbe der Beschriftung neu.
     *
     * @param textColor die neue Textfarbe; darf nicht {@code null} sein
     */
    public void setTextColor(final @Nonnull Color textColor) {
        this.textColor = textColor;
    }

    /**
     * Setzt die Beschriftung des Buttons neu.
     *
     * @param text die neue Beschriftung; darf nicht {@code null} sein
     */
    public void setText(@Nonnull final String text) {
        this.text = text;
    }

    /**
     * Setzt die Schriftart der Beschriftung neu.
     *
     * @param font die neue Schriftart; darf nicht {@code null} sein
     */
    public void setFont(@Nonnull final Font font) {
        this.font = font;
    }

    /**
     * Zeichnet den Button: zuerst den 9-Slice-Hintergrund, dann den zentrierten Text.
     *
     * <p>Vertikale Zentrierung: {@link FontMetrics#getHeight()} liefert die komplette
     * Zeilenhöhe (Ascent + Descent + Leading). Die Baseline des Strings wird so gesetzt,
     * dass die komplette Zeilenbox mittig sitzt:</p>
     * <pre>
     * baselineY = (height − lineHeight) / 2 + ascent
     * </pre>
     *
     * <p>Ist die Beschriftung länger als der Button, wird sie an den Button-Rändern
     * abgeschnitten (Clipping), statt über die Hintergrund-Textur hinauszuzeichnen.</p>
     *
     * @param g      der bereits in lokale Koordinaten übersetzte Grafik-Kontext
     * @param bounds die aufgelösten lokalen Bounds; verwendet werden nur Breite/Höhe
     */
    @Override
    protected void drawSelf(final Graphics2D g, final Rect bounds) {
        final int width = (int) bounds.width();
        final int height = (int) bounds.height();

        // Hintergrund: 9-Slice an den lokalen Ursprung (0,0) zeichnen.
        NineSliceRenderer.draw(g, slice, width, height);

        // Schriftart setzen, BEVOR die FontMetrics ermittelt werden –
        // getFontMetrics() misst stets mit der aktuell gesetzten Font.
        g.setFont(font);

        // FontMetrics nur einmal ermitteln, dann gleich für Breite und Höhe nutzen.
        final FontMetrics fm = g.getFontMetrics();
        final int textWidth = fm.stringWidth(text);

        // Horizontale Zentrierung: linke Textkante = Mitte − halbe Textbreite.
        // Anders als beim Hintergrund wird hier nicht auf int gecastet, sondern mit den
        // rohen Float-Bounds gerechnet – so bleibt die Zentrierung subpixel-genau.
        final float centerX = (bounds.width() - textWidth) / 2f;

        // Vertikale Zentrierung: Baseline so wählen, dass die komplette Zeilenbox (nicht nur
        // der Ascent-Teil) in der Mitte sitzt. Ohne +ascent würde der Text zu hoch stehen.
        // Auch hier die Float-Bounds verwenden, um Rundungsfehler der Höhe zu vermeiden.
        final float centerY = (bounds.height() - fm.getHeight()) / 2f + fm.getAscent();

        // Text auf die Button-Fläche begrenzen: Ein zu langer Text läuft sonst über die
        // 9-Slice-Kanten hinaus. Bewusst clipRect() statt setClip(): clipRect() schneidet das
        // Rechteck mit einem evtl. vorhandenen Clip (z. B. vom Eltern-Widget) und ersetzt ihn nicht.
        g.clipRect(0, 0, width, height);

        g.setColor(textColor);
        g.drawString(text, centerX, centerY);
    }

}