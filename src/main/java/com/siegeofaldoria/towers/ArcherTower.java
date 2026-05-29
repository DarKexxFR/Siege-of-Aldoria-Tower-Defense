package com.siegeofaldoria.towers;

import com.siegeofaldoria.entities.Enemy;
import com.siegeofaldoria.entities.Projectile;
import com.siegeofaldoria.entities.Projectile.ProjectileType;
import com.siegeofaldoria.entities.Tower;

import java.awt.*;
import java.util.List;

/**
 * Archer Tower — fast single-target. Good all-rounder, cheap.
 * Cost: 60 gold | Range: 192px | Damage: 20 | Rate: 1.5/s
 * Lvl3 special: Rafale — tire 5 flèches sur 5 cibles simultanément (12s)
 */
public class ArcherTower extends Tower {

    public ArcherTower(int col, int row) {
        super(col, row);
        name            = "Archer Tower";
        cost            = 60;
        range           = 192;
        damage          = 20;
        fireRate        = 1.5;
        projectileSpeed = 280;
        splashRadius    = 0;
        slowFactor      = 1.0;
        slowDuration    = 0;
        projType        = ProjectileType.ARROW;
        baseColor       = new Color(130, 100, 50);
        topColor        = new Color(200, 170, 80);
        specialCooldown    = 12.0;
        specialName        = "Rafale";
        specialFlashRadius = 80;
        specialCost        = 40;
    }

    @Override
    protected void triggerSpecial(List<Enemy> enemies, List<Projectile> projectiles) {
        int count = 0;
        for (Enemy e : enemies) {
            if (!e.isAlive()) continue;
            double dx = e.getX() - getCenterX();
            double dy = e.getY() - getCenterY();
            if (Math.sqrt(dx * dx + dy * dy) > range) continue;
            projectiles.add(new Projectile(
                getCenterX(), getCenterY(), e,
                projectileSpeed * 1.6, damage * 1.5,
                0, 1.0, 0, projType
            ));
            if (++count >= 5) break;
        }
    }
}
