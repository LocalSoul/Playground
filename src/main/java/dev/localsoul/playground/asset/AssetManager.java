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

/**
 * Verwaltung und Caching von Assets (aktuell 9-Slice-Texturen).
 *
 * <p>Assets werden über {@link Asset}-Beschreibungen (ID, Dateipfad, Border-Werte) geladen
 * und als fertige {@link NineSlice}-Objekte im Speicher gehalten. Der Cache ist bewusst
 * flach: Ein Aufruf von {@link #loadAllAssets(Asset...)} ersetzt den kompletten bisherigen
 * Bestand, damit die UI nach einem (Re-)Load immer konsistente Texturen sieht.</p>
 *
 * <p>Fehlende Dateien und unbekannte IDs führen zu einer {@link AssetNotFoundException}.</p>
 */
public final class AssetManager {

    /**
     * Cache der geladenen 9-Slice-Texturen, indiziert nach Asset-ID.
     */
    private final Map<String, NineSlice> sliceCache = new HashMap<>();

    /**
     * Lädt alle übergebenen Assets und ersetzt damit den kompletten bisherigen Cache.
     * Bereits geladene Assets, die nicht in {@code assets} vorkommen, werden verworfen.
     *
     * @param assets die zu ladenden Assets; darf leer sein (leert dann nur den Cache)
     * @throws AssetNotFoundException wenn eine Asset-Datei nicht existiert
     */
    public void loadAllAssets(final Asset... assets) {
        sliceCache.clear();
        for (final Asset asset : assets) {
            loadAndCacheAsset(asset);
        }
    }

    /**
     * Lädt ein einzelnes Asset und legt es als {@link NineSlice} im Cache ab.
     *
     * <p>Ein bereits vorhandener Eintrag mit derselben ID wird überschrieben. Existiert die
     * Datei nicht, wird eine {@link AssetNotFoundException} geworfen; kann die Datei nicht
     * als Bild gelesen werden, ein {@link RuntimeException} (Ursache liegt bei).</p>
     *
     * @param asset das zu ladende Asset; darf nicht {@code null} sein
     * @throws AssetNotFoundException wenn die Asset-Datei nicht existiert
     */
    public void loadAndCacheAsset(final Asset asset) {
        final File assetFile = new File(asset.path());
        if (!assetFile.exists()) {
            throw new AssetNotFoundException(asset);
        }

        final BufferedImage image;
        try {
            image = ImageIO.read(assetFile);
        } catch (final IOException e) {
            throw new RuntimeException("Bild konnte nicht gelesen werden: " + assetFile, e);
        }

        final NineSlice slice = new NineSlice(
                image,
                asset.borderLeft(),
                asset.borderRight(),
                asset.borderTop(),
                asset.borderBottom()
        );
        sliceCache.put(asset.id(), slice);
    }

    /**
     * Liefert die zu einer ID geladene 9-Slice-Textur.
     *
     * @param id die Asset-ID aus der {@link Asset}-Beschreibung
     * @return die geladene {@link NineSlice}-Textur
     * @throws AssetNotFoundException wenn für {@code id} kein Asset geladen wurde
     */
    public @Nonnull NineSlice getNineSlice(final String id) {
        if (!sliceCache.containsKey(id)) {
            throw new AssetNotFoundException(id);
        }
        return sliceCache.get(id);
    }
}