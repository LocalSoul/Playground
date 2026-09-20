package dev.localsoul.playground.ui;

import dev.localsoul.playground.core.math.Vector2f;
import dev.localsoul.playground.core.math.rect.RectTransform;
import dev.localsoul.playground.ui.anchor.Anchor;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Prüft, dass {@link Widget#update(float)} die gesamte Hierarchie in der richtigen Reihenfolge erreicht. */
class WidgetUpdateTest {

    private static RectTransform transform() {
        return new RectTransform(Anchor.TOP_LEFT, new Vector2f(), new Vector2f(), new Vector2f(10, 10));
    }

    @Test
    void updateReachesWholeHierarchyWithSameDt() {
        final RootWidget root = new RootWidget(100, 100);
        final Recorder child = new Recorder("child", null);
        final Recorder grandChild = new Recorder("grandChild", null);
        root.addChild(child);
        child.addChild(grandChild);

        root.update(0.016f);

        assertEquals(List.of(0.016f), child.dts);
        assertEquals(List.of(0.016f), grandChild.dts);
    }

    @Test
    void updateRunsSelfBeforeChildrenAndKeepsSiblingOrder() {
        final List<String> order = new ArrayList<>();
        final RootWidget root = new RootWidget(100, 100);
        final Recorder parent = new Recorder("parent", order);
        final Recorder firstChild = new Recorder("firstChild", order);
        final Recorder secondChild = new Recorder("secondChild", order);
        root.addChild(parent);
        parent.addChild(firstChild);
        parent.addChild(secondChild);

        root.update(1f);

        assertEquals(List.of("parent", "firstChild", "secondChild"), order);
    }

    @Test
    void widgetMayAddChildToItselfDuringUpdateAndItIsUpdatedInSamePass() {
        final RootWidget root = new RootWidget(100, 100);
        final Recorder spawned = new Recorder("spawned", null);
        final Widget spawner = new TestWidget(transform()) {
            @Override
            protected void updateSelf(final float dt) {
                if (getChildren().isEmpty()) addChild(spawned);
            }
        };
        root.addChild(spawner);

        assertDoesNotThrow(() -> root.update(0.5f));
        assertEquals(List.of(0.5f), spawned.dts);
    }

    @Test
    void widgetAddedToAncestorDuringUpdateIsUpdatedFromNextFrame() {
        final RootWidget root = new RootWidget(100, 100);
        final Recorder late = new Recorder("late", null);
        final Widget spawner = new TestWidget(transform()) {
            private boolean spawned;

            @Override
            protected void updateSelf(final float dt) {
                if (!spawned) {
                    spawned = true;
                    root.addChild(late);   // die Wurzel iteriert gerade über ihre Kinder
                }
            }
        };
        root.addChild(spawner);

        assertDoesNotThrow(() -> root.update(0.1f));
        assertTrue(late.dts.isEmpty(), "im selben Durchlauf noch nicht aktualisiert");
        assertEquals(2, root.getChildren().size());

        root.update(0.2f);
        assertEquals(List.of(0.2f), late.dts);
    }

    @Test
    void widgetThatAddsASiblingInEveryUpdateCannotExtendOnePassEndlessly() {
        final RootWidget root = new RootWidget(100, 100);
        root.addChild(new Spawner(root));

        // Jeder Spawner hängt beim Update einen weiteren an die Wurzel. Pro Durchlauf darf
        // trotzdem nur die Momentaufnahme der Kinder aktualisiert werden.
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> root.update(0.1f));
        assertEquals(2, root.getChildren().size());

        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> root.update(0.1f));
        assertEquals(4, root.getChildren().size());
    }

    /** Hängt bei jedem Update ein neues Exemplar seiner selbst an das Ziel-Widget. */
    private static final class Spawner extends Widget {
        private final Widget target;

        Spawner(final Widget target) {
            super(transform());
            this.target = target;
        }

        @Override
        protected void updateSelf(final float dt) {
            target.addChild(new Spawner(target));
        }
    }

    /** Merkt sich alle Update-Aufrufe und trägt optional seinen Namen in ein gemeinsames Protokoll ein. */
    private static final class Recorder extends Widget {
        final String name;
        final List<String> log;
        final List<Float> dts = new ArrayList<>();

        Recorder(final String name, final List<String> log) {
            super(transform());
            this.name = name;
            this.log = log;
        }

        @Override
        protected void updateSelf(final float dt) {
            dts.add(dt);
            if (log != null) log.add(name);
        }
    }
}
