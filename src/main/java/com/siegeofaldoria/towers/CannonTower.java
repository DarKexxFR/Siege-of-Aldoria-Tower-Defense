package com.siegeofaldoria.towers;

import com.siegeofaldoria.entities.Enemy;
import com.siegeofaldoria.entities.Projectile;
import com.siegeofaldoria.entities.Projectile.ProjectileType;
import com.siegeofaldoria.entities.Tower;

import java.awt.*;
import java.util.List;

/**
 * Cannon Tower — slow fire rate but deals AoE splash damage.
 * Cost: 130 gold | Range: 176px | Damage: 70 | Rate: 0.5/s | Splash: 64px
 */
public class CannonTower extends Tower {

    public CannonTower(int col, int row) {
        super(col, row);
        name            = "Cannon Tower";
        cost            = 130;
        range           = 176;
        damage          = 70;
        fireRate        = 0.5;
        projectileSpeed = 180;
        splashRadius    = 64;
        slowFactor      = 1.0;
        slowDuration    = 0;
        projType        = ProjectileType.CANNONBALL;
        baseColor       = new Color(80, 80, 80);
        topColor        = new Color(50, 50, 50);
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

    @Override
    public void draw(Graphics2D g2) {
        super.draw(g2);
        // Extra barrel detail
        int cx = (int) getCenterX();
        int cy = (int) getCenterY();
        g2.setColor(new Color(40, 40, 40));
        g2.setStroke(new BasicStroke(4f));
        g2.drawLine(cx - 6, cy, cx + 10, cy);
    }
}
