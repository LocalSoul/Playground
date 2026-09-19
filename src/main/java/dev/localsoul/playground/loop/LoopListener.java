package dev.localsoul.playground.loop;

/**
 * Empfänger der Aufrufe einer {@link GameLoop}. Die drei Methoden decken die drei
 * Zeit-Ebenen einer typischen Spiel-/UI-Schleife ab.
 */
public interface LoopListener {

    /**
     * Spiellogik in einem festen Schritt.
     *
     * <p>Wird von der {@link GameLoop} exakt {@code tickNanos} Mal pro Sekunde aufgerufen –
     * unabhängig von der Rendering-Rate. Hier gehört deterministische, zeitlich feste
     * Logik hin. {@code dt} ist immer konstant (die Schrittlänge), nie die echte
     * Frame-Zeit.</p>
     *
     * @param dt die feste Schrittlänge in Sekunden (z. B. {@code 0.05} bei 20 Hz)
     */
    void tick(double dt);

    /**
     * Grobes Nachsimulieren einer ungewöhnlich großen Zeitlücke.
     *
     * <p>Wird aufgerufen, wenn ein einzelner Frame länger dauerte als
     * {@code MAX_FRAME_NANOS}: Die über diese Obergrenze hinausgehende Zeit wird hier
     * übergeben, damit simulierte und echte Zeit nicht auseinanderlaufen. Die Simulation
     * darf hier bewusst grob sein (z. B. reine Zeitrechnung statt vieler Einzelschritte),
     * um die Schleife in einem langen Frame nicht zum Stillstand zu bringen.</p>
     *
     * @param nanos die Zeit in Nanosekunden, die grob nachsimuliert werden soll
     */
    void catchUp(long nanos);

    /**
     * Variabler Update pro gerendertem Frame.
     *
     * <p>Wird nach den festen Schritten einmal pro {@code advance()}-Aufruf mit der
     * tatsächlich vergangenen Frame-Zeit (maximal begrenzt auf {@code MAX_FRAME_NANOS})
     * aufgerufen. Hier gehören rendering-abhängige Arbeiten hin: {@code Input}-Updates,
     * UI-/Animations-Updates und das Auslösen des Repaints.</p>
     *
     * @param dt die vergangene Frame-Zeit in Sekunden als {@code float}
     */
    void frame(float dt);

}
