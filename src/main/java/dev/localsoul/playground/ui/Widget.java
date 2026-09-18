package dev.localsoul.playground.ui;

import dev.localsoul.playground.math.rect.Rect;
import dev.localsoul.playground.math.rect.RectTransform;
import dev.localsoul.playground.ui.anchor.Anchor;
import dev.localsoul.playground.ui.layout.LayoutStrategy;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public abstract class Widget {

    private final List<Widget> children = new ArrayList<>();
    private final RectTransform transform;
    private Widget parent;
    private LayoutStrategy layoutStrategy;

    public Widget(final RectTransform transform) {
        this.transform = transform;
    }

    public final void draw(final Graphics2D g) {
        drawSelf(g);      // <- das überschreibt jede konkrete Widget-Klasse
        drawChildren(g);  // <- das bleibt in der Basisklasse, unverändert
    }

    protected void drawSelf(final Graphics2D g) {
        // Default: nichts. Ein reines Container-Panel ohne eigenes Aussehen
        // muss das nicht überschreiben.
    }

    private void drawChildren(final Graphics2D g) {
        for (final Widget child : children) {
            final Rect bounds = child.getBounds();
            final Graphics2D childG = (Graphics2D) g.create(); // Kopie, damit translate() sich nicht global auswirkt
            childG.translate(bounds.x(), bounds.y());
            child.draw(childG);
            childG.dispose();
        }
    }

    public void update(final float dt) {
    }

    public void onClick() {
    }

    public void onHover() {
    }

    public final void addChild(@Nonnull final Widget child) {
        child.parent = this;
        children.add(child);
    }


    /**
     *
     * @param x relative to parent position
     * @param y relative to parent position
     * @return return the hit widget. return null if no hit was found
     */
    public final Widget hitTest(final float x, final float y) {
        for (int i = children.size() - 1; i >= 0; i--) {
            final Widget hit = children.get(i).hitTest(x, y);
            if (hit != null) return hit;
        }
        return getAbsoluteBounds().contains(x, y) ? this : null;
    }

    public final void relayout() {
        if (layoutStrategy == null) return;
        layoutStrategy.relayout(this, getChildren());
    }

    public final List<Widget> getChildren() {
        return List.copyOf(children); //kopie von liste damit children nicht geändert werden können, weil arraylist
    }

    public Rect getBounds() {
        final Rect parentBounds = parent.getBounds();
        return transform.resolve(parentBounds.width(), parentBounds.height());
    }

    public final Rect getAbsoluteBounds() {
        final Rect local = getBounds();
        if (parent == null) return local;
        final Rect parentAbs = parent.getAbsoluteBounds();
        return new Rect(parentAbs.x() + local.x(), parentAbs.y() + local.y(), local.width(), local.height());
    }

    public final Widget getParent() {
        return parent;
    }

    public final Anchor getAnchor() {
        return transform.anchor();
    }

    public final void setLayoutStrategy(final LayoutStrategy layoutStrategy) {
        this.layoutStrategy = layoutStrategy;
    }

    public final RectTransform getTransform() {
        return transform;
    }
}
