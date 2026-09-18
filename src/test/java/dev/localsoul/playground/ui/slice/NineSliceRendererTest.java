package dev.localsoul.playground.ui.slice;

import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NineSliceRendererTest {

    private static final int SIZE = 48;
    private static final int BORDER = 12;

    private static final Color TOP_LEFT_COLOR = new Color(255, 0, 0);
    private static final Color TOP_RIGHT_COLOR = new Color(0, 255, 0);
    private static final Color BOTTOM_LEFT_COLOR = new Color(0, 0, 255);
    private static final Color BOTTOM_RIGHT_COLOR = new Color(255, 255, 0);
    private static final Color CENTER_COLOR = new Color(128, 128, 128);

    private NineSlice texture() {
        final BufferedImage image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = image.createGraphics();
        g.setColor(CENTER_COLOR);
        g.fillRect(0, 0, SIZE, SIZE);
        g.setColor(TOP_LEFT_COLOR);
        g.fillRect(0, 0, BORDER, BORDER);
        g.setColor(TOP_RIGHT_COLOR);
        g.fillRect(SIZE - BORDER, 0, BORDER, BORDER);
        g.setColor(BOTTOM_LEFT_COLOR);
        g.fillRect(0, SIZE - BORDER, BORDER, BORDER);
        g.setColor(BOTTOM_RIGHT_COLOR);
        g.fillRect(SIZE - BORDER, SIZE - BORDER, BORDER, BORDER);
        g.dispose();
        return new NineSlice(image, BORDER, BORDER, BORDER, BORDER);
    }

    private int rgb(final BufferedImage image, final int x, final int y) {
        return image.getRGB(x, y);
    }

    @Test
    void cornersAreCopiedUnscaled() {
        final BufferedImage target = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = target.createGraphics();

        NineSliceRenderer.draw(g, texture(), SIZE, SIZE);
        g.dispose();

        assertEquals(TOP_LEFT_COLOR.getRGB(), rgb(target, 1, 1));
        assertEquals(TOP_RIGHT_COLOR.getRGB(), rgb(target, SIZE - 2, 1));
        assertEquals(BOTTOM_LEFT_COLOR.getRGB(), rgb(target, 1, SIZE - 2));
        assertEquals(BOTTOM_RIGHT_COLOR.getRGB(), rgb(target, SIZE - 2, SIZE - 2));
    }

    @Test
    void centerAndEdgesDrawAtIdentity() {
        final BufferedImage target = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = target.createGraphics();

        NineSliceRenderer.draw(g, texture(), SIZE, SIZE);
        g.dispose();

        assertEquals(CENTER_COLOR.getRGB(), rgb(target, SIZE / 2, SIZE / 2));
        assertEquals(CENTER_COLOR.getRGB(), rgb(target, BORDER, BORDER / 2));
        assertEquals(CENTER_COLOR.getRGB(), rgb(target, SIZE - BORDER - 1, BORDER / 2));
        assertEquals(CENTER_COLOR.getRGB(), rgb(target, BORDER / 2, BORDER));
        assertEquals(CENTER_COLOR.getRGB(), rgb(target, BORDER / 2, SIZE - BORDER - 1));
    }

    @Test
    void centerAndEdgesStretchToLargerTarget() {
        final int targetSize = SIZE * 2;
        final BufferedImage target = new BufferedImage(targetSize, targetSize, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = target.createGraphics();

        NineSliceRenderer.draw(g, texture(), targetSize, targetSize);
        g.dispose();

        assertEquals(TOP_LEFT_COLOR.getRGB(), rgb(target, 1, 1));
        assertEquals(BOTTOM_RIGHT_COLOR.getRGB(), rgb(target, targetSize - 2, targetSize - 2));
        assertEquals(CENTER_COLOR.getRGB(), rgb(target, targetSize / 2, targetSize / 2));
        assertEquals(CENTER_COLOR.getRGB(), rgb(target, targetSize / 2, BORDER / 2));
        assertEquals(CENTER_COLOR.getRGB(), rgb(target, targetSize / 2, targetSize - BORDER - 1));
    }

    @Test
    void drawsTargetSmallerThanBordersWithoutException() {
        final BufferedImage target = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = target.createGraphics();

        NineSliceRenderer.draw(g, texture(), 8, 8);
        g.dispose();
    }
}