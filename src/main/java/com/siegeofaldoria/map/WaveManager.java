package com.siegeofaldoria.map;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.siegeofaldoria.Game;
import com.siegeofaldoria.enemies.Dragon;
import com.siegeofaldoria.enemies.Goblin;
import com.siegeofaldoria.enemies.Orc;
import com.siegeofaldoria.enemies.Slim;
import com.siegeofaldoria.enemies.Troll;

/**
 * Manages enemy waves — schedules spawns and tracks wave state.
 */
public class WaveManager {

    private final Game       game;
    private final List<int[]> path;

    private int     currentWave   = 0;
    private static final int TOTAL_WAVES = 10;

    private boolean waveInProgress = false;
    private double  spawnTimer     = 0;
    private double  spawnInterval  = 0.6; // seconds between enemies
    private Queue<SpawnEntry> spawnQueue = new LinkedList<>();

    public WaveManager(Game game) {
        this.game = game;
        this.path = game.getGameMap().getPath();
    }

    // ── Update ─────────────────────────────────────────────────────────────
    public void update(double dt) {
        if (!waveInProgress) return;

        spawnTimer -= dt;
        if (spawnTimer <= 0 && !spawnQueue.isEmpty()) {
            spawnEnemy(spawnQueue.poll());
            spawnTimer = spawnInterval;
        }

        if (spawnQueue.isEmpty()) {
            waveInProgress = false;
        }
    }

    private void spawnEnemy(SpawnEntry entry) {
        List<int[]> p = new ArrayList<>(path);
        switch (entry.type) {
            case "goblin" -> game.getEnemies().add(new Goblin(p, entry.boost));
            case "slim"   -> game.getEnemies().add(new Slim(p, entry.boost));
            case "orc"    -> game.getEnemies().add(new Orc(p,  entry.boost));
            case "troll"  -> game.getEnemies().add(new Troll(p, entry.boost));
            case "dragon" -> game.getEnemies().add(new Dragon(p, entry.boost));
        }
    }

    // ── Wave start ─────────────────────────────────────────────────────────
    public void startNextWave() {
        if (currentWave >= TOTAL_WAVES) return;
        currentWave++;
        spawnQueue.clear();
        buildWave(currentWave);
        waveInProgress = true;
        spawnTimer = 0;
    }

    /**
     * Defines enemy composition per wave.
     * boost = HP/speed multiplier applied per wave for scaling difficulty.
     */
    private void buildWave(int wave) {
        double boost = 1.0 + (wave - 1) * 0.18;
        spawnInterval = Math.max(0.25, 0.6 - wave * 0.03);

        switch (wave) {
            case 1  -> { enqueue("slim", 4, boost); enqueue("goblin", 2, boost); }
            case 2  -> { enqueue("slim", 6, boost); enqueue("orc", 3, boost);  enqueue("orc", 3, boost);}
            case 3  -> { enqueue("slim", 7, boost); enqueue("goblin", 5, boost); enqueue("orc", 5, boost); }
            case 4  -> { enqueue("orc", 8, boost); enqueue("troll", 2, boost); }
            case 5  -> { enqueue("goblin", 6, boost); enqueue("orc", 5, boost); enqueue("troll", 3, boost); }
            case 6  -> { enqueue("orc", 6, boost); enqueue("troll", 5, boost); }
            case 7  -> { enqueue("goblin", 8, boost); enqueue("troll", 5, boost); enqueue("dragon", 1, boost); }
            case 8  -> { enqueue("orc", 8, boost); enqueue("troll", 4, boost); enqueue("dragon", 2, boost); }
            case 9  -> { enqueue("orc", 6, boost); enqueue("troll", 6, boost); enqueue("dragon", 3, boost); }
            case 10 -> { enqueue("goblin", 10, boost); enqueue("orc", 8, boost);
                         enqueue("troll", 6, boost);   enqueue("dragon", 4, boost); }
        }
    }

    private void enqueue(String type, int count, double boost) {
        for (int i = 0; i < count; i++) spawnQueue.add(new SpawnEntry(type, boost));
    }

    // ── Queries ────────────────────────────────────────────────────────────
    public boolean isWaveInProgress() { return waveInProgress || !spawnQueue.isEmpty(); }
    public boolean hasMoreWaves()     { return currentWave < TOTAL_WAVES; }
    public int getCurrentWave()       { return currentWave; }
    public int getTotalWaves()        { return TOTAL_WAVES; }

    // ── Inner types ────────────────────────────────────────────────────────
    private record SpawnEntry(String type, double boost) {}
}
