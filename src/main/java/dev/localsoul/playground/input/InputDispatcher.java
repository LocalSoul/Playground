package dev.localsoul.playground.input;

import dev.localsoul.playground.ui.Widget;

import javax.annotation.Nullable;

/**
 * Verteilt Maus-Eingaben (Bewegen, Drücken, Loslassen, Verlassen) an die Widgets der
 * UI-Hierarchie.
 *
 * <p>Der Dispatcher arbeitet ausschließlich über {@link Widget#hitTest(float, float)}
 * und hält deshalb selbst nur den minimalen Zustand, der für die Event-Erkennung nötig ist:</p>
 * <ul>
 *     <li><b>{@code hoveredWidget}</b> – das Widget unter dem Zeiger. Wechselt es, wird auf
 *         dem alten Widget {@code onHoverExit()} und auf dem neuen {@code onHoverEnter()} gefeuert.</li>
 *     <li><b>{@code pressedWidget}</b> – das Widget, auf dem die Maustaste gedrückt wurde.
 *         Ein Klick-Feueremal nur, wenn beim Loslassen dasselbe Widget getroffen wird
 *         („Drücken + Loslassen an derselben Stelle“).</li>
 * </ul>
 *
 * <p>Die eigentlichen Reaktionen auf Hover und Klick stecken in den Callbacks des jeweiligen
 * Widgets (siehe {@code Widget#setClickCallback}, {@code setHoverEnterCallback},
 * {@code setHoverExitCallback}). Alle Koordinaten sind absolut im Koordinatensystem der
 * Wurzel ({@code rootWidget}).</p>
 */
public final class InputDispatcher {

    /**
     * Wurzel der UI-Hierarchie, auf der alle Hit-Tests ausgeführt werden.
     */
    private final Widget rootWidget;
    /**
     * Aktuell gehovtes Widget (unter dem Zeiger); {@code null}, wenn der Zeiger über keiner
     * interaktablem Fläche liegt.
     */
    private Widget hoveredWidget;
    /**
     * Widget, auf dem die Maustaste zuletzt gedrückt wurde; {@code null}, solange nicht gedrückt.
     */
    private Widget pressedWidget;
    /**
     * Letzte bekannte Zeigerposition (für {@link #update()}).
     */
    private float lastX, lastY;
    /**
     * Ob der Zeiger derzeit innerhalb des Canvases liegt.
     */
    private boolean inside;

    /**
     * Erzeugt einen Dispatcher für die übergebene Hierarchie-Wurzel.
     *
     * @param rootWidget das {@link Widget}, an dem alle Hit-Tests beginnen; darf nicht {@code null} sein
     */
    public InputDispatcher(final Widget rootWidget) {
        this.rootWidget = rootWidget;
    }

    /**
     * Meldet eine Zeigerbewegung (Maus bewegt bzw. bei gedrückter Taste gezogen).
     * Aktualisiert die Hover-Zustände auf Basis des neuen Hit-Tests und merkt sich
     * die Position für {@link #update()}.
     *
     * @param x X-Koordinate absolut im Wurzel-Koordinatensystem
     * @param y Y-Koordinate absolut im Wurzel-Koordinatensystem
     */
    public void pointerMoved(final float x, final float y) {
        lastX = x;
        lastY = y;
        inside = true;
        setHovered(rootWidget.hitTest(x, y));
    }

    /**
     * Meldet das Drücken der Maustaste. Das getroffene Widget wird als {@code pressedWidget}
     * markiert und in den Pressed-Zustand versetzt.
     *
     * @param x X-Koordinate absolut
     * @param y Y-Koordinate absolut
     */
    public void pointerPressed(final float x, final float y) {
        pressedWidget = rootWidget.hitTest(x, y);
        if (pressedWidget != null) {
            pressedWidget.setMousePressed(true);
        }
    }

    /**
     * Meldet das Loslassen der Maustaste. Wurde auf demselben Widget gedrückt und losgelassen,
     * wird dort {@link Widget#onClick()} gefeuert; danach ist der Pressed-Zustand aufgehoben.
     *
     * @param x X-Koordinate absolut
     * @param y Y-Koordinate absolut
     */
    public void pointerReleased(final float x, final float y) {
        if (pressedWidget == null) {
            return;
        }

        pressedWidget.setMousePressed(false);
        if (pressedWidget == rootWidget.hitTest(x, y)) {
            pressedWidget.onClick();
        }

        pressedWidget = null;
    }

    /**
     * Meldet das Verlassen des Canvases durch den Zeiger. Hover-Zustand wird geleert und –
     * falls gerade gedrückt – der Pressed-Zustand abgebrochen, damit kein Widget in einem
     * hängenden „gedrückt“-Zustand zurückbleibt, wenn das Loslassen außerhalb stattfindet.
     */
    public void pointerExited() {
        inside = false;
        setHovered(null);
        cancelPress();
    }

    /**
     * Optionaler Aufruf pro Frame (z. B. aus einer Render-/Game-Schleife). Wiederholt den
     * Hit-Test an der zuletzt bekannten Zeigerposition.
     *
     * <p>Nützlich, wenn sich die Hierarchie ohne Mausbewegung verändert (Widgets animieren
     * sich, Inhalte wandern unter den statischen Zeiger). In reinen Swing-Anwendungen, in
     * denen jede Bewegung über {@link #pointerMoved(float, float)} eintrifft, ist dieser
     * Aufruf nicht zwingend erforderlich.</p>
     */
    public void update() {
        if (inside) {
            setHovered(rootWidget.hitTest(lastX, lastY));
        }
    }

    /**
     * Setzt den Hover-Zustand auf {@code next} und feuert die Enter-/Exit-Callbacks der
     * betroffenen Widgets. Tut nichts, wenn sich das Ziel nicht geändert hat.
     *
     * @param next das neu gehovte Widget oder {@code null}, wenn keines gehovt wird
     */
    private void setHovered(final @Nullable Widget next) {
        if (next == hoveredWidget) {
            return;
        }

        if (hoveredWidget != null) {
            hoveredWidget.onHoverExit();
        }

        hoveredWidget = next;

        if (hoveredWidget != null) {
            hoveredWidget.onHoverEnter();
        }
    }

    /**
     * Bricht einen laufenden Klick ab: Das gedrückte Widget wird aus dem Pressed-Zustand
     * geholt und die Press-Referenz verworfen, ohne {@code onClick()} zu feuern.
     * (Der Klick wird verworfen, wenn der Zeiger das Canvas verlassen hat.)
     */
    private void cancelPress() {
        if (pressedWidget != null) {
            pressedWidget.setMousePressed(false);
            pressedWidget = null;
        }
    }
}