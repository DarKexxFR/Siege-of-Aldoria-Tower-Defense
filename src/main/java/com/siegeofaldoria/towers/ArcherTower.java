package com.siegeofaldoria.towers;

import com.siegeofaldoria.entities.Projectile.ProjectileType;
import com.siegeofaldoria.entities.Tower;

import java.awt.*;

/**
 * Archer Tower — fast single-target. Good all-rounder, cheap.
 * Cost: 60 gold | Range: 192px | Damage: 20 | Rate: 1.5/s
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
    }
}
