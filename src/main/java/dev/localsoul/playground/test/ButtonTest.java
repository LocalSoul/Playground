package dev.localsoul.playground.test;

import dev.localsoul.playground.asset.Asset;
import dev.localsoul.playground.asset.AssetManager;
import dev.localsoul.playground.input.InputDispatcher;
import dev.localsoul.playground.math.Vector2f;
import dev.localsoul.playground.math.rect.RectTransform;
import dev.localsoul.playground.ui.Button;
import dev.localsoul.playground.ui.RootWidget;
import dev.localsoul.playground.ui.anchor.Anchor;
import dev.localsoul.playground.ui.swing.UICanvas;

import javax.swing.*;
import java.awt.*;

public class ButtonTest {

    static void main() {

        final AssetManager assetManager = new AssetManager();
        assetManager.loadAllAssets(
                new Asset("Button", "assets/ui/button.png", 6, 6, 6, 6)
        );

        SwingUtilities.invokeLater(() -> {

            final RootWidget root = new RootWidget(1280, 720);

            final RectTransform testTransform = new RectTransform(
                    Anchor.CENTER,
                    new Vector2f(-32, -16), //offsetMin: halbe Breite/Höhe nach links/oben da Center-Anker
                    new Vector2f(0, 0), //offsetMax: gleich bei nicht-stretch
                    new Vector2f(128, 64) //size
            );

            // Bewusst deutlich längerer Text als der Button (128 px breit), um das
            // Überschreiten der Ränder und das Text-Clipping in Button.drawSelf zu prüfen.
            final Button button = new Button(testTransform, assetManager.getNineSlice("Button"), "test Button das ist ein sehr langer text um zu gucken ob clip funktioniert");

            // Eingabe-Test: Der UICanvas verdrahtet die Maus-Events über den InputDispatcher
            // mit der Widget-Hierarchie. Die Callbacks landen hier nur auf stdout, um das
            // Zusammenspiel von Hover (Enter/Exit) und Klick sichtbar zu prüfen.
            button.setClickCallback(() -> System.out.println("Clicked!"));
            button.setHoverEnterCallback(() -> System.out.println("Hover Entered!"));
            button.setHoverExitCallback(() -> System.out.println("Hover Exited!"));

            root.addChild(button);

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
