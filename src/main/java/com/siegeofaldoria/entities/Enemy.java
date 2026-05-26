package com.siegeofaldoria.entities;

import com.siegeofaldoria.Game;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Base class for all enemies walking along the path.
 * If a subclass sets {@code sprite}, it is drawn instead of the fallback circle.
 */
public abstract class Enemy {

    // ── Position (pixel coordinates) ──────────────────────────────────────
    protected double x, y;

    // ── Path tracking ─────────────────────────────────────────────────────
    protected List<int[]> path;
    protected int     waypointIndex = 0;
    protected boolean reachedEnd    = false;

    // ── Stats ─────────────────────────────────────────────────────────────
    protected int    maxHp;
    protected double hp;
    protected double speed;
    protected int    goldReward;
    protected int    scoreReward;
    protected Color  color;
    protected String name;
    protected int    size = 28;   // draw size (pixels)

    // ── Sprite (optional — set in subclass constructor) ────────────────────
    protected BufferedImage sprite     = null;   // scaled sprite, ready to draw
    protected int           spriteW    = 0;
    protected int           spriteH    = 0;

    // ── Status effects ────────────────────────────────────────────────────
    protected double slowTimer  = 0;
    protected double slowFactor = 1.0;

    public Enemy(List<int[]> path) {
        this.path = path;
        if (!path.isEmpty()) {
            x = path.get(0)[0] * Game.TILE_SIZE + Game.TILE_SIZE / 2.0;
            y = path.get(0)[1] * Game.TILE_SIZE + Game.TILE_SIZE / 2.0;
            waypointIndex = 1;
        }
    }

    // ── Update ─────────────────────────────────────────────────────────────
    public void update(double dt) {
        if (slowTimer > 0) {
            slowTimer -= dt;
            if (slowTimer <= 0) { slowTimer = 0; slowFactor = 1.0; }
        }

        if (waypointIndex >= path.size()) { reachedEnd = true; return; }

        int[]  wp   = path.get(waypointIndex);
        double tx   = wp[0] * Game.TILE_SIZE + Game.TILE_SIZE / 2.0;
        double ty   = wp[1] * Game.TILE_SIZE + Game.TILE_SIZE / 2.0;
        double dx   = tx - x;
        double dy   = ty - y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        double move = speed * slowFactor * dt;

        if (dist <= move) {
            x = tx; y = ty;
            waypointIndex++;
        } else {
            x += (dx / dist) * move;
            y += (dy / dist) * move;
        }
    }

    // ── Draw ───────────────────────────────────────────────────────────────
    public void draw(Graphics2D g2) {
        if (sprite != null) {
            drawSprite(g2);
        } else {
            drawFallback(g2);
        }
        drawHpBar(g2);
        drawSlowRing(g2);
    }

    /** Draw using a pixel-art sprite centered on (x, y). */
    private void drawSprite(Graphics2D g2) {
        int px = (int) x - spriteW / 2;
        int py = (int) y - spriteH / 2;

        // Drop shadow
        g2.setColor(new Color(0, 0, 0, 60));
        g2.fillOval(px + 3, py + spriteH - 4, spriteW - 4, 6);

        // Nearest-neighbour interpolation — keeps pixel-art crisp
        Object prevInterp = g2.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.drawImage(sprite, px, py, spriteW, spriteH, null);
        if (prevInterp != null)
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, prevInterp);

        // Blue slow ring drawn over sprite
        if (slowTimer > 0) {
            g2.setColor(new Color(100, 180, 255, 160));
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawRoundRect(px - 2, py - 2, spriteW + 4, spriteH + 4, 6, 6);
        }
    }

    /** Original circle fallback for enemies without a sprite. */
    private void drawFallback(Graphics2D g2) {
        int px = (int) x - size / 2;
        int py = (int) y - size / 2;

        g2.setColor(new Color(0, 0, 0, 60));
        g2.fillOval(px + 3, py + 3, size, size);

        g2.setColor(color);
        g2.fillOval(px, py, size, size);

        g2.setColor(color.darker());
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(px, py, size, size);
    }

    private void drawHpBar(Graphics2D g2) {
        int drawSize = (sprite != null) ? spriteW : size;
        int barW = drawSize + 4;
        int barH = 5;
        int bx   = (int) x - drawSize / 2 - 2;
        int by   = (int) y - (sprite != null ? spriteH / 2 : size / 2) - 12;
        double ratio = hp / maxHp;

        g2.setColor(new Color(40, 0, 0));
        g2.fillRect(bx, by, barW, barH);

        Color hpColor = ratio > 0.5 ? new Color(50, 200, 50)
                      : ratio > 0.25 ? new Color(220, 180, 0)
                      : new Color(200, 40, 40);
        g2.setColor(hpColor);
        g2.fillRect(bx, by, (int)(barW * ratio), barH);

        g2.setColor(new Color(0, 0, 0, 180));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRect(bx, by, barW, barH);
    }

    private void drawSlowRing(Graphics2D g2) {
        if (slowTimer <= 0 || sprite != null) return; // sprite path handles its own ring
        int px = (int) x - size / 2;
        int py = (int) y - size / 2;
        g2.setColor(new Color(100, 180, 255, 180));
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(px - 2, py - 2, size + 4, size + 4);
    }

    // ── Combat ─────────────────────────────────────────────────────────────
    public void takeDamage(double dmg)                { hp -= dmg; }
    public void applySlow(double factor, double dur)  {
        if (factor < slowFactor) slowFactor = factor;
        if (dur > slowTimer)     slowTimer  = dur;
    }

    // ── Getters ────────────────────────────────────────────────────────────
    public double  getX()           { return x; }
    public double  getY()           { return y; }
    public boolean isAlive()        { return hp > 0; }
    public boolean hasReachedEnd()  { return reachedEnd; }
    public int     getGoldReward()  { return goldReward; }
    public int     getScoreReward() { return scoreReward; }
    public double  getHp()          { return hp; }
    public int     getMaxHp()       { return maxHp; }
    public String  getName()        { return name; }
}
