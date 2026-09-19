package dev.localsoul.playground.test;

import dev.localsoul.playground.core.asset.Asset;
import dev.localsoul.playground.core.asset.AssetManager;
import dev.localsoul.playground.core.math.Vector2f;
import dev.localsoul.playground.core.math.rect.RectTransform;
import dev.localsoul.playground.ui.Button;
import dev.localsoul.playground.ui.RootWidget;
import dev.localsoul.playground.ui.anchor.Anchor;
import dev.localsoul.playground.ui.swing.SwingLoopDriver;

public class ButtonTest {

    static void main() {

        final AssetManager assetManager = new AssetManager();
        assetManager.loadAllAssets(
                new Asset("Button", "assets/ui/button.png", 6, 6, 6, 6)
        );

        final SwingLoopDriver driver = new SwingLoopDriver("Idle Clicker - Render Test", 1280, 720);
        final RootWidget root = driver.root();

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

        driver.start();
    }
}
