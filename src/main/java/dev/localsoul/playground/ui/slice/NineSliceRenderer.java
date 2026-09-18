package dev.localsoul.playground.ui.slice;

import java.awt.*;

public class NineSliceRenderer {

    public static void draw(final Graphics2D g, final NineSlice nineSlice, final int targetWidth, final int targetHeight) {
        final int textureWidth = nineSlice.textureWidth();
        final int textureHeight = nineSlice.textureHeight();

        final int borderLeft = nineSlice.borderLeft();
        final int borderRight = nineSlice.borderRight();
        final int borderTop = nineSlice.borderTop();
        final int borderBottom = nineSlice.borderBottom();

        //Quell-Koordinaten aus Original-Textur
        final int srcCenterWidth = textureWidth - borderLeft - borderRight;
        final int srcCenterHeight = textureHeight - borderTop - borderBottom;

        // Ziel-Koordinaten (im Zielbereich, ggf. geklemmt falls Ziel kleiner als Border-Summe
        final int destCenterWidth = Math.max(0, targetWidth - borderLeft - borderRight);
        final int destCenterHeight = Math.max(0, targetHeight - borderTop - borderBottom);

        //3 Spalten-Grenzen, Quelle
        final int[] srcX = {0, borderLeft, borderLeft + srcCenterWidth, textureWidth};
        final int[] srcY = {0, borderTop, borderTop + srcCenterHeight, textureHeight};

        //3 Spalten-Grenzen, Ziel
        final int[] destX = {0, borderLeft, borderLeft + destCenterWidth, targetWidth};
        final int[] destY = {0, borderTop, borderTop + destCenterHeight, targetHeight};

        //Alle 9 Segmente zeichnen
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                g.drawImage(nineSlice.texture(),
                        destX[col], destY[row], destX[col + 1], destY[row + 1],
                        srcX[col], srcY[row], srcX[col + 1], srcY[row + 1],
                        null);
            }
        }


    }

}
