package dev.localsoul.playground.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Vector2fTest {

    @Test
    void defaultConstructorIsZero() {
        final Vector2f v = new Vector2f();
        assertEquals(0f, v.x());
        assertEquals(0f, v.y());
    }

    @Test
    void constructorSetsCoordinates() {
        final Vector2f v = new Vector2f(3.5f, -2f);
        assertEquals(3.5f, v.x());
        assertEquals(-2f, v.y());
    }

    @Test
    void scalarConstructorSetsBothAxes() {
        final Vector2f v = new Vector2f(4f);
        assertEquals(4f, v.x());
        assertEquals(4f, v.y());
    }

    @Test
    void copyConstructorCopiesValues() {
        final Vector2f original = new Vector2f(1f, 2f);
        final Vector2f copy = new Vector2f(original);
        assertNotSame(original, copy);
        assertEquals(original.x(), copy.x());
        assertEquals(original.y(), copy.y());
    }

    @Test
    void setReturnsNewVectorWithoutMutating() {
        final Vector2f v = new Vector2f(1f, 1f);
        final Vector2f result = v.set(5f, 6f);
        assertNotSame(v, result);
        assertEquals(5f, result.x());
        assertEquals(6f, result.y());
        assertEquals(1f, v.x());
        assertEquals(1f, v.y());
    }

    @Test
    void setWithVectorCopiesValues() {
        final Vector2f v = new Vector2f(0f, 0f);
        final Vector2f result = v.set(new Vector2f(7f, 8f));
        assertEquals(7f, result.x());
        assertEquals(8f, result.y());
    }

    @Test
    void add() {
        final Vector2f a = new Vector2f(1f, 2f);
        final Vector2f result = a.add(new Vector2f(3f, 4f));
        assertEquals(4f, result.x());
        assertEquals(6f, result.y());
    }

    @Test
    void sub() {
        final Vector2f a = new Vector2f(5f, 7f);
        final Vector2f result = a.sub(new Vector2f(1f, 2f));
        assertEquals(4f, result.x());
        assertEquals(5f, result.y());
    }

    @Test
    void mul() {
        final Vector2f a = new Vector2f(2f, 3f);
        final Vector2f result = a.mul(new Vector2f(4f, 5f));
        assertEquals(8f, result.x());
        assertEquals(15f, result.y());
    }

    @Test
    void divByVector() {
        final Vector2f a = new Vector2f(10f, 12f);
        final Vector2f result = a.div(new Vector2f(2f, 3f));
        assertEquals(5f, result.x());
        assertEquals(4f, result.y());
    }

    @Test
    void divByScalar() {
        final Vector2f a = new Vector2f(9f, 15f);
        final Vector2f result = a.div(3f);
        assertEquals(3f, result.x());
        assertEquals(5f, result.y());
    }

    @Test
    void divByZeroVectorThrows() {
        final Vector2f a = new Vector2f(1f, 1f);
        assertThrows(RuntimeException.class, () -> a.div(new Vector2f(0f, 1f)));
        assertThrows(RuntimeException.class, () -> a.div(new Vector2f(1f, 0f)));
        assertThrows(RuntimeException.class, () -> a.div(0f));
    }

    @Test
    void settersMutateInPlace() {
        final Vector2f v = new Vector2f();
        v.setX(3f);
        v.setY(4f);
        assertEquals(3f, v.x());
        assertEquals(4f, v.y());
    }
}