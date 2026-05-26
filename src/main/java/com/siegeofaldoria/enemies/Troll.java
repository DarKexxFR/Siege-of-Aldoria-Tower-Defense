package com.siegeofaldoria.enemies;

import com.siegeofaldoria.entities.Enemy;

import java.awt.*;
import java.util.List;

/**
 * Troll — slow heavy unit with massive HP. Hard to kill before reaching the end.
 */
public class Troll extends Enemy {

    public Troll(List<int[]> path, double boost) {
        super(path);
        name        = "Troll";
        maxHp       = (int)(450 * boost);
        hp          = maxHp;
        speed       = 42 * boost;
        goldReward  = 30;
        scoreReward = 50;
        color       = new Color(130, 100, 60);
        size        = 34;
    }
}
