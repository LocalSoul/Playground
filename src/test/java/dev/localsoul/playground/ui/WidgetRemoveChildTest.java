package dev.localsoul.playground.ui;

import dev.localsoul.playground.core.math.Vector2f;
import dev.localsoul.playground.core.math.rect.RectTransform;
import dev.localsoul.playground.ui.anchor.Anchor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prüft {@link Widget#removeChild(Widget)} als Gegenstück zu {@code addChild}.
 */
class WidgetRemoveChildTest {

    private static RectTransform transform() {
        return new RectTransform(Anchor.TOP_LEFT, new Vector2f(10, 10), new Vector2f(), new Vector2f(50, 50));
    }

    @Test
    void removeChildDetachesFromParentAndReturnsTrue() {
        final RootWidget root = new RootWidget(200, 200);
        final TestWidget child = new TestWidget(transform());
        root.addChild(child);

        assertTrue(root.removeChild(child));

        assertNull(child.getParent());
        assertTrue(root.getChildren().isEmpty());
    }

    @Test
    void removeChildOfSomeoneElseReturnsFalseAndChangesNothing() {
        final RootWidget root = new RootWidget(200, 200);
        final TestWidget owner = new TestWidget(transform());
        final TestWidget stranger = new TestWidget(transform());
        final TestWidget child = new TestWidget(transform());
        root.addChild(owner);
        root.addChild(stranger);
        owner.addChild(child);

        assertFalse(stranger.removeChild(child));

        assertSame(owner, child.getParent());
        assertEquals(List.of(child), owner.getChildren());
    }

    @Test
    void removingTwiceReturnsFalseTheSecondTime() {
        final RootWidget root = new RootWidget(200, 200);
        final TestWidget child = new TestWidget(transform());
        root.addChild(child);

        assertTrue(root.removeChild(child));
        assertFalse(root.removeChild(child));
    }

    @Test
    void removeChildKeepsTheOrderOfTheRemainingChildren() {
        final RootWidget root = new RootWidget(200, 200);
        final TestWidget a = new TestWidget(transform());
        final TestWidget b = new TestWidget(transform());
        final TestWidget c = new TestWidget(transform());
        root.addChild(a);
        root.addChild(b);
        root.addChild(c);

        root.removeChild(b);

        assertEquals(List.of(a, c), root.getChildren());
    }

    @Test
    void removedChildKeepsItsOwnChildren() {
        final RootWidget root = new RootWidget(200, 200);
        final TestWidget child = new TestWidget(transform());
        final TestWidget grandChild = new TestWidget(transform());
        root.addChild(child);
        child.addChild(grandChild);

        root.removeChild(child);

        assertEquals(List.of(grandChild), child.getChildren());
        assertSame(child, grandChild.getParent());
    }

    @Test
    void removedChildIsNoLongerFoundByHitTest() {
        final RootWidget root = new RootWidget(200, 200);
        final TestWidget button = new TestWidget(transform(), true);
        root.addChild(button);
        assertSame(button, root.hitTest(20, 20));

        root.removeChild(button);

        assertNull(root.hitTest(20, 20));
    }

    @Test
    void removedChildCanBeAddedAgainToTheSameOrAnotherParent() {
        final RootWidget root = new RootWidget(200, 200);
        final TestWidget other = new TestWidget(transform());
        final TestWidget button = new TestWidget(transform(), true);
        root.addChild(other);
        root.addChild(button);

        root.removeChild(button);
        root.addChild(button);
        assertSame(root, button.getParent());
        assertSame(button, root.hitTest(20, 20));

        root.removeChild(button);
        other.addChild(button);
        assertSame(other, button.getParent());
        assertEquals(List.of(button), other.getChildren());
        assertEquals(List.of(other), root.getChildren());
    }

    @Test
    void removedChildIsNoLongerUpdated() {
        final RootWidget root = new RootWidget(200, 200);
        final Recorder child = new Recorder();
        root.addChild(child);
        root.update(0.1f);

        root.removeChild(child);
        root.update(0.2f);

        assertEquals(List.of(0.1f), child.dts);
    }

    @Test
    void rejectsNull() {
        final RootWidget root = new RootWidget(200, 200);

        assertThrows(NullPointerException.class, () -> root.removeChild(null));
    }

    @Test
    void widgetMayRemoveItselfDuringUpdateWithoutException() {
        final RootWidget root = new RootWidget(200, 200);
        final Recorder before = new Recorder();
        final Recorder after = new Recorder();
        final Widget leaver = new TestWidget(transform()) {
            @Override
            protected void updateSelf(final float dt) {
                root.removeChild(this);
            }
        };
        root.addChild(before);
        root.addChild(leaver);
        root.addChild(after);

        assertDoesNotThrow(() -> root.update(0.1f));
        assertEquals(List.of(before, after), root.getChildren());

        // Im nächsten Frame sind alle verbliebenen Widgets wieder vollständig dran.
        root.update(0.2f);
        assertEquals(List.of(0.1f, 0.2f), before.dts);
        assertTrue(after.dts.contains(0.2f));
    }

    /** Merkt sich alle Update-Aufrufe. */
    private static final class Recorder extends Widget {
        final List<Float> dts = new ArrayList<>();

        Recorder() {
            super(transform());
        }

        @Override
        protected void updateSelf(final float dt) {
            dts.add(dt);
        }
    }
}
