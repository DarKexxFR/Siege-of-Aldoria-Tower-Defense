package com.siegeofaldoria.ui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import com.siegeofaldoria.Game;
import com.siegeofaldoria.GameState;
import com.siegeofaldoria.entities.Tower;
import com.siegeofaldoria.towers.ArcherTower;
import com.siegeofaldoria.towers.CannonTower;
import com.siegeofaldoria.towers.Caserne;
import com.siegeofaldoria.towers.DruideTower;
import com.siegeofaldoria.towers.MageTower;

/**
 * Handles all mouse and keyboard input for the game.
 */
public class InputHandler implements MouseListener, MouseMotionListener, KeyListener {

    private final Game game;

    private int    hoverCol          = -1;
    private int    hoverRow          = -1;
    private String selectedTowerType = null;
    private Tower  selectedTower     = null;

    public InputHandler(Game game) {
        this.game = game;
    }

    // ── Mouse ──────────────────────────────────────────────────────────────
    @Override
    public void mouseClicked(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();

        // Level select — clicks anywhere on the map area
        if (game.getState() == GameState.LEVEL_SELECT) {
            handleLevelSelectClick(mx, my);
            return;
        }

        if (mx >= Game.TILE_SIZE * Game.MAP_COLS) return; // sidebar handled by TowerShop
        if (my >= Game.TILE_SIZE * Game.MAP_ROWS) return; // HUD bar

        int col = mx / Game.TILE_SIZE;
        int row = my / Game.TILE_SIZE;

        if (e.getButton() == MouseEvent.BUTTON1) {
            handleLeftClick(col, row);
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            handleRightClick();
        }
    }

    private void handleLevelSelectClick(int mx, int my) {
        for (int i = 0; i < 3; i++) {
            int cx = Game.LEVEL_CARD_X0 + i * (Game.LEVEL_CARD_W + Game.LEVEL_CARD_GAP);
            int cy = Game.LEVEL_CARD_Y;
            if (mx >= cx && mx <= cx + Game.LEVEL_CARD_W
                    && my >= cy && my <= cy + Game.LEVEL_CARD_H) {
                game.selectLevel(i + 1);
                return;
            }
        }
    }

    private void handleLeftClick(int col, int row) {
        if (selectedTowerType != null) {
            Tower t = createTower(selectedTowerType, col, row);
            if (t != null && game.placeTower(t)) {
                selectedTowerType = null; // auto-deselect after placement
            }
        } else {
            deselectAll();
            for (Tower t : game.getTowers()) {
                if (t.getCol() == col && t.getRow() == row) {
                    t.setSelected(true);
                    selectedTower = t;
                    break;
                }
            }
        }
    }

    private void handleRightClick() {
        selectedTowerType = null;
        deselectAll();
    }

    private void deselectAll() {
        if (selectedTower != null) selectedTower.setSelected(false);
        selectedTower = null;
    }

    private Tower createTower(String type, int col, int row) {
        return switch (type) {
            case "archer"  -> new ArcherTower(col, row);
            case "druide"  -> new DruideTower(col, row);
            case "mage"    -> new MageTower(col, row);
            case "cannon"  -> new CannonTower(col, row);
            case "caserne" -> new Caserne(col, row);
            default        -> null;
        };
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();
        if (mx < Game.TILE_SIZE * Game.MAP_COLS && my < Game.TILE_SIZE * Game.MAP_ROWS) {
            hoverCol = mx / Game.TILE_SIZE;
            hoverRow = my / Game.TILE_SIZE;
        } else {
            hoverCol = -1;
            hoverRow = -1;
        }
    }

    // ── Keyboard ───────────────────────────────────────────────────────────
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_SPACE -> {
                switch (game.getState()) {
                    case MENU          -> game.goToLevelSelect();
                    case PLAYING, PAUSED -> game.togglePause();
                    case PREP, WAVE_COMPLETE -> game.startNextWave();
                    default -> {}
                }
            }
            case KeyEvent.VK_1 -> selectedTowerType = "archer";
            case KeyEvent.VK_2 -> selectedTowerType = "druide";
            case KeyEvent.VK_3 -> selectedTowerType = "mage";
            case KeyEvent.VK_4 -> selectedTowerType = "cannon";
            case KeyEvent.VK_5 -> selectedTowerType = "caserne";
            case KeyEvent.VK_ESCAPE -> {
                if (game.getState() == GameState.LEVEL_SELECT) {
                    game.setState(GameState.MENU);
                } else {
                    selectedTowerType = null;
                    deselectAll();
                }
            }
            case KeyEvent.VK_S -> {
                if (selectedTower != null) {
                    game.sellTower(selectedTower);
                    selectedTower = null;
                }
            }
            case KeyEvent.VK_U -> {
                if (selectedTower != null && selectedTower.canUpgrade()) {
                    if (game.getGold() >= selectedTower.getUpgradeCost()) {
                        game.spendGold(selectedTower.getUpgradeCost());
                        selectedTower.upgrade();
                    }
                }
            }
            case KeyEvent.VK_A -> {
                if (selectedTower != null) game.buySpecial(selectedTower);
            }
            case KeyEvent.VK_X -> {
                if (game.getState() == GameState.PLAYING) game.cycleSpeed();
            }
            case KeyEvent.VK_R -> {
                if (game.getState() == GameState.GAME_OVER
                        || game.getState() == GameState.VICTORY) {
                    game.newGame();
                }
            }
        }
    }

    // ── Unused interface methods ───────────────────────────────────────────
    @Override public void mousePressed(MouseEvent e)  {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e)  {}
    @Override public void mouseExited(MouseEvent e)   {}
    @Override public void mouseDragged(MouseEvent e)  {}
    @Override public void keyReleased(KeyEvent e)     {}
    @Override public void keyTyped(KeyEvent e)        {}

    // ── Getters ────────────────────────────────────────────────────────────
    public int    getHoverCol()          { return hoverCol; }
    public int    getHoverRow()          { return hoverRow; }
    public String getSelectedTowerType() { return selectedTowerType; }
    public Tower  getSelectedTower()     { return selectedTower; }
    public void   setSelectedTowerType(String t) { selectedTowerType = t; deselectAll(); }
}
