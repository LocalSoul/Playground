package dev.localsoul.playground.asset;

import dev.localsoul.playground.core.asset.Asset;
import dev.localsoul.playground.core.asset.AssetManager;
import dev.localsoul.playground.core.asset.exceptions.AssetNotFoundException;
import dev.localsoul.playground.ui.slice.NineSlice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AssetManagerTest {

    private static final int WIDTH = 24;
    private static final int HEIGHT = 32;
    @TempDir
    Path tempDir;

    private File writeImage(final String name) throws IOException {
        final File file = tempDir.resolve(name).toFile();
        final BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        ImageIO.write(image, "png", file);
        return file;
    }

    @Test
    void loadAndCacheAssetStoresNineSlice() throws IOException {
        final AssetManager manager = new AssetManager();
        final File file = writeImage("button.png");
        final Asset asset = new Asset("button", file.getAbsolutePath(), 2, 3, 4, 5);

        manager.loadAndCacheAsset(asset);

        final NineSlice slice = manager.getNineSlice("button");
        assertNotNull(slice);
        assertEquals(WIDTH, slice.textureWidth());
        assertEquals(HEIGHT, slice.textureHeight());
        assertEquals(2, slice.borderTop());
        assertEquals(3, slice.borderBottom());
        assertEquals(4, slice.borderLeft());
        assertEquals(5, slice.borderRight());
    }

    @Test
    void getNineSliceForUnknownAssetThrows() throws IOException {
        final AssetManager manager = new AssetManager();
        manager.loadAndCacheAsset(new Asset("button", writeImage("button.png").getAbsolutePath(), 0, 0, 0, 0));

        assertThrows(AssetNotFoundException.class, () -> manager.getNineSlice("missing"));
    }

    @Test
    void loadAllAssetsLoadsAll() throws IOException {
        final AssetManager manager = new AssetManager();
        manager.loadAllAssets(
                new Asset("a", writeImage("a.png").getAbsolutePath(), 1, 1, 1, 1),
                new Asset("b", writeImage("b.png").getAbsolutePath(), 2, 2, 2, 2)
        );

        assertEquals(1, manager.getNineSlice("a").borderTop());
        assertEquals(2, manager.getNineSlice("b").borderTop());
    }

    @Test
    void loadAllAssetsClearsPreviousCache() throws IOException {
        final AssetManager manager = new AssetManager();
        manager.loadAndCacheAsset(new Asset("a", writeImage("a.png").getAbsolutePath(), 1, 1, 1, 1));

        manager.loadAllAssets(new Asset("b", writeImage("b.png").getAbsolutePath(), 2, 2, 2, 2));

        assertThrows(AssetNotFoundException.class, () -> manager.getNineSlice("a"));
        assertNotNull(manager.getNineSlice("b"));
    }

    @Test
    void loadAllAssetsWithNoAssetsClearsCache() throws IOException {
        final AssetManager manager = new AssetManager();
        manager.loadAndCacheAsset(new Asset("a", writeImage("a.png").getAbsolutePath(), 1, 1, 1, 1));

        manager.loadAllAssets();

        assertThrows(AssetNotFoundException.class, () -> manager.getNineSlice("a"));
    }
}