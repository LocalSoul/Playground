package dev.localsoul.playground.asset.exceptions;

public class AssetNotFoundException extends RuntimeException {
    public AssetNotFoundException(final String s) {
        super(s);
    }
}
