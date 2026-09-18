package dev.localsoul.playground.ui.slice;

import java.awt.image.BufferedImage;

public record NineSlice(BufferedImage texture, int borderLeft, int borderRight, int borderTop, int borderBottom) {

    public int textureWidth() {
        return texture.getWidth();
    }

    public int textureHeight() {
        return texture.getHeight();
    }

}
