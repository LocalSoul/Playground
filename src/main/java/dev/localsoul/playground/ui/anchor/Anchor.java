package dev.localsoul.playground.ui.anchor;

import dev.localsoul.playground.math.Vector2f;

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

    @Override
    public Vector2f min() {
        return new Vector2f(min);
    }
}
