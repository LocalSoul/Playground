package dev.localsoul.playground.ui.anchor;

import dev.localsoul.playground.core.math.Vector2f;
import dev.localsoul.playground.core.math.rect.Rect;
import dev.localsoul.playground.core.math.rect.RectTransform;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnchorTest {

    @Test
    void stretchSpansTheWholeParentInBothAxes() {
        // Vector2f ist veränderlich und hat kein equals, daher werden die Komponenten verglichen.
        assertEquals(0f, Anchor.STRETCH.min().x());
        assertEquals(0f, Anchor.STRETCH.min().y());
        assertEquals(1f, Anchor.STRETCH.max().x());
        assertEquals(1f, Anchor.STRETCH.max().y());
    }

    @Test
    void stretchWithoutOffsetsFillsTheParentCompletely() {
        final RectTransform t = new RectTransform(Anchor.STRETCH, new Vector2f(), new Vector2f(), new Vector2f());

        assertEquals(new Rect(0f, 0f, 800f, 600f), t.resolve(800f, 600f));
    }

    @Test
    void stretchOffsetsActAsInsetsFromTheEdges() {
        // offsetMin schiebt links/oben nach innen, offsetMax rechts/unten (negativ = nach innen).
        final RectTransform t = new RectTransform(Anchor.STRETCH,
                new Vector2f(10f, 20f), new Vector2f(-30f, -40f), new Vector2f());

        assertEquals(new Rect(10f, 20f, 160f, 40f), t.resolve(200f, 100f));
    }

    @Test
    void stretchIgnoresTheSizeValue() {
        final RectTransform withSize = new RectTransform(Anchor.STRETCH,
                new Vector2f(), new Vector2f(), new Vector2f(999f, 999f));

        assertEquals(new Rect(0f, 0f, 200f, 100f), withSize.resolve(200f, 100f));
    }

    @Test
    void resourceBarNavBarAndContentHostFitTogether() {
        final float w = 1280f;
        final float h = 720f;

        // Ressourcenleiste oben, 56 px: oben verankert, in x gestreckt.
        final Rect top = new RectTransform(Anchor.TOP_STRETCH,
                new Vector2f(0, 0), new Vector2f(0, 0), new Vector2f(0, 56)).resolve(w, h);
        // Navigationsleiste unten, 72 px.
        final Rect bottom = new RectTransform(Anchor.BOTTOM_STRETCH,
                new Vector2f(0, -72), new Vector2f(0, 0), new Vector2f(0, 72)).resolve(w, h);
        // Inhaltsbereich füllt den Rest dazwischen.
        final Rect content = new RectTransform(Anchor.STRETCH,
                new Vector2f(0, 56), new Vector2f(0, -72), new Vector2f()).resolve(w, h);

        assertEquals(new Rect(0f, 0f, 1280f, 56f), top);
        assertEquals(new Rect(0f, 648f, 1280f, 72f), bottom);
        assertEquals(new Rect(0f, 56f, 1280f, 592f), content);
        // Keine Lücke, keine Überlappung: oben endet, wo der Inhalt beginnt, und der Inhalt endet, wo unten beginnt.
        assertEquals(top.y() + top.height(), content.y());
        assertEquals(content.y() + content.height(), bottom.y());
    }

    @Test
    void layoutFollowsWhenTheParentIsResized() {
        final RectTransform content = new RectTransform(Anchor.STRETCH,
                new Vector2f(0, 56), new Vector2f(0, -72), new Vector2f());

        assertEquals(new Rect(0f, 56f, 800f, 472f), content.resolve(800f, 600f));
        assertEquals(new Rect(0f, 56f, 1920f, 952f), content.resolve(1920f, 1080f));
    }
}
