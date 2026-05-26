package com.siegeofaldoria.entities;

import com.siegeofaldoria.Game;
import com.siegeofaldoria.entities.Projectile.ProjectileType;

import java.awt.*;
import java.util.List;

/**
 * Base class for all towers.
 */
public abstract class Tower {

    // ── Grid position ──────────────────────────────────────────────────────
    protected int col, row;

    // ── Stats ──────────────────────────────────────────────────────────────
    protected double range;       // pixels
    protected double damage;
    protected double fireRate;    // shots per second
    protected double projectileSpeed;
    protected double splashRadius;
    protected double slowFactor;
    protected double slowDuration;
    protected int    cost;
    protected int    level = 1;
    protected String name;
    protected Color  baseColor;
    protected Color  topColor;
    protected ProjectileType projType;

    // ── Internal ───────────────────────────────────────────────────────────
    protected double fireCooldown = 0;
    protected boolean selected = false;

    public Tower(int col, int row) {
        this.col = col;
        this.row = row;
    }

    // ── Update ─────────────────────────────────────────────────────────────
    public void update(double dt, List<Enemy> enemies, List<Projectile> projectiles) {
        if (fireCooldown > 0) fireCooldown -= dt;

        Enemy target = acquireTarget(enemies);
        if (target != null && fireCooldown <= 0) {
            shoot(target, enemies, projectiles);
            fireCooldown = 1.0 / fireRate;
        }
    }

    protected Enemy acquireTarget(List<Enemy> enemies) {
        double cx = getCenterX();
        double cy = getCenterY();
        Enemy best = null;

        for (Enemy e : enemies) {
            if (!e.isAlive()) continue;
            double dx = e.getX() - cx;
            double dy = e.getY() - cy;
            if (Math.sqrt(dx * dx + dy * dy) > range) continue;
            // Prefer the enemy furthest along the path (highest waypoint index)
            // Enemy doesn't expose waypoint — use a proxy: distance already traveled
            // We just pick the first in range for simplicity (enemies list is ordered by spawn)
            best = e;
            break;
        }
        return best;
    }

    protected void shoot(Enemy target, List<Enemy> enemies, List<Projectile> projectiles) {
        Projectile p = new Projectile(
            getCenterX(), getCenterY(), target,
            projectileSpeed, damage,
            splashRadius, slowFactor, slowDuration,
            projType
        );
        projectiles.add(p);
    }

    // ── Draw ───────────────────────────────────────────────────────────────
    public void draw(Graphics2D g2) {
        int px = col * Game.TILE_SIZE;
        int py = row * Game.TILE_SIZE;
        int ts = Game.TILE_SIZE;

        // Base platform
        g2.setColor(new Color(100, 80, 50));
        g2.fillRoundRect(px + 4, py + 4, ts - 8, ts - 8, 8, 8);

        // Tower body
        g2.setColor(baseColor);
        g2.fillRoundRect(px + 8, py + 8, ts - 16, ts - 16, 6, 6);

        // Tower top
        g2.setColor(topColor);
        g2.fillOval(px + 14, py + 14, ts - 28, ts - 28);

        // Border
        g2.setColor(baseColor.darker());
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(px + 8, py + 8, ts - 16, ts - 16, 6, 6);

        // Level pips
        for (int i = 0; i < level - 1; i++) {
            g2.setColor(Color.YELLOW);
            g2.fillOval(px + 7 + i * 7, py + ts - 14, 5, 5);
        }

        // Selection ring
        if (selected) {
            g2.setColor(new Color(255, 255, 100, 180));
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawOval((int)(getCenterX() - range), (int)(getCenterY() - range),
                        (int)(range * 2), (int)(range * 2));
        }
    }

    /** Draw range circle when hovering during placement. */
    public void drawRangePreview(Graphics2D g2) {
        g2.setColor(new Color(255, 255, 255, 50));
        g2.fillOval((int)(getCenterX() - range), (int)(getCenterY() - range),
                    (int)(range * 2), (int)(range * 2));
        g2.setColor(new Color(255, 255, 255, 150));
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                                     1f, new float[]{6, 4}, 0));
        g2.drawOval((int)(getCenterX() - range), (int)(getCenterY() - range),
                    (int)(range * 2), (int)(range * 2));
    }

    // ── Upgrade ────────────────────────────────────────────────────────────
    public boolean canUpgrade()  { return level < 3; }
    public int getUpgradeCost()  { return cost; }         // same as build cost per level

    public void upgrade() {
        if (!canUpgrade()) return;
        level++;
        damage       *= 1.4;
        range        *= 1.1;
        fireRate     *= 1.15;
    }

    // ── Helpers ────────────────────────────────────────────────────────────
    public double getCenterX() { return col * Game.TILE_SIZE + Game.TILE_SIZE / 2.0; }
    public double getCenterY() { return row * Game.TILE_SIZE + Game.TILE_SIZE / 2.0; }

    // ── Getters / Setters ──────────────────────────────────────────────────
    public int getCol()         { return col; }
    public int getRow()         { return row; }
    public int getCost()        { return cost; }
    public int getSellValue()   { return (int)(cost * 0.6 * level); }
    public double getRange()    { return range; }
    public int getLevel()       { return level; }
    public String getName()     { return name; }
    public boolean isSelected() { return selected; }
    public void setSelected(boolean s) { this.selected = s; }
}
