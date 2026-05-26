package com.siegeofaldoria;

import com.siegeofaldoria.map.GameMap;
import com.siegeofaldoria.map.WaveManager;
import com.siegeofaldoria.entities.Tower;
import com.siegeofaldoria.entities.Enemy;
import com.siegeofaldoria.entities.Projectile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Main game controller — owns the loop, state, and all entity lists.
 */
public class Game implements Runnable {

    // ── Constants ──────────────────────────────────────────────────────────
    public static final int TILE_SIZE   = 48;
    public static final int MAP_COLS    = 20;
    public static final int MAP_ROWS    = 14;
    public static final int SIDEBAR_W   = 200;
    public static final int SCREEN_W    = TILE_SIZE * MAP_COLS + SIDEBAR_W;
    public static final int SCREEN_H    = TILE_SIZE * MAP_ROWS + 60; // +HUD bar

    private static final int TARGET_FPS = 60;
    private static final long FRAME_NS  = 1_000_000_000L / TARGET_FPS;

    // ── Core ───────────────────────────────────────────────────────────────
    private final GamePanel panel;
    private Thread gameThread;
    private volatile boolean running;

    // ── Game state ────────────────────────────────────────────────────────
    private GameState state = GameState.MENU;
    private GameMap   gameMap;
    private WaveManager waveManager;

    private final List<Tower>      towers      = new ArrayList<>();
    private final List<Enemy>      enemies     = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();

    // ── Player stats ──────────────────────────────────────────────────────
    private int gold  = 150;
    private int lives = 20;
    private int score = 0;

    public Game() {
        panel = new GamePanel(this);
        newGame();
    }

    // ── Lifecycle ──────────────────────────────────────────────────────────
    public void start() {
        if (running) return;
        running = true;
        gameThread = new Thread(this, "GameLoop");
        gameThread.start();
    }

    public void stop() {
        running = false;
    }

    /** Resets everything for a fresh game. */
    public void newGame() {
        towers.clear();
        enemies.clear();
        projectiles.clear();
        gold  = 150;
        lives = 20;
        score = 0;
        gameMap     = new GameMap();
        waveManager = new WaveManager(this);
        state = GameState.MENU;
    }

    // ── Game Loop ──────────────────────────────────────────────────────────
    @Override
    public void run() {
        long lastTime = System.nanoTime();
        long timer    = 0;
        int  frames   = 0;

        while (running) {
            long now   = System.nanoTime();
            long delta = now - lastTime;
            lastTime   = now;

            update(delta / 1_000_000_000.0); // convert ns → seconds
            panel.repaint();

            frames++;
            timer += delta;
            if (timer >= 1_000_000_000L) {
                timer  -= 1_000_000_000L;
                frames  = 0;
            }

            // Cap to target FPS
            long elapsed = System.nanoTime() - now;
            long sleep   = (FRAME_NS - elapsed) / 1_000_000;
            if (sleep > 0) {
                try { Thread.sleep(sleep); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }
    }

    // ── Update ─────────────────────────────────────────────────────────────
    private void update(double dt) {
        if (state == GameState.PLAYING) {
            waveManager.update(dt);
            updateEnemies(dt);
            updateTowers(dt);
            updateProjectiles(dt);
            checkWaveComplete();
            checkGameOver();
        }
    }

    private void updateEnemies(double dt) {
        Iterator<Enemy> it = enemies.iterator();
        while (it.hasNext()) {
            Enemy e = it.next();
            e.update(dt);
            if (e.hasReachedEnd()) {
                lives--;
                it.remove();
            } else if (!e.isAlive()) {
                gold  += e.getGoldReward();
                score += e.getScoreReward();
                it.remove();
            }
        }
    }

    private void updateTowers(double dt) {
        for (Tower t : towers) {
            t.update(dt, enemies, projectiles);
        }
    }

    private void updateProjectiles(double dt) {
        Iterator<Projectile> it = projectiles.iterator();
        while (it.hasNext()) {
            Projectile p = it.next();
            p.update(dt);
            if (p.isExpired()) it.remove();
        }
    }

    private void checkWaveComplete() {
        if (waveManager.isWaveInProgress()) return;
        if (enemies.isEmpty()) {
            if (waveManager.hasMoreWaves()) {
                state = GameState.WAVE_COMPLETE;
            } else {
                state = GameState.VICTORY;
            }
        }
    }

    private void checkGameOver() {
        if (lives <= 0) {
            lives = 0;
            state = GameState.GAME_OVER;
        }
    }

    // ── Public Actions (called from input / UI) ────────────────────────────

    public boolean placeTower(Tower tower) {
        int cost = tower.getCost();
        if (gold < cost) return false;
        // Check tile is buildable and not occupied
        int col = tower.getCol();
        int row = tower.getRow();
        if (!gameMap.isBuildable(col, row)) return false;
        for (Tower t : towers) {
            if (t.getCol() == col && t.getRow() == row) return false;
        }
        gold -= cost;
        towers.add(tower);
        gameMap.setOccupied(col, row, true);
        return true;
    }

    public void sellTower(Tower tower) {
        if (towers.remove(tower)) {
            gold += tower.getSellValue();
            gameMap.setOccupied(tower.getCol(), tower.getRow(), false);
        }
    }

    public void startNextWave() {
        if (state == GameState.MENU || state == GameState.WAVE_COMPLETE) {
            state = GameState.PLAYING;
            waveManager.startNextWave();
        }
    }

    public void togglePause() {
        if (state == GameState.PLAYING)       state = GameState.PAUSED;
        else if (state == GameState.PAUSED)   state = GameState.PLAYING;
    }

    // ── Getters ────────────────────────────────────────────────────────────
    public GamePanel    getPanel()       { return panel; }
    public GameState    getState()       { return state; }
    public GameMap      getGameMap()     { return gameMap; }
    public WaveManager  getWaveManager() { return waveManager; }
    public List<Tower>      getTowers()      { return towers; }
    public List<Enemy>      getEnemies()     { return enemies; }
    public List<Projectile> getProjectiles() { return projectiles; }
    public int getGold()  { return gold; }
    public int getLives() { return lives; }
    public int getScore() { return score; }

    public void setState(GameState s) { this.state = s; }
}
