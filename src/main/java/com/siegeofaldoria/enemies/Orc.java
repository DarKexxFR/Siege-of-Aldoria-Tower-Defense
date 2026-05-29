package com.siegeofaldoria.enemies;

import com.siegeofaldoria.entities.Enemy;
import com.siegeofaldoria.util.Animation;
import com.siegeofaldoria.util.SpriteManager;
import com.siegeofaldoria.util.SpriteSheet;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Orc — tank équilibré avec animation directionnelle.
 *
 * Sprite sheet : /assets/enemies/orc3_run_with_shadow.png
 *   8 colonnes × 4 lignes
 *   Ligne 0 → marche vers le BAS
 *   Ligne 1 → marche vers le HAUT
 *   Ligne 2 → marche vers la GAUCHE
 *   Ligne 3 → marche vers la DROITE
 */
public class Orc extends Enemy {

    private static final String PATH      = "/assets/enemies/orc3_run_with_shadow.png";
    private static final int    COLS      = 8;
    private static final int    ROWS      = 4;
    private static final double ANIM_FPS  = 10.0;
    private static final int    DISPLAY_H = 42;  // hauteur affichée (px)

    public Orc(List<int[]> path, double boost) {
        super(path);
        name        = "Orc";
        maxHp       = (int)(180 * boost);
        hp          = maxHp;
        speed       = 70 * boost;
        goldReward  = 15;
        scoreReward = 20;
        color       = new Color(60, 160, 60);  // fallback si pas de sprite
        size        = 28;

        // ── Chargement des 4 animations directionnelles ────────────────────
        // Direction.ordinal() : 0=DOWN 1=UP 2=LEFT 3=RIGHT  → correspond aux lignes 0,1,2,3
        boolean loaded = loadAnims();
        if (!loaded) {
            System.err.println("[Orc] Sprite non trouvé : " + PATH + " → fallback cercle");
        }
    }

    private boolean loadAnims() {
        // Vérifier que l'image existe
        BufferedImage sheet = SpriteManager.load(PATH);
        if (sheet == null) return false;

        // Calculer la taille d'affichage en gardant le ratio d'une frame
        int frameW = sheet.getWidth()  / COLS;
        int frameH = sheet.getHeight() / ROWS;
        double aspect = (double) frameW / frameH;
        spriteH = DISPLAY_H;
        spriteW = (int)(DISPLAY_H * aspect);

        // Charger et scaler les 4 lignes (DOWN=0, UP=1, LEFT=2, RIGHT=3)
        dirAnims = new Animation[4];
        for (int row = 0; row < ROWS; row++) {
            BufferedImage[] frames = SpriteSheet.extractRow(PATH, COLS, ROWS, row);
            // Scaler chaque frame une seule fois
            for (int i = 0; i < frames.length; i++) {
                frames[i] = SpriteManager.scale(frames[i], spriteW, spriteH);
            }
            dirAnims[row] = new Animation(frames, ANIM_FPS);
        }
        return true;
    }
}
