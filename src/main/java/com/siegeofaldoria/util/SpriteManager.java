package com.siegeofaldoria.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads and caches sprites from the classpath resources.
 * Images are scaled with nearest-neighbour to preserve the pixel-art look.
 */
public class SpriteManager {

    private static final Map<String, BufferedImage> cache = new HashMap<>();

    /**
     * Load an image from the classpath and cache it.
     * Returns null (silently) if the resource is missing — callers fall back to shapes.
     *
     * @param path  Classpath path, e.g. "/assets/enemies/goblin.png"
     */
    public static BufferedImage load(String path) {
        return cache.computeIfAbsent(path, p -> {
            try (InputStream is = SpriteManager.class.getResourceAsStream(p)) {
                if (is == null) {
                    System.err.println("[SpriteManager] Not found: " + p);
                    return null;
                }
                return ImageIO.read(is);
            } catch (Exception e) {
                System.err.println("[SpriteManager] Failed to load " + p + ": " + e.getMessage());
                return null;
            }
        });
    }

    /**
     * Scale a BufferedImage using nearest-neighbour interpolation
     * (keeps the crisp pixel-art look at any size).
     *
     * @param src    Source image
     * @param width  Target width  in pixels
     * @param height Target height in pixels
     */
    public static BufferedImage scale(BufferedImage src, int width, int height) {
        if (src == null) return null;
        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                           RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(src, 0, 0, width, height, null);
        g.dispose();
        return out;
    }

    /** Convenience: load + scale in one call. */
    public static BufferedImage loadScaled(String path, int width, int height) {
        String key = path + "@" + width + "x" + height;
        return cache.computeIfAbsent(key, k -> scale(load(path), width, height));
    }

    /** Clear the cache (useful for hot-reload in dev). */
    public static void clearCache() { cache.clear(); }
}
