package com.siegeofaldoria.enemies;

import com.siegeofaldoria.entities.Enemy;

import java.awt.*;
import java.util.List;

/**
 * Orc — balanced tank. Average speed, moderate HP.
 */
public class Orc extends Enemy {

    public Orc(List<int[]> path, double boost) {
        super(path);
        name        = "Orc";
        maxHp       = (int)(180 * boost);
        hp          = maxHp;
        speed       = 70 * boost;
        goldReward  = 15;
        scoreReward = 20;
        color       = new Color(60, 160, 60);
        size        = 28;
    }
}
