package com.siegeofaldoria.towers;

import java.awt.Color;
import java.util.List;

import com.siegeofaldoria.entities.Enemy;
import com.siegeofaldoria.entities.Projectile;
import com.siegeofaldoria.entities.Projectile.ProjectileType;
import com.siegeofaldoria.entities.Tower;

public class DruideTower extends Tower {
    
    public DruideTower(int col, int row) {
        super(col, row);
        name            = "Druide Tower";
        cost            = 90;
        range           = 150;
        damage          = 10;
        fireRate        = 0.7;
        projectileSpeed = 220;
        splashRadius    = 60;
        slowFactor      = 0.8;
        slowDuration    = 0.8;
        projType        = ProjectileType.DRUID_VINES;
        baseColor       = new Color(70, 250, 130);
        topColor        = new Color(90, 230, 20);
        specialCooldown    = 16.0;
        specialName        = "Enracinement";
        specialFlashRadius = 100;
        specialCost        = 50;
    }

    @Override
    protected void triggerSpecial(List<Enemy> enemies, List<Projectile> projectiles) {
        for (Enemy e : enemies) {
            if (!e.isAlive()) continue;
            double dx = e.getX() - getCenterX();
            double dy = e.getY() - getCenterY();
            if (Math.sqrt(dx * dx + dy * dy) <= range) {
                e.applySlow(0.1, 2.5);
            }
        }
    }

    @Override
    protected void shoot(Enemy target, List<Enemy> enemies, List<Projectile> projectiles) {
        Projectile p = new Projectile(
            getCenterX(), getCenterY(), target,
            projectileSpeed, damage,
            splashRadius, slowFactor, slowDuration,
            projType, enemies          // pass live enemy list for AoE splash
        );
        projectiles.add(p);
    }
}
