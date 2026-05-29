package com.siegeofaldoria;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.siegeofaldoria.entities.AlliedUnit;
import com.siegeofaldoria.entities.Enemy;
import com.siegeofaldoria.entities.Projectile;
import com.siegeofaldoria.entities.Tower;
import com.siegeofaldoria.map.GameMap;
import com.siegeofaldoria.map.WaveManager;
import com.siegeofaldoria.towers.Caserne;

/**
 * Main game controller — owns the loop, state, and all entity lists.
 */
public class Game implements Runnable {

    // ── Constants ──────────────────────────────────────────────────────────
    public static final int TILE_SIZE  = 48;
    public static final int MAP_COLS   = 20;
    public static final int MAP_ROWS   = 14;
    public static final int SIDEBAR_W  = 200;
    public static final int SCREEN_W   = TILE_SIZE * MAP_COLS + SIDEBAR_W;
    public static final int SCREEN_H   = TILE_SIZE * MAP_ROWS + 60; // +HUD bar

    // Level select card layout (shared with HUD and InputHandler)
    public static final int LEVEL_CARD_W   = 200;
    public static final int LEVEL_CARD_H   = 220;
    public static final int LEVEL_CARD_GAP = 24;
    public static final int LEVEL_CARD_X0  = (TILE_SIZE * MAP_COLS - (3 * LEVEL_CARD_W + 2 * LEVEL_CARD_GAP)) / 2;
    public static final int LEVEL_CARD_Y   = TILE_SIZE * MAP_ROWS / 2 - LEVEL_CARD_H / 2;

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
    private int currentLevel = 1;

    private final List<Tower>      towers      = new ArrayList<>();
    private final List<Enemy>      enemies     = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<AlliedUnit> alliedUnits = new ArrayList<>();

    // ── Player stats ──────────────────────────────────────────────────────
    private int gold      = 150;
    private int lives     = 20;
    private int score     = 0;
    private int gameSpeed = 1;

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

    /** Resets everything and goes back to the level selection screen. */
    public void newGame() {
        towers.clear();
        enemies.clear();
        projectiles.clear();
        alliedUnits.clear();
        gold      = 150;
        lives     = 20;
        score     = 0;
        gameSpeed = 1;
        currentLevel = 1;
        gameMap     = new GameMap(1);
        waveManager = new WaveManager(this);
        state = GameState.MENU;
    }

    /** Called when the player clicks a level card on the selection screen. */
    public void selectLevel(int level) {
        towers.clear();
        enemies.clear();
        projectiles.clear();
        alliedUnits.clear();
        gameSpeed = 1;
        gold  = 150;
        lives = 20;
        score = 0;
        currentLevel = level;
        gameMap      = new GameMap(level);
        waveManager  = new WaveManager(this);
        state = GameState.PREP;
    }

    /** Advances from MENU to the level selection screen. */
    public void goToLevelSelect() {
        if (state == GameState.MENU) state = GameState.LEVEL_SELECT;
    }

    // ── Game Loop ──────────────────────────────────────────────────────────
    @Override
    public void run() {
        long lastTime = System.nanoTime();

        while (running) {
            long now   = System.nanoTime();
            long delta = now - lastTime;
            lastTime   = now;

            update(delta / 1_000_000_000.0);
            panel.repaint();

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
            double eff = dt * gameSpeed;
            waveManager.update(eff);
            applyEnemyBlocking();
            updateEnemies(eff);
            updateTowers(eff);
            updateProjectiles(eff);
            updateAlliedUnits(eff);
            checkWaveComplete();
            checkGameOver();
        }
    }

    private void applyEnemyBlocking() {
        for (Enemy e : enemies) {
            if (!e.isAlive()) continue;
            boolean blocked = false;
            for (AlliedUnit u : alliedUnits) {
                if (!u.isAlive()) continue;
                double dx = u.getX() - e.getX();
                double dy = u.getY() - e.getY();
                if (Math.sqrt(dx * dx + dy * dy) < AlliedUnit.BLOCK_RANGE) {
                    blocked = true;
                    break;
                }
            }
            e.setBlocked(blocked);
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
            if (t instanceof Caserne) {
                alliedUnits.addAll(((Caserne) t).popPendingUnits());
            }
        }
    }

    private void updateAlliedUnits(double dt) {
        Iterator<AlliedUnit> it = alliedUnits.iterator();
        while (it.hasNext()) {
            AlliedUnit u = it.next();
            u.update(dt, enemies);
            if (!u.isAlive()) it.remove();
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

    // ── Public Actions ─────────────────────────────────────────────────────

    public boolean placeTower(Tower tower) {
        int cost = tower.getCost();
        if (gold < cost) return false;
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
        if (state == GameState.PREP || state == GameState.WAVE_COMPLETE) {
            state = GameState.PLAYING;
            waveManager.startNextWave();
        }
    }

    public void cycleSpeed() { gameSpeed = (gameSpeed % 3) + 1; }
    public int  getGameSpeed() { return gameSpeed; }

    public boolean spendGold(int amount) {
        if (gold < amount) return false;
        gold -= amount;
        return true;
    }

    public boolean buySpecial(Tower tower) {
        if (tower == null || tower.isSpecialUnlocked()) return false;
        if (tower.getSpecialCost() <= 0) return false;
        if (gold < tower.getSpecialCost()) return false;
        gold -= tower.getSpecialCost();
        tower.unlockSpecial();
        return true;
    }

    public void togglePause() {
        if (state == GameState.PLAYING)     state = GameState.PAUSED;
        else if (state == GameState.PAUSED) state = GameState.PLAYING;
    }

    // ── Getters ────────────────────────────────────────────────────────────
    public GamePanel    getPanel()        { return panel; }
    public GameState    getState()        { return state; }
    public GameMap      getGameMap()      { return gameMap; }
    public WaveManager  getWaveManager()  { return waveManager; }
    public List<Tower>      getTowers()      { return towers; }
    public List<Enemy>      getEnemies()     { return enemies; }
    public List<Projectile> getProjectiles() { return projectiles; }
    public List<AlliedUnit> getAlliedUnits()  { return alliedUnits; }
    public int getGold()         { return gold; }
    public int getLives()        { return lives; }
    public int getScore()        { return score; }
    public int getCurrentLevel() { return currentLevel; }

    public void setState(GameState s) { this.state = s; }
}
