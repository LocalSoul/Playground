package dev.localsoul.playground.input;

import dev.localsoul.playground.core.input.InputDispatcher;
import dev.localsoul.playground.core.math.Vector2f;
import dev.localsoul.playground.core.math.rect.RectTransform;
import dev.localsoul.playground.ui.RootWidget;
import dev.localsoul.playground.ui.TestWidget;
import dev.localsoul.playground.ui.anchor.Anchor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class InputDispatcherTest {

    private static final RectTransform BUTTON_TRANSFORM =
            new RectTransform(Anchor.TOP_LEFT, new Vector2f(50f, 50f), new Vector2f(), new Vector2f(50f, 50f));

    private RootWidget root;
    private TestWidget button;
    private InputDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        root = new RootWidget(200, 200);
        button = new TestWidget(BUTTON_TRANSFORM, true);
        root.addChild(button);
        dispatcher = new InputDispatcher(root);
    }

    @Test
    void pointerMovedFiresHoverEnterOnWidget() {
        final AtomicInteger enters = new AtomicInteger();
        button.setHoverEnterCallback(enters::incrementAndGet);

        dispatcher.pointerMoved(60f, 60f);

        assertEquals(1, enters.get());
    }

    @Test
    void hoverEnterFiresOnlyOnceWhileStaying() {
        final AtomicInteger enters = new AtomicInteger();
        button.setHoverEnterCallback(enters::incrementAndGet);

        dispatcher.pointerMoved(60f, 60f);
        dispatcher.pointerMoved(61f, 61f);

        assertEquals(1, enters.get());
    }

    @Test
    void pointerLeavingWidgetFiresHoverExit() {
        final AtomicInteger exits = new AtomicInteger();
        button.setHoverExitCallback(exits::incrementAndGet);

        dispatcher.pointerMoved(60f, 60f);
        dispatcher.pointerMoved(10f, 10f);

        assertEquals(1, exits.get());
    }

    @Test
    void clickFiresWhenPressedAndReleasedOnSameWidget() {
        final AtomicInteger clicks = new AtomicInteger();
        button.setClickCallback(clicks::incrementAndGet);

        dispatcher.pointerPressed(60f, 60f);
        assertTrue(button.isMousePressed());

        dispatcher.pointerReleased(60f, 60f);
        assertEquals(1, clicks.get());
        assertFalse(button.isMousePressed());
    }

    @Test
    void noClickWhenReleasedElsewhere() {
        final AtomicInteger clicks = new AtomicInteger();
        button.setClickCallback(clicks::incrementAndGet);

        dispatcher.pointerPressed(60f, 60f);
        dispatcher.pointerReleased(10f, 10f);

        assertEquals(0, clicks.get());
        assertFalse(button.isMousePressed());
    }

    @Test
    void pressCancelledWhenPointerExits() {
        dispatcher.pointerPressed(60f, 60f);
        assertTrue(button.isMousePressed());

        dispatcher.pointerExited();

        assertFalse(button.isMousePressed());
    }

    @Test
    void hoveringEmptyRootAreaKeepsLogStateNullSafe() {
        // Über einer nicht interaktablen Fläche (Root) dürfen Hover-Events nicht feuern.
        dispatcher.pointerMoved(5f, 5f);
        assertFalse(button.isMousePressed());
    }
}