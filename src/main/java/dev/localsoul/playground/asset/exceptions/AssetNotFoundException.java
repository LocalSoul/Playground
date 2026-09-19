package dev.localsoul.playground.asset.exceptions;

import dev.localsoul.playground.asset.Asset;

public class AssetNotFoundException extends RuntimeException {
    public AssetNotFoundException(final String id) {
        super("Asset with id " + id + " not found!");
    }

    public AssetNotFoundException(final Asset asset) {
        this(asset.id());
    }

}
