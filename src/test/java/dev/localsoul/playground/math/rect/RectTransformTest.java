package dev.localsoul.playground.math.rect;

import dev.localsoul.playground.math.Vector2f;
import dev.localsoul.playground.ui.anchor.Anchor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RectTransformTest {

    @Test
    void resolveNonStretchedTopLeft() {
        final RectTransform t = new RectTransform(
                Anchor.TOP_LEFT,
                new Vector2f(10f, 20f),
                new Vector2f(),
                new Vector2f(100f, 50f)
        );
        final Rect r = t.resolve(800f, 600f);
        assertEquals(10f, r.x());
        assertEquals(20f, r.y());
        assertEquals(100f, r.width());
        assertEquals(50f, r.height());
    }

    @Test
    void resolveNonStretchedCenter() {
        final RectTransform t = new RectTransform(
                Anchor.CENTER,
                new Vector2f(-50f, -25f),
                new Vector2f(),
                new Vector2f(100f, 50f)
        );
        final Rect r = t.resolve(800f, 600f);
        assertEquals(350f, r.x());
        assertEquals(275f, r.y());
        assertEquals(100f, r.width());
        assertEquals(50f, r.height());
    }

    @Test
    void resolveStretchedBothAxes() {
        final RectTransform t = new RectTransform(
                new Anchor(new Vector2f(), new Vector2f(1f, 1f)),
                new Vector2f(10f, 10f),
                new Vector2f(-10f, -10f),
                new Vector2f(0f, 0f)
        );
        final Rect r = t.resolve(800f, 600f);
        assertEquals(10f, r.x());
        assertEquals(10f, r.y());
        assertEquals(780f, r.width());
        assertEquals(580f, r.height());
    }

    @Test
    void resolveStretchOnlyOnX() {
        final RectTransform t = new RectTransform(
                Anchor.TOP_STRETCH,
                new Vector2f(10f, 100f),
                new Vector2f(-10f, 0f),
                new Vector2f(0f, 0f)
        );
        final Rect r = t.resolve(800f, 600f);
        assertEquals(10f, r.x());
        assertEquals(100f, r.y());
        assertEquals(780f, r.width());
        assertEquals(0f, r.height());
    }

    @Test
    void resolveStretchOnlyOnY() {
        final RectTransform t = new RectTransform(
                Anchor.LEFT_STRETCH,
                new Vector2f(50f, 20f),
                new Vector2f(0f, -20f),
                new Vector2f(0f, 0f)
        );
        final Rect r = t.resolve(800f, 600f);
        assertEquals(50f, r.x());
        assertEquals(20f, r.y());
        assertEquals(0f, r.width());
        assertEquals(560f, r.height());
    }

    @Test
    void resolveBottomRightNonStretched() {
        final RectTransform t = new RectTransform(
                Anchor.BOTTOM_RIGHT,
                new Vector2f(-100f, -50f),
                new Vector2f(),
                new Vector2f(100f, 50f)
        );
        final Rect r = t.resolve(800f, 600f);
        assertEquals(700f, r.x());
        assertEquals(550f, r.y());
        assertEquals(100f, r.width());
        assertEquals(50f, r.height());
    }
}