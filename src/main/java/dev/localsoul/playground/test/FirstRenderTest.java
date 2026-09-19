package dev.localsoul.playground.test;

import dev.localsoul.playground.core.asset.Asset;
import dev.localsoul.playground.core.asset.AssetManager;
import dev.localsoul.playground.core.input.InputDispatcher;
import dev.localsoul.playground.core.math.Vector2f;
import dev.localsoul.playground.core.math.rect.RectTransform;
import dev.localsoul.playground.ui.Panel;
import dev.localsoul.playground.ui.RootWidget;
import dev.localsoul.playground.ui.anchor.Anchor;
import dev.localsoul.playground.ui.swing.UICanvas;

import javax.swing.*;
import java.awt.*;

public class FirstRenderTest {

    static void main() {

        final AssetManager assetManager = new AssetManager();
        assetManager.loadAllAssets(
                new Asset("NineSliceTest", "assets/ui/nineslice_test.png", 12, 12, 12, 12)
        );

        SwingUtilities.invokeLater(() -> {

            final RootWidget root = new RootWidget(1280, 720);

            final RectTransform testTransform = new RectTransform(
                    Anchor.CENTER,
                    new Vector2f(-150, -75), //offsetMin: halbe Breite/Höhe nach links/oben da Center-Anker
                    new Vector2f(0, -75), //offsetMax: gleich bei nicht-stretch
                    new Vector2f(300, 150) //size
            );

            final Panel testPanel = new Panel(testTransform, assetManager.getNineSlice("NineSliceTest"));
            root.addChild(testPanel);

            final UICanvas canvas = new UICanvas(root, new InputDispatcher(root));
            canvas.setPreferredSize(new Dimension(1280, 720));

            final JFrame frame = new JFrame("Idle Clicker - Render Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(canvas);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

        });

    }

}
