package dev.localsoul.playground.math.rect;

import dev.localsoul.playground.math.Vector2f;

public record Rect(float x, float y, float width, float height) {

    public float right() {
        return x + width;
    }

    public float bottom() {
        return y + height;
    }

    public boolean contains(final Vector2f point) {
        return point.x() >= x && point.x() <= right() && point.y() >= y && point.y() <= bottom();
    }

    public boolean contains(final float x, final float y) {
        return contains(new Vector2f(x, y));
    }

}
