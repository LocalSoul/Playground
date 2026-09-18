package dev.localsoul.playground.ui;

import dev.localsoul.playground.math.Vector2f;
import dev.localsoul.playground.math.rect.Rect;
import dev.localsoul.playground.math.rect.RectTransform;
import dev.localsoul.playground.ui.anchor.Anchor;

public class RootWidget extends Widget {

    private float width;
    private float height;

    public RootWidget(final float width, final float height) {
        super(new RectTransform(Anchor.TOP_LEFT, new Vector2f(), new Vector2f(), new Vector2f(width, height)));

        this.width = width;
        this.height = height;
    }

    public void resize(final float width, final float height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public Rect getBounds() {
        return new Rect(0, 0, width, height);
    }
}
