package dev.localsoul.playground.ui;

import dev.localsoul.playground.core.math.rect.RectTransform;

/**
 * Schlichtes, konfigurierbares Widget für Tests. Standardmäßig nicht interaktabel;
 * über den Zweiparameter-Konstruktor lässt sich ein interaktables Exemplar erzeugen.
 */
public class TestWidget extends Widget {

    public TestWidget(final RectTransform transform) {
        super(transform);
    }

    public TestWidget(final RectTransform transform, final boolean interactable) {
        super(transform, interactable);
    }
}