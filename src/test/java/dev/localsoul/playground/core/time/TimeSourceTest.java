package dev.localsoul.playground.core.time;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class TimeSourceTest {

    @Test
    void systemSourceFollowsTheSystemClock() {
        final Instant before = Instant.now();
        final Instant value = TimeSource.system().now();
        final Instant after = Instant.now();

        assertFalse(value.isBefore(before));
        assertFalse(value.isAfter(after));
    }

    @Test
    void lambdaCanControlTimeInTests() {
        final long[] millis = {1_000L};
        final TimeSource source = () -> Instant.ofEpochMilli(millis[0]);

        assertEquals(Instant.ofEpochMilli(1_000L), source.now());

        millis[0] += Duration.ofMinutes(1).toMillis();
        assertEquals(Instant.ofEpochMilli(61_000L), source.now());
    }
}
