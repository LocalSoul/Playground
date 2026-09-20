package dev.localsoul.playground.ui.screen;

import dev.localsoul.playground.core.math.Vector2f;
import dev.localsoul.playground.core.math.rect.RectTransform;
import dev.localsoul.playground.ui.Widget;
import dev.localsoul.playground.ui.anchor.Anchor;

/**
 * Ein Bildschirm des Spiels (z. B. Quest-Screen oder Charakter-Screen): ein {@link Widget}, das
 * die gesamte Fläche seines {@link ScreenHost} ausfüllt und beim Wechsel Lebenszyklus-Hooks
 * erhält.
 *
 * <p>Ein Screen wird typischerweise <b>einmal gebaut</b> und im {@link Navigator} registriert.
 * Beim Wechsel wird er nur aus dem Widget-Baum ausgehängt, nicht zerstört; seine Kinder und sein
 * Zustand (z. B. eine Scroll-Position) bleiben erhalten. Deshalb sollte ein Screen nur eine
 * <i>Ansicht</i> des Spielmodells sein und in {@link #onShow()} auffrischen, was sich in der
 * Zwischenzeit geändert haben kann.</p>
 *
 * <p>Der Screen ist selbst nicht interaktiv ({@code interactable = false}): Leere Flächen fangen
 * keine Klicks ab, nur seine interaktiven Kinder (z. B. Buttons) reagieren.</p>
 */
public abstract class Screen extends Widget {

    /**
     * Erzeugt einen Screen, der in beiden Achsen ohne Abstand die ganze Fläche des Elternteils
     * ({@link Anchor#STRETCH}) ausfüllt.
     */
    protected Screen() {
        super(new RectTransform(Anchor.STRETCH, new Vector2f(), new Vector2f(), new Vector2f()), false);
    }

    /**
     * Wird vom {@link ScreenHost} aufgerufen, <b>nachdem</b> dieser Screen in den Widget-Baum
     * eingehängt wurde (Eltern-Bezug und Bounds sind dann bereits gültig). Gedacht, um die
     * Anzeige aus dem Spielmodell aufzufrischen. Die Standard-Implementierung ist leer.
     */
    protected void onShow() {
        // Default: nichts zu tun.
    }

    /**
     * Wird vom {@link ScreenHost} aufgerufen, <b>bevor</b> dieser Screen wieder aus dem
     * Widget-Baum ausgehängt wird. Gedacht für Aufräumarbeiten wie das Abbrechen einer
     * Eingabe. Die Standard-Implementierung ist leer.
     */
    protected void onHide() {
        // Default: nichts zu tun.
    }
}
