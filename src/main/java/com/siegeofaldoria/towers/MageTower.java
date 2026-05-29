package com.siegeofaldoria.towers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

import com.siegeofaldoria.entities.Enemy;
import com.siegeofaldoria.entities.Projectile;
import com.siegeofaldoria.entities.Projectile.ProjectileType;
import com.siegeofaldoria.entities.Tower;

/**
 * Mage Tower — slows enemies and deals magic damage. Moderate cost.
 * Cost: 100 gold | Range: 160px | Damage: 30 | Rate: 0.9/s | Slow: 50% for 2s
 * Lvl3 special: Nova — ralentit TOUS les ennemis à 30% pendant 3s (18s)
 */
public class MageTower extends Tower {

    public MageTower(int col, int row) {
        super(col, row);
        name            = "Mage Tower";
        cost            = 100;
        range           = 160;
        damage          = 30;
        fireRate        = 0.9;
        projectileSpeed = 220;
        splashRadius    = 0;
        slowFactor      = 0.5;
        slowDuration    = 2.0;
        projType        = ProjectileType.MAGIC_BOLT;
        baseColor       = new Color(70, 50, 130);
        topColor        = new Color(140, 100, 220);
        specialCooldown    = 18.0;
        specialName        = "Nova";
        specialFlashRadius = 180;
        specialCost        = 60;
    }

    @Override
    protected void triggerSpecial(List<Enemy> enemies, List<Projectile> projectiles) {
        for (Enemy e : enemies) {
            if (e.isAlive()) e.applySlow(0.3, 3.0);
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        super.draw(g2);
        int px = col * com.siegeofaldoria.Game.TILE_SIZE;
        int py = row * com.siegeofaldoria.Game.TILE_SIZE;
        int ts = com.siegeofaldoria.Game.TILE_SIZE;
        g2.setColor(new Color(140, 100, 220, 40));
        g2.fillOval(px + 6, py + 6, ts - 12, ts - 12);
    }
}
