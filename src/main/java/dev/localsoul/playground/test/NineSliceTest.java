package dev.localsoul.playground.test;

import dev.localsoul.playground.asset.Asset;
import dev.localsoul.playground.asset.AssetManager;
import dev.localsoul.playground.asset.exceptions.AssetNotFoundException;
import dev.localsoul.playground.math.Vector2f;
import dev.localsoul.playground.math.rect.RectTransform;
import dev.localsoul.playground.ui.Panel;
import dev.localsoul.playground.ui.RootWidget;
import dev.localsoul.playground.ui.anchor.Anchor;
import dev.localsoul.playground.ui.slice.NineSlice;
import dev.localsoul.playground.ui.swing.UICanvas;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class NineSliceTest {

    static void main() {
        final AssetManager assetManager = new AssetManager();
        try {
            assetManager.loadAllAssets(
                    new Asset("NineSliceTest", "assets/ui/nineslice_test.png", 12, 12, 12, 12)
            );
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        SwingUtilities.invokeLater(() -> {

            final RootWidget root = new RootWidget(1280, 720);
            final NineSlice nineSlice;
            try {
                nineSlice = assetManager.getNineSlice("NineSliceTest");
            } catch (final AssetNotFoundException e) {
                throw new RuntimeException(e);
            }


            // 1) Großes, quadratisch-nahes Panel (Referenzfall aus vorher)
            final RectTransform bigTransform = new RectTransform(
                    Anchor.CENTER,
                    new Vector2f(-150, -75),
                    new Vector2f(-150, -75),
                    new Vector2f(300, 150)
            );
            root.addChild(new Panel(bigTransform, nineSlice));

            // 2) Breiter, flacher Button-artiger Panel (testet unterschiedlich starke Streckung X vs Y)
            final RectTransform wideTransform = new RectTransform(
                    Anchor.TOP_LEFT,
                    new Vector2f(50, 50),
                    new Vector2f(50, 50),
                    new Vector2f(400, 48)
            );
            root.addChild(new Panel(wideTransform, nineSlice));

            // 3) Sehr kleines Panel, kleiner als 2x Border (testet destCenterWidth/Height = 0 Fall)
            final RectTransform tinyTransform = new RectTransform(
                    Anchor.TOP_LEFT,
                    new Vector2f(50, 150),
                    new Vector2f(50, 150),
                    new Vector2f(16, 16)
            );
            root.addChild(new Panel(tinyTransform, nineSlice));

            // 4) Schmale, hohe Spalte (testet starke Y-Streckung bei kaum X-Streckung)
            final RectTransform tallTransform = new RectTransform(
                    Anchor.TOP_LEFT,
                    new Vector2f(50, 220),
                    new Vector2f(50, 220),
                    new Vector2f(48, 300)
            );
            root.addChild(new Panel(tallTransform, nineSlice));

            // 5) Rechte untere Ecke, testet Anchor BOTTOM_RIGHT in Kombination mit 9-Slice
            final RectTransform cornerTransform = new RectTransform(
                    Anchor.BOTTOM_RIGHT,
                    new Vector2f(-150, -80),
                    new Vector2f(-150, -80),
                    new Vector2f(150, 80)
            );
            root.addChild(new Panel(cornerTransform, nineSlice));

            // 6) Volle Breite oben, testet TOP_STRETCH mit 9-Slice (Höhe fix, Breite folgt Fenster)
            final RectTransform stretchTransform = new RectTransform(
                    Anchor.TOP_STRETCH,
                    new Vector2f(20, 500),
                    new Vector2f(-20, 60), // rechter Rand 20px Einzug, Höhe endet bei y=560 relativ
                    new Vector2f(0, 0) // bei stretch ungenutzt, siehe Hinweis unten
            );
            root.addChild(new Panel(stretchTransform, nineSlice));

            final UICanvas canvas = new UICanvas(root);
            canvas.setPreferredSize(new Dimension(1280, 720));

            final JFrame frame = new JFrame("Idle Clicker - NineSlice Render Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(canvas);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

}
