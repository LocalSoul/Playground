package dev.localsoul.playground.ui;

import dev.localsoul.playground.math.rect.Rect;
import dev.localsoul.playground.math.rect.RectTransform;
import dev.localsoul.playground.ui.anchor.Anchor;
import dev.localsoul.playground.ui.layout.LayoutStrategy;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Basisklasse der UI-Hierarchie. Jedes Widget besitzt eine optionale Eltern-Widget und
 * beliebig viele Kinder und bildet damit einen Baum (Composition-Pattern).
 *
 * <p>Für die Positionierung ist pro Widget eine {@link RectTransform} zuständig, die die
 * lokalen Ränder relativ zu den Bounds des Eltern-Widgets auflöst (Anker-basiertes Layout).
 * Die Darstellung läuft über den klassischen Swing-/AWT-{@code Graphics2D}-Kontext.</p>
 *
 * <p>Der Lebenszyklus eines Widgets gliedert sich in drei Phasen:</p>
 * <ol>
 *     <li><b>Layout</b> – {@link #relayout()} wendet eine {@link LayoutStrategy} auf die Kinder an.</li>
 *     <li><b>Update</b> – {@link #update(float)} wird pro Frame mit der vergangenen Zeit aufgerufen.</li>
 *     <li><b>Draw</b> – {@link #draw(Graphics2D)} rendert das Widget und anschließend rekursiv alle Kinder.</li>
 * </ol>
 *
 * <p>Nur die Wurzel der Hierarchie (siehe {@link RootWidget}) hat {@code parent == null}; für alle
 * anderen Widgets müssen die Bounds über das Eltern-Widget auflösbar sein.</p>
 */
public abstract class Widget {

    /**
     * Die direkten Kinder in Draw- und HitTest-Reihenfolge (Kinder vor Eltern, hintere zuletzt).
     * Über {@link #getChildren()} wird stets eine unveränderliche Kopie zurückgegeben.
     */
    private final List<Widget> children = new ArrayList<>();
    /**
     * Beschreibt die lokale Position, Größe und Ankerung dieses Widgets.
     * Wird in {@link #getBounds()} gegen die Eltern-Bounds aufgelöst.
     */
    private final RectTransform transform;
    /**
     * Das Eltern-Widget in der Hierarchie. Ist nur bei der Wurzel (RootWidget) {@code null}.
     * Wird beim Hinzufügen über {@link #addChild(Widget)} gesetzt und ist danach final.
     */
    private Widget parent;
    /**
     * Optionale Strategie zur automatischen Anordnung der Kinder (z. B. horizontale/vertikale Reihe).
     * Ist sie {@code null}, hat {@link #relayout()} keine Wirkung.
     */
    private LayoutStrategy layoutStrategy;

    /**
     * Erzeugt ein Widget mit der übergebenen Transformation.
     *
     * @param transform die Anker-/Offset-/Größen-Definition für dieses Widget; darf nicht {@code null} sein
     */
    public Widget(final RectTransform transform) {
        this.transform = Objects.requireNonNull(transform, "transform must not be null");
    }

    /**
     * Rendert dieses Widget. Implementiert das Template-Method-Pattern:
     * Zuerst werden die lokalen Bounds über {@link #getBounds()} aufgelöst und zusammen mit
     * dem Kontext an {@link #drawSelf(Graphics2D, Rect)} (das eigene Aussehen) übergeben,
     * danach werden rekursiv alle Kinder über {@code drawChildren} gezeichnet.
     *
     * <p>Diese Methode ist {@code final}, damit die Reihenfolge „Self → Children"
     * fest garantiert bleibt und keine Unterklasse sie versehentlich bricht.</p>
     *
     * @param g der Grafik-Kontext; der Aufrufer übersetzt das Koordinatensystem vorher nicht –
     *          die Translation in Kind-Koordinaten passiert innerhalb von {@code drawChildren}
     */
    public final void draw(final Graphics2D g) {
        final Rect bounds = getBounds();
        drawSelf(g, bounds);      // <- das überschreibt jede konkrete Widget-Klasse
        drawChildren(g);  // <- das bleibt in der Basisklasse, unverändert
    }

    /**
     * Hook für das eigene Aussehen eines Widgets (die konkrete Widget-Klasse).
     * Wird von {@link #draw(Graphics2D)} vor den Kindern aufgerufen.
     *
     * <p>Die Bounds werden bereits in {@link #draw(Graphics2D)} aufgelöst und als Parameter
     * übergeben – eine konkrete Unterklasse muss {@link #getBounds()} nicht erneut aufrufen.</p>
     *
     * <p>Die Standard-Implementierung ist leer: Ein reiner Container wie ein hierarchisches
     * Panel zeichnet selbst nichts und muss diese Methode nicht überschreiben.</p>
     *
     * @param g      der bereits in lokale Koordinaten übersetzte Grafik-Kontext
     * @param bounds die aufgelösten lokalen Bounds (Breite/Höhe) dieses Widgets
     */
    protected void drawSelf(final Graphics2D g, final Rect bounds) {
        // Default: nichts. Ein reines Container-Panel ohne eigenes Aussehen
        // muss das nicht überschreiben.
    }

    /**
     * Zeichnet alle Kinder rekursiv. Für jedes Kind wird eine Kopie des Grafik-Kontexts erzeugt
     * und um die lokale Position des Kindes verschoben, damit {@code translate()} nicht global
     * auf den Eltern-Kontext wirkt (eigene Kopie je Kind + {@code dispose()} nach dem Zeichnen).
     */
    private void drawChildren(final Graphics2D g) {
        for (final Widget child : children) {
            final Rect bounds = child.getBounds();
            final Graphics2D childG = (Graphics2D) g.create(); // Kopie, damit translate() sich nicht global auswirkt
            childG.translate(bounds.x(), bounds.y());
            child.draw(childG);
            childG.dispose();
        }
    }

    /**
     * Pro-Frame-Update-Hook. Wird von außen (z. B. von der Spiel- oder Render-Schleife)
     * für jedes Widget aufgerufen.
     *
     * @param dt vergangene Zeit seit dem letzten Frame in Sekunden
     */
    public void update(final float dt) {
        // Default: nichts zu tun. Konkrete Widgets überschreiben diesen Hook nach Bedarf.
    }

    /**
     * Eingabe-Hook für Klick-Events. Wird aufgerufen, wenn dieses Widget über
     * {@link #hitTest(float, float)} als getroffenes Widget ermittelt wurde.
     */
    public void onClick() {
        // Default: keine Reaktion.
    }

    /**
     * Eingabe-Hook für Hover-Events. Wird aufgerufen, sobald der Mauszeiger
     * über diesem Widget liegt.
     */
    public void onHover() {
        // Default: keine Reaktion.
    }

    /**
     * Hängt ein Kind-Widget an.
     *
     * <p>Hat das Widget bereits ein anderes Eltern-Widget, wird es dort zuvor entfernt
     * (Reparenting). So bleiben Eltern-Referenz und Kind-Liste immer konsistent – ein
     * Widget besitzt zu jedem Zeitpunkt höchstens ein Eltern-Widget.</p>
     *
     * @param child das anzuhängende Kind-Widget; darf nicht {@code null} sein
     */
    public final void addChild(@Nonnull final Widget child) {
        if (child.parent == this) {
            return; // bereits direktes Kind dieses Widgets – nichts zu tun
        }
        // Reparenting: vorherige Eltern-Beziehung sauber auflösen, sonst bliebe das
        // Widget in der Kind-Liste des alten Parents und würde dort weiter mitgezeichnet.
        if (child.parent != null) {
            child.parent.children.remove(child);
        }
        child.parent = this;
        children.add(child);
    }

    /**
     * Liefert das oberste (zuletzt gezeichnete) Widget, das die übergebene Position abdeckt.
     *
     * <p>Die Prüfung läuft rekursiv von den hinteren zu den vorderen Kindern
     * ({@code z}-Reihenfolge), sodass ein vorne liegendes, überlappendes Kind Vorrang hat.
     * Erst wenn kein Kind trifft, wird die eigene absolute Bounding-Box geprüft.</p>
     *
     * @param x X-Koordinate absolut – im Koordinatensystem der Wurzel/des Canvases,
     *          nicht relativ zum Eltern-Widget
     * @param y Y-Koordinate absolut – im Koordinatensystem der Wurzel/des Canvases,
     *          nicht relativ zum Eltern-Widget
     * @return das getroffene Widget oder {@code null}, wenn kein Widget getroffen wurde
     */
    public final Widget hitTest(final float x, final float y) {
        // Von hinten nach vorne durchgehen, damit das oberste Widget gewinnt.
        for (int i = children.size() - 1; i >= 0; i--) {
            final Widget hit = children.get(i).hitTest(x, y);
            if (hit != null) return hit;
        }
        return getAbsoluteBounds().contains(x, y) ? this : null;
    }

    /**
     * Löst das Layout neu auf: Wendet die gesetzte {@link LayoutStrategy} auf die Kinder an.
     * Ohne gesetzte Strategie passiert nichts.
     */
    public final void relayout() {
        if (layoutStrategy == null) return;
        layoutStrategy.relayout(this, getChildren());
    }

    /**
     * Liefert die Kinder als unveränderliche Kopie der internen Liste.
     * So kann ein Aufrufer die interne {@code ArrayList} weder erweitern noch löschen –
     * die Hierarchie kann nur über {@link #addChild(Widget)} verändert werden.
     *
     * @return unveränderliche Kopie der Kind-Liste
     */
    public final List<Widget> getChildren() {
        return List.copyOf(children); // Kopie der Liste, damit die innere ArrayList nicht manipuliert werden kann
    }

    /**
     * Liefert die Bounds dieses Widgets in lokalen Koordinaten (relativ zum Eltern-Widget),
     * aufgelöst gegen die Breite/Höhe der Eltern-Bounds.
     *
     * <p>Nur {@link RootWidget} überschreibt diese Methode und liefert die absoluten,
     * festgelegten Canvas-Maße. Für alle anderen Widgets darf das Eltern-Widget nie
     * {@code null} sein, sonst schlägt die Auflösung fehl.</p>
     *
     * @return die lokalen Bounds (x, y, Breite, Höhe)
     */
    public Rect getBounds() {
        final Rect parentBounds = parent.getBounds();
        return transform.resolve(parentBounds.width(), parentBounds.height());
    }

    /**
     * Liefert die globalen (szenenweiten) Bounds, indem die lokalen Bounds schrittweise um
     * die Positionen aller Vorfahren verschoben werden. Wird u. a. für das {@link #hitTest(float, float)}
     * benötigt, da die zu prüfenden Koordinaten absolut sind.
     *
     * @return die absoluten Bounds (x, y, Breite, Höhe) im Wurzel-Koordinatensystem
     */
    public final Rect getAbsoluteBounds() {
        final Rect local = getBounds();
        if (parent == null) return local;
        final Rect parentAbs = parent.getAbsoluteBounds();
        return new Rect(parentAbs.x() + local.x(), parentAbs.y() + local.y(), local.width(), local.height());
    }

    /**
     * Liefert das Eltern-Widget.
     *
     * @return das Eltern-Widget; {@code null} nur bei der Hierarchie-Wurzel
     */
    public final Widget getParent() {
        return parent;
    }

    /**
     * Liefert den Anker dieses Widgets (bequemer Zugriff auf die Transforms).
     *
     * @return der aktuell gesetzte {@link Anchor}
     */
    public final Anchor getAnchor() {
        return transform.anchor();
    }

    /**
     * Setzt die Layout-Strategie, mit der die Kinder automatisch angeordnet werden.
     *
     * @param layoutStrategy die zu verwendende Strategie; {@code null} deaktiviert das Auto-Layout
     */
    public final void setLayoutStrategy(final LayoutStrategy layoutStrategy) {
        this.layoutStrategy = layoutStrategy;
    }

    /**
     * Liefert die zugrunde liegende {@link RectTransform} dieses Widgets.
     *
     * @return die sog. Transform-Instanz (Anker, Offsets, Größe)
     */
    public final RectTransform getTransform() {
        return transform;
    }
}