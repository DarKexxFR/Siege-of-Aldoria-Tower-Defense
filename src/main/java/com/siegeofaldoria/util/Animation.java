package com.siegeofaldoria.util;

import java.awt.image.BufferedImage;

/**
 * Anime un tableau de frames à une vitesse donnée (FPS).
 * Appelle update(dt) à chaque tick du jeu, getCurrentFrame() pour dessiner.
 */
public class Animation {

    private final BufferedImage[] frames;
    private final double          frameDuration; // secondes par frame
    private double                timer        = 0;
    private int                   currentIndex = 0;
    private boolean               loop         = true;
    private boolean               finished     = false;

    /**
     * @param frames Tableau de frames (extrait via SpriteSheet)
     * @param fps    Vitesse de l'animation (frames par seconde)
     */
    public Animation(BufferedImage[] frames, double fps) {
        this.frames        = frames;
        this.frameDuration = (fps > 0) ? 1.0 / fps : 0.1;
    }

    // ── Update ─────────────────────────────────────────────────────────────
    public void update(double dt) {
        if (finished || frames == null || frames.length == 0) return;

        timer += dt;
        while (timer >= frameDuration) {
            timer -= frameDuration;
            currentIndex++;
            if (currentIndex >= frames.length) {
                if (loop) {
                    currentIndex = 0;
                } else {
                    currentIndex = frames.length - 1;
                    finished = true;
                }
            }
        }
    }

    // ── Getters ────────────────────────────────────────────────────────────
    public BufferedImage getCurrentFrame() {
        if (frames == null || frames.length == 0) return null;
        return frames[currentIndex];
    }

    public boolean isFinished() { return finished; }
    public void reset()         { currentIndex = 0; timer = 0; finished = false; }
    public void setLoop(boolean loop) { this.loop = loop; }
}
