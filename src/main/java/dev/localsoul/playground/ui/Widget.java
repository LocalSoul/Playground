package dev.localsoul.playground.ui;

import dev.localsoul.playground.core.math.rect.Rect;
import dev.localsoul.playground.core.math.rect.RectTransform;
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
 *     <li><b>Update</b> – {@link #update(float)} wird pro Frame mit der vergangenen Zeit aufgerufen.
 *          Es ist ein Template-Method-Pattern analog zu {@link #draw(Graphics2D)}: Das Widget aktualisiert
 *          zuerst sich selbst über den {@link #updateSelf(float)}-Hook und danach rekursiv alle Kinder –
 *          ein einziger Aufruf auf der Wurzel genügt, um die gesamte Hierarchie zu aktualisieren.</li>
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
     * Ob dieses Widget selbst als Eingabeziel (Hover/Klick) infrage kommt. Reine Container wie
     * {@link Panel} oder {@link RootWidget} sind {@code false} und werden von {@link #hitTest(float, float)}
     * übergangen – sie dienen nur als Träger für interaktable Kinder.
     */
    private final boolean interactable;
    /**
     * Optionale Aktion, die bei einem Klick (drücken und an derselben Stelle loslassen) ausgeführt wird.
     * Wird von {@link #onClick()} aufgerufen; {@code null} bedeutet „keine Reaktion möglich“.
     * Siehe {@link #setClickCallback(Runnable)}.
     */
    private Runnable clickCallback;
    /**
     * Optionale Aktion beim Eintritt des Mauszeigers. Wird von {@link #onHoverEnter()} aufgerufen;
     * {@code null} bedeutet „keine Reaktion“. Siehe {@link #setHoverEnterCallback(Runnable)}.
     */
    private Runnable hoverEnterCallback;
    /**
     * Optionale Aktion beim Verlassen des Mauszeigers. Wird von {@link #onHoverExit()} aufgerufen;
     * {@code null} bedeutet „keine Reaktion“. Siehe {@link #setHoverExitCallback(Runnable)}.
     */
    private Runnable hoverExitCallback;
    /**
     * Ob die Maustaste derzeit über diesem Widget gedrückt ist (Pressed-Zustand). Wird vom
     * {@code InputDispatcher} beim Drücken/Loslassen gesetzt und kann von {@link #drawSelf(Graphics2D, Rect)}
     * für eine visuelle Rückmeldung (z. B. abgedunkelter Button) verwendet werden.
     */
    private boolean mousePressed;

    /**
     * Vollständiger Konstruktor für die Unterklassen.
     *
     * @param transform   die Anker-/Offset-/Größen-Definition für dieses Widget; darf nicht {@code null} sein
     * @param interactable {@code true}, wenn das Widget selbst Eingaben (Hover/Klick) empfangen soll;
     *                     {@code false} für reine Container (z. B. {@link Panel}, {@link RootWidget})
     */
    public Widget(final RectTransform transform, final boolean interactable) {
        this.transform = Objects.requireNonNull(transform, "transform must not be null");
        this.interactable = interactable;
    }

    /**
     * Convenience-Konstruktor für nicht interaktable Widgets.
     *
     * <p>Container, die selbst keine Eingaben empfangen, können auf den zweiten Parameter
     * verzichten. Interaktive Widgets (z. B. {@link Button}) übergeben dagegen explizit
     * {@code interactable = true} über den zweiparametrigen Konstruktor.</p>
     *
     * @param transform die Anker-/Offset-/Größen-Definition für dieses Widget; darf nicht {@code null} sein
     */
    protected Widget(final RectTransform transform) {
        this(transform, false);
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
     * Pro-Frame-Update. Implementiert das Template-Method-Pattern analog zu {@link #draw(Graphics2D)}:
     * Zuerst wird die eigene Logik über den {@link #updateSelf(float)}-Hook ausgeführt, danach werden
     * rekursiv alle Kinder über {@code child.update(dt)} aktualisiert.
     *
     * <p>Diese Methode ist {@code final}, damit die Reihenfolge „Self → Children" fest garantiert
     * bleibt und keine Unterklasse sie versehentlich bricht. Ein einziger Aufruf auf der Wurzel
     * reicht aus, um pro Frame die gesamte Hierarchie zu aktualisieren.</p>
     *
     * <p>Widgets dürfen sich während ihres Updates verändern, auch an Vorfahren: Beispielsweise
     * kann ein Widget einen Floating-Text an die Wurzel hängen, während diese gerade ihre Kinder
     * aktualisiert. Ein während des Durchlaufs hinzugefügtes Kind wird <b>erst im nächsten Frame</b>
     * erstmals aktualisiert. Fügt ein Widget sich in {@code updateSelf} eigene Kinder hinzu, werden
     * diese dagegen noch im selben Durchlauf aktualisiert, weil sie vor der Kinder-Schleife
     * existieren. Ein Widget, das in jedem Update ein Geschwister anhängt, kann den Durchlauf so
     * nicht endlos verlängern.</p>
     *
     * @param dt vergangene Zeit seit dem letzten Frame in Sekunden
     */
    public final void update(final float dt) {
        updateSelf(dt);  // <- das überschreibt jede konkrete Widget-Klasse
        // Index-Schleife über eine Momentaufnahme der Kinderzahl statt for-each: Ein for-each würde
        // bei addChild() auf diesem Widget (z. B. durch ein Kind) eine ConcurrentModificationException
        // werfen. Die zweite Bedingung schützt zusätzlich vor einer schrumpfenden Liste, falls später
        // ein removeChild() ergänzt wird.
        final int count = children.size();
        for (int i = 0; i < count && i < children.size(); i++) {
            children.get(i).update(dt);  // <- das bleibt in der Basisklasse, unverändert
        }
    }

    /**
     * Hook für die eigene Pro-Frame-Logik eines Widgets (die konkrete Widget-Klasse).
     * Wird von {@link #update(float)} vor den Kindern aufgerufen.
     *
     * <p>Konkrete Unterklassen überschreiben diese Methode, um Animationen, Timer oder andere
     * zeitabhängige Zustände fortzuschreiben. Reine Container ohne eigene Logik müssen sie
     * nicht überschreiben.</p>
     *
     * @param dt vergangene Zeit seit dem letzten Frame in Sekunden
     */
    protected void updateSelf(final float dt) {
        // Default: nichts zu tun. Konkrete Widgets überschreiben diesen Hook nach Bedarf.
    }

    /**
     * Eingabe-Hook für Klick-Events. Wird aufgerufen, wenn dieses Widget über
     * {@link #hitTest(float, float)} als getroffenes Widget ermittelt wurde und der
     * Klick-Trigger (Drücken + Loslassen an derselben Stelle) vollständig ist.
     *
     * <p>Die Standard-Implementierung führt den über {@link #setClickCallback(Runnable)}
     * registrierten Callback aus, sofern einer gesetzt ist. Unterklassen können diese
     * Methode überschreiben, um zusätzlich eigene Logik auszuführen.</p>
     */
    public void onClick() {
        if (clickCallback != null) {
            clickCallback.run();
        }
    }

    /**
     * Eingabe-Hook für Hover-Verlassen. Wird aufgerufen, sobald der Mauszeiger dieses
     * Widget verlässt (der Hit-Test wechselt auf ein anderes oder gar kein Widget).
     *
     * <p>Die Standard-Implementierung führt den über {@link #setHoverExitCallback(Runnable)}
     * registrierten Callback aus, sofern einer gesetzt ist.</p>
     */
    public void onHoverExit() {
        if (hoverExitCallback != null) {
            hoverExitCallback.run();
        }
    }

    /**
     * Eingabe-Hook für Hover-Eintritt. Wird aufgerufen, sobald der Mauszeiger über diesem
     * Widget zu liegen kommt (der Hit-Test wechselt auf dieses Widget).
     *
     * <p>Die Standard-Implementierung führt den über {@link #setHoverEnterCallback(Runnable)}
     * registrierten Callback aus, sofern einer gesetzt ist.</p>
     */
    public void onHoverEnter() {
        if (hoverEnterCallback != null) {
            hoverEnterCallback.run();
        }
    }

    /**
     * Liefert das oberste (zuletzt gezeichnete) <em>interaktable</em> Widget, das die übergebene Position abdeckt.
     *
     * <p>Die Prüfung läuft rekursiv von den hinteren zu den vorderen Kindern
     * ({@code z}-Reihenfolge), sodass ein vorne liegendes, überlappendes Kind Vorrang hat.
     * Erst wenn kein Kind trifft, wird die eigene absolute Bounding-Box geprüft – allerdings
     * nur, wenn dieses Widget selbst {@link #isInteractable() interaktabel} ist. Reine Container
     * (z. B. {@link Panel}, {@link RootWidget}) werden übersprungen, Klicks auf deren Fläche
     * ergeben daher {@code null}.</p>
     *
     * @param x X-Koordinate absolut – im Koordinatensystem der Wurzel/des Canvases,
     *          nicht relativ zum Eltern-Widget
     * @param y Y-Koordinate absolut – im Koordinatensystem der Wurzel/des Canvases,
     *          nicht relativ zum Eltern-Widget
     * @return das oberste getroffene, interaktable Widget oder {@code null}, wenn kein
     *         interaktables Widget getroffen wurde
     */
    public final Widget hitTest(final float x, final float y) {
        // Von hinten nach vorne durchgehen, damit das oberste Widget gewinnt.
        for (int i = children.size() - 1; i >= 0; i--) {
            final Widget hit = children.get(i).hitTest(x, y);
            if (hit != null) return hit;
        }
        if (!interactable) {
            return null;
        }
        return getAbsoluteBounds().contains(x, y) ? this : null;
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
     * Entfernt ein direktes Kind aus diesem Widget – das Gegenstück zu {@link #addChild(Widget)}.
     *
     * <p>Das entfernte Widget hat danach keinen Eltern-Bezug mehr ({@link #getParent()} liefert
     * {@code null}), wird nicht mehr gezeichnet, aktualisiert oder von {@link #hitTest(float, float)}
     * gefunden und darf später wieder (auch bei einem anderen Widget) mit {@code addChild}
     * eingehängt werden. Es behält seine eigenen Kinder. Ein entferntes Widget hat keine
     * auflösbaren Bounds mehr; {@link #getBounds()} setzt einen Parent voraus.</p>
     *
     * <p><b>Während eines Update-Durchlaufs:</b> Entfernt ein Widget sich selbst oder ein Geschwister
     * aus dem Elternteil, während dieses gerade seine Kinder aktualisiert, wirft das keinen Fehler,
     * aber ein nachfolgendes Geschwister kann in diesem einen Frame übersprungen werden, weil die
     * Liste nachrückt. Bei Wechseln ganzer Bereiche daher besser den
     * {@code ScreenHost} verwenden, der so einen Wechsel an einer sicheren Stelle des Frames
     * ausführt.</p>
     *
     * @param child das zu entfernende Kind; darf nicht {@code null} sein
     * @return {@code true}, wenn das Widget ein direktes Kind war und entfernt wurde;
     * {@code false}, wenn es kein Kind dieses Widgets war (dann bleibt alles unverändert)
     * @throws NullPointerException wenn {@code child} {@code null} ist
     */
    public final boolean removeChild(@Nonnull final Widget child) {
        Objects.requireNonNull(child, "child must not be null");
        if (child.parent != this) {
            return false; // kein Kind dieses Widgets – insbesondere ein Kind eines anderen Parents bleibt unberührt
        }
        children.remove(child);
        child.parent = null;
        return true;
    }

    /**
     * Liefert, ob die Maustaste derzeit über diesem Widget gedrückt ist.
     * Wird für die optische Pressed-Darstellung (z. B. in {@link Button#drawSelf(Graphics2D, Rect)}) verwendet.
     *
     * @return {@code true}, solange das Widget gedrückt ist
     */
    public boolean isMousePressed() {
        return mousePressed;
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

    /**
     * Setzt den Pressed-Zustand dieses Widgets. Wird vom {@code InputDispatcher} beim
     * Drücken/Loslassen der Maustaste gesetzt; konkrete Widgets können den Zustand in
     * {@link #drawSelf(Graphics2D, Rect)} für eine optische „Pressed“-Darstellung abfragen.
     *
     * @param mousePressed {@code true}, solange die Maustaste über diesem Widget gedrückt ist
     */
    public void setMousePressed(final boolean mousePressed) {
        this.mousePressed = mousePressed;
    }

    /**
     * Registriert einen Callback, der bei einem Klick auf dieses Widget ausgeführt wird.
     *
     * @param clickCallback die bei einem Klick auszuführende Aktion; {@code null} deaktiviert den Callback
     */
    public void setClickCallback(final Runnable clickCallback) {
        this.clickCallback = clickCallback;
    }

    /**
     * Registriert einen Callback, der ausgeführt wird, sobald der Mauszeiger über dieses
     * Widget eintritt.
     *
     * @param hoverEnterCallback die auszuführende Aktion; {@code null} deaktiviert den Callback
     */
    public void setHoverEnterCallback(final Runnable hoverEnterCallback) {
        this.hoverEnterCallback = hoverEnterCallback;
    }

    /**
     * Registriert einen Callback, der ausgeführt wird, sobald der Mauszeiger dieses Widget
     * wieder verlässt.
     *
     * @param hoverExitCallback die auszuführende Aktion; {@code null} deaktiviert den Callback
     */
    public void setHoverExitCallback(final Runnable hoverExitCallback) {
        this.hoverExitCallback = hoverExitCallback;
    }

    /**
     * Liefert, ob dieses Widget selbst Eingaben (Hover, Klick) empfangen kann.
     *
     * @return {@code true}, wenn das Widget ein Eingabeziel sein kann
     */
    public final boolean isInteractable() {
        return interactable;
    }
}