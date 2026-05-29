package com.siegeofaldoria.entities;

import java.awt.*;
import java.util.List;

/**
 * Soldat invoqué par la Caserne. Attaque les ennemis en portée et bloque leur avancée.
 * Géré par Caserne (respawn automatique à la mort).
 */
public class AlliedUnit {

    private double x, y;
    private double hp, maxHp;
    private double damage;
    private final double attackRange;
    private double attackCooldown = 0;
    private static final double ATTACK_RATE   = 1.2;  // attaques/s
    private static final double CONTACT_DPS   = 20.0;
    public  static final double BLOCK_RANGE   = 45.0; // ennemi bloqué ET prend des dégâts de contact
    private static final double CONTACT_RANGE = BLOCK_RANGE;

    private boolean alive = true;
    private final boolean fury;

    public AlliedUnit(double x, double y, boolean fury, double baseDamage, double baseRange) {
        this.x           = x;
        this.y           = y;
        this.fury        = fury;
        this.attackRange = baseRange;
        this.damage      = fury ? baseDamage * 1.5 : baseDamage;
        this.maxHp       = fury ? 120 : 60;
        this.hp          = maxHp;
    }

    public void update(double dt, List<Enemy> enemies) {
        if (!alive) return;

        if (attackCooldown > 0) attackCooldown -= dt;

        // Attaque l'ennemi le plus proche en portée
        Enemy nearest = null;
        double nearestDist = Double.MAX_VALUE;
        for (Enemy e : enemies) {
            if (!e.isAlive()) continue;
            double dx = e.getX() - x, dy = e.getY() - y;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist < attackRange && dist < nearestDist) {
                nearestDist = dist;
                nearest = e;
            }
        }
        if (nearest != null && attackCooldown <= 0) {
            nearest.takeDamage(damage);
            attackCooldown = 1.0 / ATTACK_RATE;
        }

        // Dégâts de contact des ennemis proches
        for (Enemy e : enemies) {
            if (!e.isAlive()) continue;
            double dx = e.getX() - x, dy = e.getY() - y;
            if (Math.sqrt(dx * dx + dy * dy) < CONTACT_RANGE) {
                hp -= CONTACT_DPS * dt;
                break;
            }
        }
        if (hp <= 0) alive = false;
    }

    public void draw(Graphics2D g2) {
        if (!alive) return;
        int cx = (int) x, cy = (int) y;

        // Corps
        Color body = fury ? new Color(255, 160, 30) : new Color(60, 140, 255);
        Color rim  = fury ? new Color(180, 90, 0)   : new Color(20, 70, 160);
        g2.setColor(rim);
        g2.fillOval(cx - 11, cy - 11, 22, 22);
        g2.setColor(body);
        g2.fillOval(cx - 9,  cy - 9,  18, 18);

        // Épée
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(cx - 4, cy + 5, cx + 4, cy - 5);
        g2.drawLine(cx - 4, cy - 2, cx + 4, cy - 2);

        // Barre de vie
        int barW = 20, bx = cx - barW / 2, by = cy - 16;
        g2.setColor(new Color(40, 40, 40, 180));
        g2.fillRect(bx, by, barW, 3);
        double ratio = hp / maxHp;
        Color hpC = ratio > 0.5 ? new Color(80, 200, 80)
                  : ratio > 0.25 ? new Color(220, 200, 40)
                  : new Color(220, 60, 60);
        g2.setColor(hpC);
        g2.fillRect(bx, by, (int)(barW * ratio), 3);

        if (fury) {
            g2.setColor(new Color(255, 160, 30, 45));
            g2.fillOval(cx - 15, cy - 15, 30, 30);
        }
    }

    /** Force la mort de l'unité (ex: remplacement Fureur). */
    public void kill() { alive = false; hp = 0; }

    public boolean isAlive() { return alive; }
    public double  getX()    { return x; }
    public double  getY()    { return y; }
}
