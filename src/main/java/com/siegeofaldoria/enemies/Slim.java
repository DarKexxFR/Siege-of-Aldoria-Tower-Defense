package com.siegeofaldoria.enemies;

import java.awt.Color;
import java.util.List;

import com.siegeofaldoria.entities.Enemy;

public class Slim extends Enemy{

    public Slim(List<int[]> path, double boost) {
        super(path);
        name        = "Slim";
        maxHp       = (int)(45 * boost);
        hp          = maxHp;
        speed       = 75 * boost;
        goldReward  = 6;
        scoreReward = 7;
        color       = new Color(50, 125, 230);
        size        = 18;
    }
}
