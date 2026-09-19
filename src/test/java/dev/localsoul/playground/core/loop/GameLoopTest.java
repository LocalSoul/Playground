package dev.localsoul.playground.core.loop;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prüft die Zeitlogik des {@link GameLoop} mit einer selbst gesteuerten Uhr, ohne Swing und
 * ohne echte Wartezeiten. (Nicht zu verwechseln mit dem manuellen Fenster-Test
 * {@code dev.localsoul.playground.test.GameLoopTest}.)
 */
class GameLoopTest {

    private static final long MS = 1_000_000L;
    private static final long TICK = 50 * MS;        // 20 Hz
    private static final long MAX_FRAME = 250 * MS;  // entspricht GameLoop.MAX_FRAME_NANOS (dort private)

    /** Selbst gesteuerte Uhr: {@code now[0]} wird im Test gesetzt. */
    private final long[] now = {0L};

    private final List<String> events = new ArrayList<>();
    private final List<Double> tickDts = new ArrayList<>();
    private final List<Float> frameDts = new ArrayList<>();
    private final List<Long> catchUps = new ArrayList<>();

    private final GameLoop loop = new GameLoop(() -> now[0], TICK, new LoopListener() {
        @Override
        public void tick(final double dt) {
            events.add("tick");
            tickDts.add(dt);
        }

        @Override
        public void catchUp(final long nanos) {
            events.add("catchUp");
            catchUps.add(nanos);
        }

        @Override
        public void frame(final float dt) {
            events.add("frame");
            frameDts.add(dt);
        }
    });

    private long count(final String event) {
        return events.stream().filter(event::equals).count();
    }

    /** Setzt die Uhr auf den absoluten Zeitpunkt (in ms) und ruft {@code advance()} auf. */
    private void advanceTo(final long ms) {
        now[0] = ms * MS;
        loop.advance();
    }

    @Test
    void firstAdvanceOnlySetsTimeBase() {
        advanceTo(1_000);

        assertTrue(events.isEmpty(), "der erste Aufruf darf keinen Callback auslösen");
    }

    @Test
    void runsFullTicksAndKeepsRemainder() {
        advanceTo(0);
        advanceTo(120);   // 2 Ticks (100 ms), 20 ms bleiben im Akkumulator

        assertEquals(2, count("tick"));
        assertEquals(0.12f, frameDts.get(0), 1e-6f);

        advanceTo(150);   // 20 ms Rest + 30 ms = 50 ms -> genau 1 weiterer Tick
        assertEquals(3, count("tick"));
    }

    @Test
    void everyTickGetsTheSameFixedDt() {
        advanceTo(0);
        advanceTo(200);

        assertEquals(4, tickDts.size());
        for (final double dt : tickDts) {
            assertEquals(0.05, dt, 1e-12);
        }
    }

    @Test
    void frameShorterThanTickAccumulatesAcrossFrames() {
        advanceTo(0);
        advanceTo(16);
        advanceTo(32);
        advanceTo(48);
        assertEquals(0, count("tick"));
        assertEquals(3, count("frame"));   // Frames laufen trotzdem jedes Mal

        advanceTo(64);    // 64 ms >= 50 ms
        assertEquals(1, count("tick"));
    }

    @Test
    void largeGapIsClampedAndExcessGoesToCatchUpBeforeTicks() {
        advanceTo(0);
        advanceTo(10_000); // 10 s Lücke, gedeckelt auf 250 ms

        assertEquals(List.of(10_000 * MS - MAX_FRAME), catchUps);
        assertEquals(5, count("tick")); // 250 ms / 50 ms
        assertEquals(0.25f, frameDts.get(0), 1e-6f);

        // catchUp liegt zeitlich vor den Ticks, frame() kommt zuletzt.
        assertEquals(List.of("catchUp", "tick", "tick", "tick", "tick", "tick", "frame"), events);
    }

    @Test
    void gapExactlyAtLimitIsNotCatchUp() {
        advanceTo(0);
        advanceTo(250);

        assertTrue(catchUps.isEmpty());
        assertEquals(5, count("tick"));
    }

    @Test
    void gapJustOverLimitReportsOnlyTheExcess() {
        advanceTo(0);
        advanceTo(251);

        assertEquals(List.of(1 * MS), catchUps);
    }

    @Test
    void noTimeIsLostOrInvented() {
        // Pseudo-zufällige Frame-Abstände: meist kurz, gelegentlich lange Lücken.
        // Invariante nach jedem Aufruf: vergangene Zeit = Ticks * Schritt + catchUp + Rest im Akkumulator,
        // und der Rest ist immer kleiner als ein Schritt.
        final Random random = new Random(42);
        long elapsed = 0;
        advanceTo(0);

        for (int i = 0; i < 2_000; i++) {
            final long stepMs = random.nextInt(20) == 0
                    ? 300 + random.nextInt(5_000)   // Lücke über dem Limit
                    : 1 + random.nextInt(40);       // normaler Frame
            elapsed += stepMs * MS;
            now[0] = elapsed;
            loop.advance();

            final long caughtUp = catchUps.stream().mapToLong(Long::longValue).sum();
            final long remainder = elapsed - count("tick") * TICK - caughtUp;
            assertTrue(remainder >= 0 && remainder < TICK,
                    "Rest im Akkumulator muss in [0, Schritt) liegen, war " + remainder + " ns nach Aufruf " + i);
        }
    }
}
