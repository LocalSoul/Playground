package dev.localsoul.playground.asset;

import dev.localsoul.playground.asset.exceptions.AssetNotFoundException;
import dev.localsoul.playground.ui.slice.NineSlice;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AssetManager {

    private final Map<String, NineSlice> sliceCache = new HashMap<>();

    public final void loadAllAssets(final Asset... assets) throws IOException {
        sliceCache.clear();
        for (final Asset asset : assets) {
            loadAndCacheAsset(asset);
        }
    }

    public final void loadAndCacheAsset(final Asset asset) throws IOException {
        final BufferedImage image = ImageIO.read(new File(asset.path()));
        final NineSlice slice = new NineSlice(image, asset.borderLeft(), asset.borderRight(), asset.borderTop(), asset.borderBottom()); // links, rechts, oben, unten
        sliceCache.put(asset.id(), slice);
    }

    public final @Nonnull NineSlice getNineSlice(final String id) {
        if (!sliceCache.containsKey(id)) {
            throw new AssetNotFoundException("Asset with id " + id + " not found!");
        }
        return sliceCache.get(id);
    }

}
