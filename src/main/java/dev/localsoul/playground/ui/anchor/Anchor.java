package dev.localsoul.playground.ui.anchor;

import dev.localsoul.playground.core.math.Vector2f;

/**
 * Ein Paar normierter Ankerpunkte ({@code min}, {@code max}) im Bereich 0…1, das einen
 * Anker-Punkt oder eine Streckung beschreibt (siehe {@code RectTransform}).
 *
 * <p>Da {@link Vector2f} veränderlich ist, liefern sowohl {@link #min()} als auch
 * {@link #max()} eine defensive Kopie zurück – durch Mutieren einer zurückgegebenen
 * Referenz lässt sich der Anker also nicht nachträglich verändern (ansonsten wären die
 * statischen Konstanten dieser Klasse ungewollt modifizierbar).</p>
 */
public record Anchor(Vector2f min, Vector2f max) {

    public static final Anchor TOP_LEFT = new Anchor(new Vector2f(), new Vector2f());
    public static final Anchor TOP_RIGHT = new Anchor(new Vector2f(1f, 0f), new Vector2f(1f, 0f));

    public static final Anchor BOTTOM_LEFT = new Anchor(new Vector2f(0f, 1f), new Vector2f(0f, 1f));
    public static final Anchor BOTTOM_RIGHT = new Anchor(new Vector2f(1f, 1f), new Vector2f(1f, 1f));

    public static final Anchor CENTER = new Anchor(new Vector2f(0.5f), new Vector2f(0.5f));

    public static final Anchor TOP_STRETCH = new Anchor(new Vector2f(), new Vector2f(1f, 0f));
    public static final Anchor BOTTOM_STRETCH = new Anchor(new Vector2f(0f, 1f), new Vector2f(1f, 1f));
    public static final Anchor LEFT_STRETCH = new Anchor(new Vector2f(), new Vector2f(0f, 1f));
    public static final Anchor RIGHT_STRETCH = new Anchor(new Vector2f(1f, 0f), new Vector2f(1f, 1f));

    /**
     * Liefert den unteren/kleineren Anker-Punkt als defensive Kopie.
     *
     * @return eine Kopie des {@code min}-Vektors, damit der interne Wert nicht mutiert wird
     */
    @Override
    public Vector2f min() {
        return new Vector2f(min);
    }

    /**
     * Liefert den oberen/größeren Anker-Punkt als defensive Kopie.
     *
     * @return eine Kopie des {@code max}-Vektors, damit der interne Wert nicht mutiert wird
     */
    @Override
    public Vector2f max() {
        return new Vector2f(max);
    }
}
