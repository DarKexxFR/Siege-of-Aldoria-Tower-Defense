package com.siegeofaldoria.towers;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.siegeofaldoria.Game;
import com.siegeofaldoria.entities.AlliedUnit;
import com.siegeofaldoria.entities.Enemy;
import com.siegeofaldoria.entities.Projectile;
import com.siegeofaldoria.entities.Tower;

/**
 * Caserne — maintient une escouade permanente de soldats mêlée.
 *   Lvl1: 3 soldats  |  Lvl2: 4  |  Lvl3: 5
 *   Capacité Fureur (80g): soldats renforcés (2× HP, 1.5× dégâts).
 *   Respawn automatique après 5s.
 */
public class Caserne extends Tower {

    // Offsets des 5 slots autour du centre de la caserne (px)
    private static final double[] OFF_X = {  0, -30,  30, -20,  20 };
    private static final double[] OFF_Y = { 32,  22,  22, -26, -26 };

    private static final double RESPAWN_TIME = 15.0;
    private static final int    MAX_SQUAD    = 5;

    private final AlliedUnit[] squad         = new AlliedUnit[MAX_SQUAD];
    private final double[]     respawnTimers = new double[MAX_SQUAD]; // 0 = spawn immédiat
    private final List<AlliedUnit> pendingUnits = new ArrayList<>();

    public Caserne(int col, int row) {
        super(col, row);
        name            = "Caserne";
        cost            = 110;
        range           = 55;          // portée d'attaque des unités (affichage)
        damage          = 12;          // dégâts des unités
        fireRate        = 0;
        projectileSpeed = 0;
        splashRadius    = 0;
        slowFactor      = 1.0;
        slowDuration    = 0;
        baseColor       = new Color(110, 80, 40);
        topColor        = new Color(180, 140, 70);
        specialName     = "Fureur";
        specialCost     = 80;
        // specialCooldown = 0 → passif, pas de timer
    }

    // ── Pas de tir ────────────────────────────────────────────────────────
    @Override protected Enemy acquireTarget(List<Enemy> enemies) { return null; }

    // ── Gestion de l'escouade ─────────────────────────────────────────────
    @Override
    public void update(double dt, List<Enemy> enemies, List<Projectile> projectiles) {
        int size = squadSize();
        for (int i = 0; i < size; i++) {
            if (squad[i] == null || !squad[i].isAlive()) {
                squad[i] = null;
                if (respawnTimers[i] <= 0) {
                    AlliedUnit u = spawnUnit(i);
                    squad[i] = u;
                    pendingUnits.add(u);
                    respawnTimers[i] = RESPAWN_TIME;
                } else {
                    respawnTimers[i] -= dt;
                }
            }
        }
    }

    private int squadSize() {
        return switch (level) { case 3 -> 5; case 2 -> 4; default -> 3; };
    }

    private AlliedUnit spawnUnit(int slot) {
        double ux = getCenterX() + OFF_X[slot];
        double uy = getCenterY() + OFF_Y[slot];
        return new AlliedUnit(ux, uy, specialUnlocked, damage, range);
    }

    /** Game.updateTowers() appelle ceci chaque frame pour récupérer les nouveaux soldats. */
    public List<AlliedUnit> popPendingUnits() {
        if (pendingUnits.isEmpty()) return Collections.emptyList();
        List<AlliedUnit> copy = new ArrayList<>(pendingUnits);
        pendingUnits.clear();
        return copy;
    }

    // ── Upgrade ───────────────────────────────────────────────────────────
    @Override
    public void upgrade() {
        if (!canUpgrade()) return;
        level++;
        damage *= 1.25;    // soldats plus forts à chaque niveau
        // Nouveau slot → respawn immédiat
        int size = squadSize();
        for (int i = 0; i < size; i++) {
            if (squad[i] == null) respawnTimers[i] = 0;
        }
    }

    // ── Fureur : remplace immédiatement tous les soldats ─────────────────
    @Override
    public void unlockSpecial() {
        super.unlockSpecial();
        for (int i = 0; i < MAX_SQUAD; i++) {
            if (squad[i] != null) {
                squad[i].kill();
                squad[i] = null;
            }
            respawnTimers[i] = 0; // respawn immédiat en version Fury
        }
    }

    // ── Affichage ─────────────────────────────────────────────────────────
    @Override
    public void draw(Graphics2D g2) {
        int px = col * Game.TILE_SIZE;
        int py = row * Game.TILE_SIZE;
        int ts = Game.TILE_SIZE;

        // Socle
        g2.setColor(new Color(75, 55, 30));
        g2.fillRoundRect(px + 2, py + 2, ts - 4, ts - 4, 6, 6);

        // Murs
        g2.setColor(baseColor);
        g2.fillRect(px + 6, py + 12, ts - 12, ts - 16);

        // Créneaux
        g2.setColor(topColor);
        for (int i = 0; i < 3; i++) g2.fillRect(px + 7 + i * 13, py + 5, 9, 9);

        // Porche
        g2.setColor(new Color(30, 18, 6));
        g2.fillRoundRect(px + ts / 2 - 7, py + ts - 20, 14, 16, 4, 4);

        // Bordure
        g2.setColor(baseColor.darker());
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(px + 2, py + 2, ts - 4, ts - 4, 6, 6);

        // Pips de niveau
        for (int i = 0; i < level - 1; i++) {
            g2.setColor(Color.YELLOW);
            g2.fillOval(px + 7 + i * 7, py + ts - 10, 5, 5);
        }

        // Étoile Fureur
        if (specialUnlocked) {
            g2.setColor(new Color(255, 170, 40));
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.drawString("★", px + ts - 16, py + 14);
        }

        // Anneau de sélection
        if (selected) {
            g2.setColor(new Color(255, 255, 100, 180));
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawOval((int)(getCenterX() - range), (int)(getCenterY() - range),
                        (int)(range * 2), (int)(range * 2));
        }
    }

    // ── Popup : stat labels adaptés ───────────────────────────────────────
    @Override public double getFireRate()     { return 1.0 / RESPAWN_TIME; } // respawn/s pour affichage
    @Override public int    getSellValue()    { return (int)(cost * 0.6); }
}
