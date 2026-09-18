package dev.localsoul.playground.math;

public class Vector2f {

    private float x;
    private float y;

    public Vector2f(final float x, final float y) {
        this.x = x;
        this.y = y;
    }

    public Vector2f() {
        this(0, 0);
    }

    public Vector2f(final float i) {
        this(i, i);
    }

    public Vector2f(final Vector2f vector2f) {
        this(vector2f.x, vector2f.y);
    }


    public Vector2f set(final float x, final float y) {
        return new Vector2f(x, y);
    }

    public Vector2f set(final Vector2f v) {
        return set(v.x, v.y);
    }

    public Vector2f add(final Vector2f vector2f) {
        return new Vector2f(x + vector2f.x, y + vector2f.y);
    }

    public Vector2f sub(final Vector2f vector2f) {
        return new Vector2f(x - vector2f.x, y - vector2f.y);
    }

    public Vector2f mul(final Vector2f v) {
        return new Vector2f(x * v.x, y * v.y);
    }


    public Vector2f div(final Vector2f v) {
        if (v.x == 0 || v.y == 0) {
            throw new RuntimeException("Division by zero");
        }
        return new Vector2f(x / v.x, y / v.y);
    }

    public Vector2f div(final float m) {
        return div(new Vector2f(m, m));
    }

    public void setX(final float x) {
        this.x = x;
    }

    public void setY(final float y) {
        this.y = y;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

}
