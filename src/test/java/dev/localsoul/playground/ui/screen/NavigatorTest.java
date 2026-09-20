package dev.localsoul.playground.ui.screen;

import dev.localsoul.playground.core.math.Vector2f;
import dev.localsoul.playground.core.math.rect.RectTransform;
import dev.localsoul.playground.ui.RootWidget;
import dev.localsoul.playground.ui.anchor.Anchor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NavigatorTest {

    private enum Tab {QUESTS, CHARACTER, SETTINGS}

    private final List<String> log = new ArrayList<>();
    private RootWidget root;
    private ScreenHost host;
    private Navigator<Tab> navigator;
    private Screen quests;
    private Screen character;

    private final class NamedScreen extends Screen {
        private final String name;

        NamedScreen(final String name) {
            this.name = name;
        }

        @Override
        protected void onShow() {
            log.add(name + ":show");
        }

        @Override
        protected void onHide() {
            log.add(name + ":hide");
        }
    }

    @BeforeEach
    void setUp() {
        root = new RootWidget(800, 600);
        host = new ScreenHost(new RectTransform(Anchor.STRETCH, new Vector2f(), new Vector2f(), new Vector2f()));
        root.addChild(host);

        quests = new NamedScreen("quests");
        character = new NamedScreen("character");
        navigator = new Navigator<Tab>(host)
                .register(Tab.QUESTS, quests)
                .register(Tab.CHARACTER, character);
    }

    @Test
    void nothingIsShownBeforeTheFirstNavigation() {
        root.update(0.016f);

        assertNull(navigator.current());
        assertNull(host.current());
        assertFalse(navigator.isCurrent(Tab.QUESTS));
    }

    @Test
    void navigateShowsTheRegisteredScreenOnTheNextUpdate() {
        navigator.navigate(Tab.QUESTS);
        root.update(0.016f);

        assertSame(quests, host.current());
        assertEquals(Tab.QUESTS, navigator.current());
        assertTrue(navigator.isCurrent(Tab.QUESTS));
        assertFalse(navigator.isCurrent(Tab.CHARACTER));
    }

    @Test
    void currentIsUpToDateImmediatelyWhileTheHostFollowsInTheNextUpdate() {
        navigator.navigate(Tab.QUESTS);
        root.update(0.016f);

        navigator.navigate(Tab.CHARACTER);

        assertTrue(navigator.isCurrent(Tab.CHARACTER), "der Navigator kennt das Ziel sofort");
        assertSame(quests, host.current(), "der Host folgt erst zu Beginn des nächsten Updates");

        root.update(0.016f);
        assertSame(character, host.current());
    }

    @Test
    void navigatingBackAndForthRunsTheHooksInOrder() {
        navigator.navigate(Tab.QUESTS);
        root.update(0.016f);
        navigator.navigate(Tab.CHARACTER);
        root.update(0.016f);
        navigator.navigate(Tab.QUESTS);
        root.update(0.016f);

        assertEquals(List.of("quests:show", "quests:hide", "character:show", "character:hide", "quests:show"), log);
    }

    @Test
    void navigatingToTheCurrentIdAgainDoesNotRetriggerTheHooks() {
        navigator.navigate(Tab.QUESTS);
        root.update(0.016f);
        log.clear();

        navigator.navigate(Tab.QUESTS);
        root.update(0.016f);

        assertTrue(log.isEmpty());
    }

    @Test
    void lastNavigationWinsWhenSeveralHappenBeforeAnUpdate() {
        navigator.navigate(Tab.QUESTS);
        navigator.navigate(Tab.CHARACTER);
        navigator.navigate(Tab.QUESTS);
        root.update(0.016f);

        assertEquals(Tab.QUESTS, navigator.current());
        assertSame(quests, host.current());
        assertEquals(List.of("quests:show"), log);
    }

    @Test
    void navigateToUnknownIdThrowsAndKeepsTheCurrentScreen() {
        navigator.navigate(Tab.QUESTS);
        root.update(0.016f);

        assertThrows(IllegalArgumentException.class, () -> navigator.navigate(Tab.SETTINGS));

        assertEquals(Tab.QUESTS, navigator.current());
        assertSame(quests, host.current());
    }

    @Test
    void registerRejectsDuplicateIdAndScreenUsedTwice() {
        assertThrows(IllegalArgumentException.class, () -> navigator.register(Tab.QUESTS, new NamedScreen("other")));
        assertThrows(IllegalArgumentException.class, () -> navigator.register(Tab.SETTINGS, quests));
    }

    @Test
    void registerAndNavigateRejectNull() {
        assertThrows(NullPointerException.class, () -> navigator.register(null, new NamedScreen("x")));
        assertThrows(NullPointerException.class, () -> navigator.register(Tab.SETTINGS, null));
        assertThrows(NullPointerException.class, () -> navigator.navigate(null));
        assertThrows(NullPointerException.class, () -> new Navigator<Tab>(null));
    }

    @Test
    void registerReturnsTheSameNavigatorForChaining() {
        assertSame(navigator, navigator.register(Tab.SETTINGS, new NamedScreen("settings")));
    }
}
