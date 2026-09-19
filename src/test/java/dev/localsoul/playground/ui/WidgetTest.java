package dev.localsoul.playground.ui;

import dev.localsoul.playground.core.math.Vector2f;
import dev.localsoul.playground.core.math.rect.Rect;
import dev.localsoul.playground.core.math.rect.RectTransform;
import dev.localsoul.playground.ui.anchor.Anchor;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class WidgetTest {

    @Test
    void rootBoundsMatchSize() {
        final RootWidget root = new RootWidget(100, 200);
        assertEquals(0f, root.getBounds().x());
        assertEquals(0f, root.getBounds().y());
        assertEquals(100f, root.getBounds().width());
        assertEquals(200f, root.getBounds().height());
    }

    @Test
    void rootResizeUpdatesBounds() {
        final RootWidget root = new RootWidget(100, 100);
        root.resize(400, 300);
        assertEquals(400f, root.getBounds().width());
        assertEquals(300f, root.getBounds().height());
    }

    @Test
    void addChildSetsParentAndPreservesOrder() {
        final RootWidget root = new RootWidget(100, 100);
        final TestWidget first = new TestWidget(new RectTransform(Anchor.TOP_LEFT, new Vector2f(), new Vector2f(), new Vector2f(10, 10)));
        final TestWidget second = new TestWidget(new RectTransform(Anchor.TOP_LEFT, new Vector2f(), new Vector2f(), new Vector2f(10, 10)));

        root.addChild(first);
        root.addChild(second);

        assertSame(root, first.getParent());
        assertSame(root, second.getParent());
        assertEquals(2, root.getChildren().size());
        assertSame(first, root.getChildren().get(0));
        assertSame(second, root.getChildren().get(1));
    }

    @Test
    void getChildrenReturnsUnmodifiableCopy() {
        final RootWidget root = new RootWidget(100, 100);
        root.addChild(new TestWidget(new RectTransform(Anchor.TOP_LEFT, new Vector2f(), new Vector2f(), new Vector2f(10, 10))));

        final List<Widget> children = root.getChildren();
        assertThrows(UnsupportedOperationException.class, () -> children.add(new TestWidget(new RectTransform(Anchor.TOP_LEFT, new Vector2f(), new Vector2f(), new Vector2f(10, 10)))));
        assertEquals(1, root.getChildren().size());
    }

    @Test
    void childBoundsAreRelativeToParent() {
        final RootWidget root = new RootWidget(800, 600);
        final TestWidget child = new TestWidget(new RectTransform(Anchor.TOP_LEFT, new Vector2f(50f, 30f), new Vector2f(), new Vector2f(100f, 40f)));
        root.addChild(child);

        assertEquals(50f, child.getBounds().x());
        assertEquals(30f, child.getBounds().y());
        assertEquals(100f, child.getBounds().width());
        assertEquals(40f, child.getBounds().height());
    }

    @Test
    void getAbsoluteBoundsIncludesParentPosition() {
        final RootWidget root = new RootWidget(800, 600);
        final TestWidget child = new TestWidget(new RectTransform(Anchor.TOP_LEFT, new Vector2f(50f, 30f), new Vector2f(), new Vector2f(100f, 40f)));
        root.addChild(child);

        final Rect abs = child.getAbsoluteBounds();
        assertEquals(50f, abs.x());
        assertEquals(30f, abs.y());
        assertEquals(100f, abs.width());
        assertEquals(40f, abs.height());
    }

    @Test
    void getAbsoluteBoundsNested() {
        final RootWidget root = new RootWidget(800, 600);
        final TestWidget parent = new TestWidget(new RectTransform(Anchor.TOP_LEFT, new Vector2f(100f, 100f), new Vector2f(), new Vector2f(200f, 200f)));
        final TestWidget child = new TestWidget(new RectTransform(Anchor.TOP_LEFT, new Vector2f(25f, 25f), new Vector2f(), new Vector2f(50f, 50f)));
        root.addChild(parent);
        parent.addChild(child);

        final Rect abs = child.getAbsoluteBounds();
        assertEquals(125f, abs.x());
        assertEquals(125f, abs.y());
        assertEquals(50f, abs.width());
        assertEquals(50f, abs.height());
    }

    @Test
    void hitTestReturnsTopmostChildFirst() {
        final RootWidget root = new RootWidget(100, 100);
        final TestWidget first = new TestWidget(new RectTransform(Anchor.TOP_LEFT, new Vector2f(10f, 10f), new Vector2f(), new Vector2f(20f, 20f)), true);
        final TestWidget second = new TestWidget(new RectTransform(Anchor.TOP_LEFT, new Vector2f(15f, 10f), new Vector2f(), new Vector2f(20f, 20f)), true);
        root.addChild(first);
        root.addChild(second);

        assertSame(second, root.hitTest(20f, 15f));
        assertSame(first, root.hitTest(12f, 15f));
    }

    @Test
    void hitTestSkipsNonInteractableWidgets() {
        // RootWidget ist nicht interaktabel – trifft kein interaktables Kind,
        // liefert der Hit-Test null (statt die Wurzel selbst).
        final RootWidget root = new RootWidget(100, 100);
        root.addChild(new TestWidget(new RectTransform(Anchor.TOP_LEFT, new Vector2f(10f, 10f), new Vector2f(), new Vector2f(20f, 20f))));

        assertNull(root.hitTest(15f, 15f));
    }

    @Test
    void hitTestReturnsInteractableChildInsideNonInteractableContainer() {
        // Ein nicht interaktabler Container (Panel-artig) darf als Träger für ein
        // interaktables Kind fungieren: Das Kind wird getroffen, die Container-Fläche nicht.
        final RootWidget root = new RootWidget(100, 100);
        final TestWidget panel = new TestWidget(new RectTransform(Anchor.TOP_LEFT, new Vector2f(), new Vector2f(), new Vector2f(100f, 100f)));
        final TestWidget button = new TestWidget(new RectTransform(Anchor.TOP_LEFT, new Vector2f(10f, 10f), new Vector2f(), new Vector2f(20f, 20f)), true);
        root.addChild(panel);
        panel.addChild(button);

        assertSame(button, root.hitTest(15f, 15f));
        assertNull(root.hitTest(5f, 5f));
    }

    @Test
    void hitTestReturnsNullOutsideEverything() {
        final RootWidget root = new RootWidget(100, 100);
        root.addChild(new TestWidget(new RectTransform(Anchor.TOP_LEFT, new Vector2f(10f, 10f), new Vector2f(), new Vector2f(20f, 20f)), true));

        assertNull(root.hitTest(150f, 150f));
    }

    @Test
    void isInteractableReflectsConstructor() {
        final RectTransform transform = new RectTransform(Anchor.TOP_LEFT, new Vector2f(), new Vector2f(), new Vector2f(10, 10));
        assertFalse(new TestWidget(transform).isInteractable());
        assertTrue(new TestWidget(transform, true).isInteractable());
    }

    @Test
    void clickCallbackFiresOnClick() {
        final RootWidget root = new RootWidget(100, 100);
        final TestWidget widget = new TestWidget(new RectTransform(Anchor.TOP_LEFT, new Vector2f(), new Vector2f(), new Vector2f(10, 10)), true);
        root.addChild(widget);

        final AtomicBoolean clicked = new AtomicBoolean(false);
        widget.setClickCallback(() -> clicked.set(true));

        widget.onClick();
        assertTrue(clicked.get());
    }

    @Test
    void hoverCallbacksFireOnEnterAndExit() {
        final TestWidget widget = new TestWidget(new RectTransform(Anchor.TOP_LEFT, new Vector2f(), new Vector2f(), new Vector2f(10, 10)), true);

        final AtomicBoolean entered = new AtomicBoolean(false);
        final AtomicBoolean exited = new AtomicBoolean(false);
        widget.setHoverEnterCallback(() -> entered.set(true));
        widget.setHoverExitCallback(() -> exited.set(true));

        widget.onHoverEnter();
        assertTrue(entered.get());

        widget.onHoverExit();
        assertTrue(exited.get());
    }

    @Test
    void mousePressedStateToggles() {
        final TestWidget widget = new TestWidget(new RectTransform(Anchor.TOP_LEFT, new Vector2f(), new Vector2f(), new Vector2f(10, 10)), true);

        assertFalse(widget.isMousePressed());
        widget.setMousePressed(true);
        assertTrue(widget.isMousePressed());
        widget.setMousePressed(false);
        assertFalse(widget.isMousePressed());
    }

    @Test
    void getAnchorAndTransformReflectConstructor() {
        final RectTransform transform = new RectTransform(Anchor.CENTER, new Vector2f(-10f, -5f), new Vector2f(), new Vector2f(20f, 10f));
        final RootWidget root = new RootWidget(100, 100);
        final TestWidget child = new TestWidget(transform);
        root.addChild(child);

        assertEquals(Anchor.CENTER, child.getAnchor());
        assertSame(transform, child.getTransform());
    }

    @Test
    void relayoutInvokesLayoutStrategy() {
        final RootWidget root = new RootWidget(100, 100);
        final TestWidget child = new TestWidget(new RectTransform(Anchor.TOP_LEFT, new Vector2f(), new Vector2f(), new Vector2f(10, 10)));
        root.addChild(child);

        final AtomicBoolean invoked = new AtomicBoolean(false);
        root.setLayoutStrategy((widget, children) -> {
            invoked.set(true);
            assertSame(root, widget);
            assertSame(child, children.getFirst());
        });

        root.relayout();
        assertTrue(invoked.get());
    }

    @Test
    void relayoutWithoutStrategyDoesNothing() {
        final RootWidget root = new RootWidget(100, 100);
        root.relayout();
    }
}