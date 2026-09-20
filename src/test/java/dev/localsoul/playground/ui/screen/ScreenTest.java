package dev.localsoul.playground.ui.screen;

import dev.localsoul.playground.core.math.Vector2f;
import dev.localsoul.playground.core.math.rect.Rect;
import dev.localsoul.playground.core.math.rect.RectTransform;
import dev.localsoul.playground.ui.RootWidget;
import dev.localsoul.playground.ui.TestWidget;
import dev.localsoul.playground.ui.anchor.Anchor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScreenTest {

    private static final class EmptyScreen extends Screen {
    }

    @Test
    void screenFillsItsParentCompletely() {
        final RootWidget root = new RootWidget(640, 360);
        final Screen screen = new EmptyScreen();
        root.addChild(screen);

        assertEquals(new Rect(0f, 0f, 640f, 360f), screen.getBounds());
        assertEquals(new Rect(0f, 0f, 640f, 360f), screen.getAbsoluteBounds());
    }

    @Test
    void screenFollowsWhenTheParentIsResized() {
        final RootWidget root = new RootWidget(640, 360);
        final Screen screen = new EmptyScreen();
        root.addChild(screen);

        root.resize(1000, 500);

        assertEquals(new Rect(0f, 0f, 1000f, 500f), screen.getBounds());
    }

    @Test
    void emptyScreenAreaDoesNotSwallowClicks() {
        final RootWidget root = new RootWidget(640, 360);
        root.addChild(new EmptyScreen());

        assertNull(root.hitTest(100, 100), "Screen ist nicht interaktiv, leere Flächen fangen keine Klicks ab");
    }

    @Test
    void interactiveChildOfAScreenIsStillFound() {
        final RootWidget root = new RootWidget(640, 360);
        final Screen screen = new EmptyScreen();
        final TestWidget button = new TestWidget(
                new RectTransform(Anchor.TOP_LEFT, new Vector2f(10, 10), new Vector2f(), new Vector2f(50, 50)), true);
        screen.addChild(button);
        root.addChild(screen);

        assertSame(button, root.hitTest(20, 20));
    }
}
