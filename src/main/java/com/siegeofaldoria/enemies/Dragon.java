package com.siegeofaldoria.enemies;

import com.siegeofaldoria.entities.Enemy;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.List;

/**
 * Dragon — boss-tier enemy. High HP, fast, and drawn with wings.
 */
public class Dragon extends Enemy {

    private double wingAnim = 0; // wing flap angle

    public Dragon(List<int[]> path, double boost) {
        super(path);
        name        = "Dragon";
        maxHp       = (int)(900 * boost);
        hp          = maxHp;
        speed       = 80 * boost;
        goldReward  = 80;
        scoreReward = 150;
        color       = new Color(200, 50, 30);
        size        = 36;
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        wingAnim += dt * 6.0; // flap frequency
    }

    @Override
    public void draw(Graphics2D g2) {
        int px = (int) x;
        int py = (int) y;

        // Wings
        double wFlap = Math.sin(wingAnim) * 12;
        g2.setColor(new Color(180, 30, 20, 180));
        // Left wing
        GeneralPath leftWing = new GeneralPath();
        leftWing.moveTo(px, py);
        leftWing.curveTo(px - 28, py - 20 + wFlap, px - 22, py + 10, px - 8, py + 6);
        leftWing.closePath();
        g2.fill(leftWing);
        // Right wing
        GeneralPath rightWing = new GeneralPath();
        rightWing.moveTo(px, py);
        rightWing.curveTo(px + 28, py - 20 + wFlap, px + 22, py + 10, px + 8, py + 6);
        rightWing.closePath();
        g2.fill(rightWing);

        // Body (calls super draw logic via manual replication)
        int half = size / 2;
        g2.setColor(new Color(0, 0, 0, 60));
        g2.fillOval(px - half + 3, py - half + 3, size, size);
        g2.setColor(color);
        g2.fillOval(px - half, py - half, size, size);
        g2.setColor(color.darker());
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(px - half, py - half, size, size);

        // Eyes
        g2.setColor(Color.YELLOW);
        g2.fillOval(px - 8, py - 6, 6, 6);
        g2.fillOval(px + 2, py - 6, 6, 6);
        g2.setColor(Color.BLACK);
        g2.fillOval(px - 6, py - 5, 3, 3);
        g2.fillOval(px + 4, py - 5, 3, 3);

        // HP bar
        int barW = size + 8;
        int barH = 6;
        int bx = px - barW / 2;
        int by = py - half - 14;
        double ratio = hp / maxHp;
        g2.setColor(new Color(60, 0, 0));
        g2.fillRect(bx, by, barW, barH);
        g2.setColor(ratio > 0.5 ? new Color(50, 200, 50) :
                    ratio > 0.25 ? new Color(220, 180, 0) : new Color(200, 40, 40));
        g2.fillRect(bx, by, (int)(barW * ratio), barH);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRect(bx, by, barW, barH);
    }
}
