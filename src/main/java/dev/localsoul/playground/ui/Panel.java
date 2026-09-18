package dev.localsoul.playground.ui;

import dev.localsoul.playground.math.rect.Rect;
import dev.localsoul.playground.math.rect.RectTransform;
import dev.localsoul.playground.ui.slice.NineSlice;
import dev.localsoul.playground.ui.slice.NineSliceRenderer;

import java.awt.*;

public class Panel extends Widget {

    private final NineSlice nineSlice;

    public Panel(final RectTransform transform, final NineSlice nineSlice) {
        super(transform);
        this.nineSlice = nineSlice;
    }

    @Override
    protected void drawSelf(final Graphics2D g) {
        final Rect b = getBounds();
        NineSliceRenderer.draw(g, nineSlice, (int) b.width(), (int) b.height());
    }


}
