package com.siegeofaldoria.util;

import java.awt.image.BufferedImage;

/**
 * Découpe une sprite sheet en frames individuelles.
 * Usage : SpriteSheet.extractRow("/assets/enemies/orc.png", 6, 4, 3)
 *   → extrait les 6 frames de la ligne 3 (dernière = marche)
 */
public class SpriteSheet {

    /**
     * Extrait toutes les frames d'une ligne donnée.
     *
     * @param path     Chemin classpath de l'image (ex: "/assets/enemies/orc.png")
     * @param cols     Nombre de colonnes dans la sheet
     * @param rows     Nombre de lignes dans la sheet
     * @param rowIndex Ligne à extraire (0 = première, rows-1 = dernière)
     * @return Tableau de frames, ou null si l'image est introuvable
     */
    public static BufferedImage[] extractRow(String path, int cols, int rows, int rowIndex) {
        BufferedImage sheet = SpriteManager.load(path);
        if (sheet == null) return null;

        int frameW = sheet.getWidth()  / cols;
        int frameH = sheet.getHeight() / rows;

        BufferedImage[] frames = new BufferedImage[cols];
        for (int c = 0; c < cols; c++) {
            frames[c] = sheet.getSubimage(c * frameW, rowIndex * frameH, frameW, frameH);
        }
        return frames;
    }

    /**
     * Extrait une frame précise [col, row].
     */
    public static BufferedImage extractFrame(String path, int cols, int rows, int col, int row) {
        BufferedImage sheet = SpriteManager.load(path);
        if (sheet == null) return null;
        int frameW = sheet.getWidth()  / cols;
        int frameH = sheet.getHeight() / rows;
        return sheet.getSubimage(col * frameW, row * frameH, frameW, frameH);
    }
}
