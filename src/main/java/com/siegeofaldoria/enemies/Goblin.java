package com.siegeofaldoria.enemies;

import com.siegeofaldoria.entities.Enemy;

import java.awt.*;
import java.util.List;

/**
 * Goblin — fast but fragile. Scouts that slip through gaps.
 */
public class Goblin extends Enemy {

    public Goblin(List<int[]> path, double boost) {
        super(path);
        name        = "Goblin";
        maxHp       = (int)(60 * boost);
        hp          = maxHp;
        speed       = 110 * boost;
        goldReward  = 8;
        scoreReward = 10;
        color       = new Color(120, 200, 70);
        size        = 22;
    }
}
