package dev.localsoul.playground.math.rect;

import dev.localsoul.playground.math.Vector2f;
import dev.localsoul.playground.ui.anchor.Anchor;

import javax.annotation.Nonnull;

public record RectTransform(Anchor anchor, Vector2f offsetMin, Vector2f offsetMax, Vector2f size) {

    public @Nonnull Rect resolve(final float parentWidth, final float parentHeight) {
        final float x1;
        final float x2;
        final float y1;
        final float y2;

        // X-Achse
        if (anchor.min().x() == anchor.max().x()) {
            // nicht gestreckt: offsetMin.x ist die Position, size gibt die Breite
            x1 = anchor.min().x() * parentWidth + offsetMin.x();
            x2 = x1 + size.x();
        } else {
            // gestreckt: beide Ränder werden vom jeweiligen Anker-Punkt aus versetzt
            x1 = anchor.min().x() * parentWidth + offsetMin.x();
            x2 = anchor.max().x() * parentWidth + offsetMax.x();
        }

        // Y-Achse, analog
        if (anchor.min().y() == anchor.max().y()) {
            y1 = anchor.min().y() * parentHeight + offsetMin.y();
            y2 = y1 + size.y();
        } else {
            y1 = anchor.min().y() * parentHeight + offsetMin.y();
            y2 = anchor.max().y() * parentHeight + offsetMax.y();
        }


        return new Rect(x1, y1, x2 - x1, y2 - y1);
    }
}
