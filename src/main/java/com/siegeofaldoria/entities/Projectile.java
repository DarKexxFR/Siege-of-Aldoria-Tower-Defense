package com.siegeofaldoria.entities;

import java.awt.*;
import java.util.List;

/**
 * A projectile fired by a tower toward a target enemy.
 * Supports single-target and AoE-splash variants.
 */
public class Projectile {

    public enum ProjectileType { ARROW, MAGIC_BOLT, CANNONBALL }

    private double x, y;
    private double snapTargetX, snapTargetY;
    private final Enemy  target;
    private final double speed;
    private final double damage;
    private final double splashRadius;
    private final double slowFactor;
    private final double slowDuration;
    private final ProjectileType type;

    /** For AoE: live reference to the enemy list supplied at creation. */
    private final List<Enemy> splashTargets;

    private boolean expired = false;
    private final Color color;
    private final int   drawRadius;

    // ── Single-target constructor ──────────────────────────────────────────
    public Projectile(double startX, double startY, Enemy target,
                      double speed, double damage,
                      double splashRadius, double slowFactor, double slowDuration,
                      ProjectileType type) {
        this(startX, startY, target, speed, damage,
             splashRadius, slowFactor, slowDuration, type, null);
    }

    // ── AoE constructor ───────────────────────────────────────────────────
    public Projectile(double startX, double startY, Enemy target,
                      double speed, double damage,
                      double splashRadius, double slowFactor, double slowDuration,
                      ProjectileType type, List<Enemy> splashTargets) {
        this.x             = startX;
        this.y             = startY;
        this.target        = target;
        this.snapTargetX   = target.getX();
        this.snapTargetY   = target.getY();
        this.speed         = speed;
        this.damage        = damage;
        this.splashRadius  = splashRadius;
        this.slowFactor    = slowFactor;
        this.slowDuration  = slowDuration;
        this.type          = type;
        this.splashTargets = splashTargets;

        switch (type) {
            case ARROW      -> { color = new Color(200, 160, 60); drawRadius = 5; }
            case MAGIC_BOLT -> { color = new Color(140, 80, 220); drawRadius = 7; }
            case CANNONBALL -> { color = new Color(60, 60, 60);   drawRadius = 9; }
            default         -> { color = Color.WHITE;             drawRadius = 5; }
        }
    }

    // ── Update ─────────────────────────────────────────────────────────────
    public void update(double dt) {
        if (expired) return;

        double tx   = target.isAlive() ? target.getX() : snapTargetX;
        double ty   = target.isAlive() ? target.getY() : snapTargetY;
        double dx   = tx - x;
        double dy   = ty - y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        double move = speed * dt;

        if (dist <= move + drawRadius) {
            onHit();
        } else {
            x += (dx / dist) * move;
            y += (dy / dist) * move;
        }
    }

    private void onHit() {
        if (splashRadius > 0 && splashTargets != null) {
            hitSplash(splashTargets);
        } else {
            applyTo(target);
        }
        expired = true;
    }

    public void hitSplash(List<Enemy> enemies) {
        double cx = target.getX();
        double cy = target.getY();
        for (Enemy e : enemies) {
            double dx = e.getX() - cx;
            double dy = e.getY() - cy;
            if (Math.sqrt(dx * dx + dy * dy) <= splashRadius) applyTo(e);
        }
    }

    private void applyTo(Enemy e) {
        if (!e.isAlive()) return;
        e.takeDamage(damage);
        if (slowFactor < 1.0) e.applySlow(slowFactor, slowDuration);
    }

    // ── Draw ───────────────────────────────────────────────────────────────
    public void draw(Graphics2D g2) {
        if (expired) return;
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 70));
        g2.fillOval((int)x - drawRadius - 3, (int)y - drawRadius - 3,
                    (drawRadius + 3) * 2, (drawRadius + 3) * 2);
        g2.setColor(color);
        g2.fillOval((int)x - drawRadius, (int)y - drawRadius,
                    drawRadius * 2, drawRadius * 2);
    }

    // ── Getters ────────────────────────────────────────────────────────────
    public boolean isExpired()      { return expired; }
    public ProjectileType getType() { return type; }
    public double getSplashRadius() { return splashRadius; }
    public Enemy getTarget()        { return target; }
}
