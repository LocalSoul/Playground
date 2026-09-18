package dev.localsoul.playground.ui.swing;

import dev.localsoul.playground.ui.RootWidget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class UICanvas extends JPanel {

    private final RootWidget rootWidget;

    public UICanvas(final RootWidget rootWidget) {
        this.rootWidget = rootWidget;
        setDoubleBuffered(true);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(final ComponentEvent e) {
                rootWidget.resize(getWidth(), getHeight());
                repaint();
            }
        });


    }


    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);

        final Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        rootWidget.draw(g2d);
    }
}
