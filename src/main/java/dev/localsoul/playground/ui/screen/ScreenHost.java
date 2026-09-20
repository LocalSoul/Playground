package dev.localsoul.playground.ui.screen;

import dev.localsoul.playground.core.math.rect.RectTransform;
import dev.localsoul.playground.ui.Widget;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Der Inhaltsbereich, der genau einen {@link Screen} anzeigt. Dauerhafte Elemente wie eine
 * Navigations- oder Ressourcenleiste liegen als Geschwister neben dem Host im Widget-Baum und
 * bleiben deshalb beim Wechsel stehen:
 *
 * <pre>
 * RootWidget
 *  ├─ ScreenHost     (füllt den Rest; darin der aktuelle Screen)
 *  ├─ ResourceBar    (dauerhaft)
 *  └─ NavBar         (dauerhaft)
 * </pre>
 *
 * <h2>Wechsel an einer sicheren Stelle</h2>
 * <p>{@link #show(Screen)} führt den Wechsel <b>nicht sofort</b> aus, sondern merkt ihn vor. Er wird
 * zu Beginn des nächsten {@code update}-Durchlaufs dieses Hosts angewendet, bevor der Host seine
 * Kinder aktualisiert. Dadurch darf {@code show} von überall aufgerufen werden, auch aus dem
 * {@code updateSelf} eines Widgets innerhalb des gerade angezeigten Screens, ohne dass der Baum
 * mitten in einer Iteration verändert wird. Der neue Screen wird noch im selben Durchlauf erstmals
 * aktualisiert und danach gezeichnet, sichtbar verzögert sich der Wechsel also nicht.</p>
 *
 * <p>Beim Wechsel gilt die Reihenfolge: {@link Screen#onHide()} des alten Screens, Aushängen,
 * Einhängen des neuen Screens, Layout, {@link Screen#onShow()} des neuen Screens. Wird mehrfach
 * angefragt, bevor ein Update lief, zählt die <b>letzte</b> Anfrage. Wird der bereits angezeigte
 * Screen angefragt, passiert nichts (keine Hooks).</p>
 *
 * <p>Der Host muss im Widget-Baum hängen und aktualisiert werden, damit ein Wechsel angewendet
 * wird. Wie alle Widgets ist er nicht thread-safe; Aufrufe gehören in den Thread der
 * Spielschleife (bei Swing der Event-Dispatch-Thread).</p>
 */
public final class ScreenHost extends Widget {

    /**
     * Der aktuell eingehängte Screen; {@code null}, solange noch keiner angezeigt wurde.
     */
    private Screen current;

    /** Die vorgemerkte Anfrage; {@code null}, wenn nichts ansteht. */
    private Screen pending;

    /**
     * Erzeugt einen Host mit der übergebenen Lage im Elternteil. Der Host selbst ist nicht
     * interaktiv.
     *
     * @param transform die Lage des Inhaltsbereichs, meist gestreckt zwischen den dauerhaften
     *                  Leisten (siehe {@code Anchor.STRETCH}); darf nicht {@code null} sein
     */
    public ScreenHost(@Nonnull final RectTransform transform) {
        super(transform, false);
    }

    /**
     * Merkt vor, dass dieser Screen angezeigt werden soll. Der Wechsel wird zu Beginn des nächsten
     * Updates dieses Hosts angewendet (siehe Klassenbeschreibung).
     *
     * @param screen der anzuzeigende Screen; darf nicht {@code null} sein
     * @throws NullPointerException     wenn {@code screen} {@code null} ist
     * @throws IllegalArgumentException wenn der Screen bereits in einem anderen Widget hängt; ein
     *                                  Screen gehört immer nur zu einem Host
     */
    public void show(@Nonnull final Screen screen) {
        Objects.requireNonNull(screen, "screen must not be null");
        if (screen.getParent() != null && screen.getParent() != this) {
            throw new IllegalArgumentException("screen already belongs to another parent: " + screen.getParent());
        }
        pending = screen;
    }

    /**
     * Liefert den Screen, der gerade im Widget-Baum hängt. Eine mit {@link #show(Screen)}
     * vorgemerkte Anfrage ist hier erst nach dem nächsten Update sichtbar.
     *
     * @return der aktuell angezeigte Screen oder {@code null}, wenn noch keiner angezeigt wird
     */
    public @Nullable Screen current() {
        return current;
    }

    /**
     * Wendet eine vorgemerkte Anfrage an. Läuft in {@code updateSelf}, also bevor der Host seine
     * eigenen Kinder aktualisiert; der Host verändert damit nur seine <i>eigene</i> Kinderliste an
     * einer Stelle, an der er sie nicht iteriert.
     */
    @Override
    protected void updateSelf(final float dt) {
        final Screen next = pending;
        pending = null;
        if (next == null || next == current) {
            return;
        }

        if (current != null) {
            current.onHide();
            removeChild(current);
        }
        current = next;
        addChild(next);
        relayout();
        next.onShow();
    }
}
