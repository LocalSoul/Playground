package dev.localsoul.playground.ui.swing;

import dev.localsoul.playground.input.InputDispatcher;
import dev.localsoul.playground.ui.RootWidget;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UICanvas extends JPanel {

    /**
     * Die Wurzel der Widget-Hierarchie, die auf diesem Canvas gezeichnet wird.
     */
    private final RootWidget rootWidget;

    /**
     * Erzeugt ein Canvas für die übergebene Hierarchie-Wurzel und verdrahtet den
     * übergebenen {@link InputDispatcher} mit den Swing-Maus-Events: Bewegung und Drag
     * werden als {@code pointerMoved}, linker Klick als {@code pointerPressed}/
     * {@code pointerReleased}, und Verlassen des Canvases als {@code pointerExited}
     * weitergeleitet. Zusätzlich wird die Wurzel bei Größenänderung des Panels neu
     * dimensioniert.
     *
     * <p>Der Dispatcher wird von außen übergeben (nicht hier erzeugt), damit dieselbe
     * Instanz für Click- und Hover-Callbacks sowie für den Per-Frame-Update aus der
     * Game-Schleife (siehe {@code input.update()}) genutzt werden kann.</p>
     *
     * @param rootWidget die Hierarchie-Wurzel; darf nicht {@code null} sein
     * @param input      der zu verdrahtende Eingabe-Dispatcher; darf nicht {@code null} sein
     */
    public UICanvas(final @Nonnull RootWidget rootWidget, @Nonnull final InputDispatcher input) {
        this.rootWidget = rootWidget;
        setDoubleBuffered(true);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(final ComponentEvent e) {
                rootWidget.resize(getWidth(), getHeight());
                repaint();
            }
        });

        final MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mouseMoved(final MouseEvent e) {
                input.pointerMoved(e.getX(), e.getY());
            }

            @Override
            public void mouseDragged(final MouseEvent e) {
                input.pointerMoved(e.getX(), e.getY());
            }

            @Override
            public void mousePressed(final MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) input.pointerPressed(e.getX(), e.getY());
            }

            @Override
            public void mouseReleased(final MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) input.pointerReleased(e.getX(), e.getY());
            }

            @Override
            public void mouseExited(final MouseEvent e) {
                input.pointerExited();
            }
        };

        addMouseListener(adapter);
        addMouseMotionListener(adapter);

    }

    /**
     * Zeichnet den aktuellen Frame: Antialiasing einschalten und die gesamte Hierarchie
     * ab der Wurzel zeichnen.
     */
    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);

        final Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        rootWidget.draw(g2d);
    }
}
