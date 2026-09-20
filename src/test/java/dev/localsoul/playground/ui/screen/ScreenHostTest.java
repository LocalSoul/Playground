package dev.localsoul.playground.ui.screen;

import dev.localsoul.playground.core.input.InputDispatcher;
import dev.localsoul.playground.core.math.Vector2f;
import dev.localsoul.playground.core.math.rect.Rect;
import dev.localsoul.playground.core.math.rect.RectTransform;
import dev.localsoul.playground.ui.RootWidget;
import dev.localsoul.playground.ui.TestWidget;
import dev.localsoul.playground.ui.anchor.Anchor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ScreenHostTest {

    private static final float TOP_BAR = 56f;
    private static final float NAV_BAR = 72f;

    private final List<String> log = new ArrayList<>();
    private RootWidget root;
    private ScreenHost host;

    @BeforeEach
    void setUp() {
        root = new RootWidget(1280, 720);
        host = new ScreenHost(new RectTransform(Anchor.STRETCH,
                new Vector2f(0, TOP_BAR), new Vector2f(0, -NAV_BAR), new Vector2f()));
        root.addChild(host);
    }

    /**
     * Ein Screen, der seine Hooks und Updates protokolliert.
     */
    private final class RecordingScreen extends Screen {
        final List<Float> dts = new ArrayList<>();
        private final String name;
        boolean attachedInOnShow;
        boolean attachedInOnHide;

        RecordingScreen(final String name) {
            this.name = name;
        }

        @Override
        protected void onShow() {
            attachedInOnShow = getParent() != null;
            log.add(name + ":show");
        }

        @Override
        protected void onHide() {
            attachedInOnHide = getParent() != null;
            log.add(name + ":hide");
        }

        @Override
        protected void updateSelf(final float dt) {
            dts.add(dt);
        }
    }

    @Test
    void showIsDeferredUntilTheNextUpdate() {
        final Screen a = new RecordingScreen("a");

        host.show(a);

        assertNull(host.current());
        assertTrue(host.getChildren().isEmpty());
        assertTrue(log.isEmpty());

        root.update(0.016f);

        assertSame(a, host.current());
        assertEquals(List.of(a), host.getChildren());
        assertSame(host, a.getParent());
    }

    @Test
    void onShowRunsAfterTheScreenIsAttached() {
        final RecordingScreen a = new RecordingScreen("a");
        host.show(a);

        root.update(0.016f);

        assertTrue(a.attachedInOnShow, "in onShow müssen Eltern-Bezug und Bounds schon gültig sein");
    }

    @Test
    void switchHidesOldThenShowsNewAndDetachesTheOld() {
        final RecordingScreen a = new RecordingScreen("a");
        final RecordingScreen b = new RecordingScreen("b");
        host.show(a);
        root.update(0.016f);

        host.show(b);
        root.update(0.016f);

        assertEquals(List.of("a:show", "a:hide", "b:show"), log);
        assertTrue(a.attachedInOnHide, "onHide läuft, solange der alte Screen noch im Baum hängt");
        assertNull(a.getParent());
        assertEquals(List.of(b), host.getChildren());
        assertSame(b, host.current());
    }

    @Test
    void showingTheCurrentScreenAgainDoesNothing() {
        final Screen a = new RecordingScreen("a");
        host.show(a);
        root.update(0.016f);
        log.clear();

        host.show(a);
        root.update(0.016f);

        assertTrue(log.isEmpty());
        assertEquals(List.of(a), host.getChildren());
    }

    @Test
    void lastRequestWinsAndSkippedScreensNeverShow() {
        final Screen a = new RecordingScreen("a");
        final Screen b = new RecordingScreen("b");

        host.show(a);
        host.show(b);
        root.update(0.016f);

        assertEquals(List.of("b:show"), log);
        assertNull(a.getParent());
        assertSame(b, host.current());
    }

    @Test
    void requestingTheCurrentScreenCancelsAPendingSwitch() {
        final Screen a = new RecordingScreen("a");
        final Screen b = new RecordingScreen("b");
        host.show(a);
        root.update(0.016f);
        log.clear();

        host.show(b);
        host.show(a);
        root.update(0.016f);

        assertTrue(log.isEmpty());
        assertSame(a, host.current());
    }

    @Test
    void aHiddenScreenCanBeShownAgainAndKeepsItsState() {
        final RecordingScreen a = new RecordingScreen("a");
        final RecordingScreen b = new RecordingScreen("b");
        final TestWidget content = new TestWidget(
                new RectTransform(Anchor.TOP_LEFT, new Vector2f(), new Vector2f(), new Vector2f(10, 10)));
        a.addChild(content);

        host.show(a);
        root.update(0.016f);
        host.show(b);
        root.update(0.016f);
        host.show(a);
        root.update(0.016f);

        assertEquals(List.of("a:show", "a:hide", "b:show", "b:hide", "a:show"), log);
        assertSame(host, a.getParent());
        assertEquals(List.of(content), a.getChildren(), "der Screen bleibt beim Ausblenden unverändert");
    }

    @Test
    void newScreenIsUpdatedInTheSamePassAsItIsShown() {
        final RecordingScreen a = new RecordingScreen("a");
        host.show(a);

        root.update(0.1f);

        assertEquals(List.of(0.1f), a.dts);
    }

    @Test
    void hiddenScreenIsNoLongerUpdated() {
        final RecordingScreen a = new RecordingScreen("a");
        final RecordingScreen b = new RecordingScreen("b");
        host.show(a);
        root.update(0.1f);
        host.show(b);
        root.update(0.2f);

        root.update(0.3f);

        assertEquals(List.of(0.1f), a.dts);
        assertEquals(List.of(0.2f, 0.3f), b.dts);
    }

    @Test
    void requestFromInsideTheCurrentScreenIsSafeAndAppliedInTheNextFrame() {
        final RecordingScreen b = new RecordingScreen("b");
        final Screen a = new RecordingScreen("a");
        final TestWidget trigger = new TestWidget(
                new RectTransform(Anchor.TOP_LEFT, new Vector2f(), new Vector2f(), new Vector2f(10, 10))) {
            @Override
            protected void updateSelf(final float dt) {
                host.show(b);   // mitten im Update-Durchlauf, aus einem Kind des aktuellen Screens
            }
        };
        a.addChild(trigger);
        host.show(a);
        root.update(0.016f);

        assertDoesNotThrow(() -> root.update(0.016f));
        assertSame(b, host.current(), "der Wechsel wird zu Beginn des nächsten Updates angewendet");
    }

    @Test
    void screenFillsTheHostBetweenTheBars() {
        final Screen a = new RecordingScreen("a");
        host.show(a);
        root.update(0.016f);

        final Rect expected = new Rect(0f, TOP_BAR, 1280f, 720f - TOP_BAR - NAV_BAR);
        assertEquals(expected, host.getAbsoluteBounds());
        assertEquals(expected, a.getAbsoluteBounds());
    }

    @Test
    void rejectsNullScreen() {
        assertThrows(NullPointerException.class, () -> host.show(null));
    }

    @Test
    void rejectsAScreenThatBelongsToAnotherParent() {
        final Screen a = new RecordingScreen("a");
        final TestWidget otherParent = new TestWidget(
                new RectTransform(Anchor.TOP_LEFT, new Vector2f(), new Vector2f(), new Vector2f(10, 10)));
        root.addChild(otherParent);
        otherParent.addChild(a);

        assertThrows(IllegalArgumentException.class, () -> host.show(a));
        assertSame(otherParent, a.getParent(), "der Screen bleibt, wo er war");
    }

    @Test
    void clicksReachOnlyTheWidgetsOfTheCurrentScreen() {
        final AtomicInteger clicksInA = new AtomicInteger();
        final AtomicInteger clicksInB = new AtomicInteger();
        final RectTransform sameSpot = new RectTransform(
                Anchor.TOP_LEFT, new Vector2f(100, 100), new Vector2f(), new Vector2f(80, 40));

        final Screen a = new RecordingScreen("a");
        final TestWidget buttonA = new TestWidget(sameSpot, true);
        buttonA.setClickCallback(clicksInA::incrementAndGet);
        a.addChild(buttonA);

        final Screen b = new RecordingScreen("b");
        final TestWidget buttonB = new TestWidget(sameSpot, true);
        buttonB.setClickCallback(clicksInB::incrementAndGet);
        b.addChild(buttonB);

        final InputDispatcher input = new InputDispatcher(root);
        // Punkt im Button: 100 px vom Host-Rand plus dessen Abstand nach oben (TOP_BAR).
        final float x = 120f;
        final float y = TOP_BAR + 110f;

        host.show(a);
        root.update(0.016f);
        input.pointerPressed(x, y);
        input.pointerReleased(x, y);

        host.show(b);
        root.update(0.016f);
        input.pointerPressed(x, y);
        input.pointerReleased(x, y);

        assertEquals(1, clicksInA.get());
        assertEquals(1, clicksInB.get());
    }
}
