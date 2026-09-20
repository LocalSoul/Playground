package dev.localsoul.playground.ui.screen;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Verwaltet die Screens eines Spiels unter Bezeichnern und schaltet einen {@link ScreenHost}
 * zwischen ihnen um. Die Navigations-Buttons rufen nur {@link #navigate(Object)} auf; sie müssen
 * die Screen-Instanzen nicht kennen.
 *
 * <p>Als Bezeichner {@code K} eignet sich ein {@code enum} des Spiels:</p>
 * <pre>{@code
 * enum Tab { QUESTS, CHARACTER }
 *
 * Navigator<Tab> navigator = new Navigator<>(host)
 *         .register(Tab.QUESTS, questScreen)
 *         .register(Tab.CHARACTER, characterScreen);
 * navigator.navigate(Tab.QUESTS);
 * }</pre>
 *
 * <p>Welcher Tab in einer Navigationsleiste aktiv aussieht, muss nicht über Callbacks gemeldet
 * werden: Die Leiste liest in ihrem {@code updateSelf} einfach jeden Frame
 * {@link #isCurrent(Object)} ab.</p>
 *
 * <p>{@link #current()} liefert den zuletzt <b>angefragten</b> Bezeichner und ist sofort nach
 * {@link #navigate(Object)} aktuell. Der Screen im {@link ScreenHost} folgt erst zu Beginn des
 * nächsten Updates (siehe dort), der Unterschied ist höchstens ein Frame lang.</p>
 *
 * @param <K> der Typ der Screen-Bezeichner; muss sinnvolle {@code equals}/{@code hashCode}
 *            haben (bei einem {@code enum} gegeben)
 */
public final class Navigator<K> {

    private final ScreenHost host;
    private final Map<K, Screen> screens = new LinkedHashMap<>();
    private K current;

    /**
     * Erzeugt einen Navigator für den übergebenen Host. Es ist noch kein Screen angezeigt, bis
     * zum ersten Mal {@link #navigate(Object)} aufgerufen wird.
     *
     * @param host der Inhaltsbereich, in dem die Screens erscheinen; darf nicht {@code null} sein
     * @throws NullPointerException wenn {@code host} {@code null} ist
     */
    public Navigator(@Nonnull final ScreenHost host) {
        this.host = Objects.requireNonNull(host, "host must not be null");
    }

    /**
     * Registriert einen Screen unter einem Bezeichner. Kann verkettet aufgerufen werden.
     *
     * @param id     der Bezeichner; darf nicht {@code null} sein und nicht schon vergeben
     * @param screen der Screen; darf nicht {@code null} sein und nicht schon unter einem
     *               anderen Bezeichner registriert
     * @return dieser Navigator, für verkettete Aufrufe
     * @throws NullPointerException     wenn {@code id} oder {@code screen} {@code null} ist
     * @throws IllegalArgumentException wenn der Bezeichner oder der Screen schon registriert ist
     */
    public Navigator<K> register(@Nonnull final K id, @Nonnull final Screen screen) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(screen, "screen must not be null");
        if (screens.containsKey(id)) {
            throw new IllegalArgumentException("id already registered: " + id);
        }
        if (screens.containsValue(screen)) {
            throw new IllegalArgumentException("screen already registered under another id");
        }
        screens.put(id, screen);
        return this;
    }

    /**
     * Wechselt zum Screen mit diesem Bezeichner. Der Wechsel im {@link ScreenHost} wird zu Beginn
     * des nächsten Updates ausgeführt; der Aufruf ist deshalb aus jedem Kontext sicher. Wird der
     * bereits aktuelle Screen angefragt, passiert im Host nichts.
     *
     * @param id der Bezeichner eines registrierten Screens; darf nicht {@code null} sein
     * @throws NullPointerException     wenn {@code id} {@code null} ist
     * @throws IllegalArgumentException wenn kein Screen unter diesem Bezeichner registriert ist
     */
    public void navigate(@Nonnull final K id) {
        Objects.requireNonNull(id, "id must not be null");
        final Screen screen = screens.get(id);
        if (screen == null) {
            throw new IllegalArgumentException("unknown screen id: " + id);
        }
        current = id;
        host.show(screen);
    }

    /**
     * Liefert den zuletzt angefragten Bezeichner.
     *
     * @return der aktuelle Bezeichner oder {@code null}, solange noch nie navigiert wurde
     */
    public @Nullable K current() {
        return current;
    }

    /**
     * Prüft, ob der Bezeichner der zuletzt angefragte ist. Gedacht für Navigationsleisten, die
     * ihren aktiven Tab jeden Frame daraus ableiten.
     *
     * @param id der zu prüfende Bezeichner
     * @return {@code true}, wenn {@code id} der aktuelle Bezeichner ist
     */
    public boolean isCurrent(final K id) {
        return current != null && current.equals(id);
    }
}
