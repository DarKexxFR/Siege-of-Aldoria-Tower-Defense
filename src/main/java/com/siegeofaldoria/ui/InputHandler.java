package com.siegeofaldoria.ui;

import com.siegeofaldoria.Game;
import com.siegeofaldoria.GameState;
import com.siegeofaldoria.entities.Tower;
import com.siegeofaldoria.towers.ArcherTower;
import com.siegeofaldoria.towers.CannonTower;
import com.siegeofaldoria.towers.MageTower;

import java.awt.event.*;

/**
 * Handles all mouse and keyboard input for the game.
 */
public class InputHandler implements MouseListener, MouseMotionListener, KeyListener {

    private final Game game;

    // Hover tile for placement preview
    private int hoverCol = -1;
    private int hoverRow = -1;

    // Which tower type is selected in the shop (null = none)
    private String selectedTowerType = null;

    // Tower selected on the map (for sell/upgrade)
    private Tower selectedTower = null;

    public InputHandler(Game game) {
        this.game = game;
    }

    // ── Mouse ──────────────────────────────────────────────────────────────
    @Override
    public void mouseClicked(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();

        // Clicks on the sidebar are handled by TowerShop directly via its own listener
        if (mx >= Game.TILE_SIZE * Game.MAP_COLS) return;

        // Clicks in the HUD bar at the bottom
        if (my >= Game.TILE_SIZE * Game.MAP_ROWS) return;

        int col = mx / Game.TILE_SIZE;
        int row = my / Game.TILE_SIZE;

        if (e.getButton() == MouseEvent.BUTTON1) {
            handleLeftClick(col, row);
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            handleRightClick(col, row);
        }
    }

    private void handleLeftClick(int col, int row) {
        if (selectedTowerType != null) {
            // Try to place a tower
            Tower t = createTower(selectedTowerType, col, row);
            if (t != null) {
                boolean placed = game.placeTower(t);
                if (!placed) {
                    // Flash feedback handled by GamePanel
                }
            }
        } else {
            // Select/deselect tower on map
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

    private void handleRightClick(int col, int row) {
        // Right-click deselects placement mode or tower
        selectedTowerType = null;
        deselectAll();
    }

    private void deselectAll() {
        if (selectedTower != null) selectedTower.setSelected(false);
        selectedTower = null;
    }

    private Tower createTower(String type, int col, int row) {
        return switch (type) {
            case "archer" -> new ArcherTower(col, row);
            case "mage"   -> new MageTower(col, row);
            case "cannon" -> new CannonTower(col, row);
            default       -> null;
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
                if (game.getState() == GameState.PLAYING || game.getState() == GameState.PAUSED) {
                    game.togglePause();
                } else if (game.getState() == GameState.MENU
                        || game.getState() == GameState.WAVE_COMPLETE) {
                    game.startNextWave();
                }
            }
            case KeyEvent.VK_1 -> selectedTowerType = "archer";
            case KeyEvent.VK_2 -> selectedTowerType = "mage";
            case KeyEvent.VK_3 -> selectedTowerType = "cannon";
            case KeyEvent.VK_ESCAPE -> {
                selectedTowerType = null;
                deselectAll();
            }
            case KeyEvent.VK_S -> {
                // Sell selected tower
                if (selectedTower != null) {
                    game.sellTower(selectedTower);
                    selectedTower = null;
                }
            }
            case KeyEvent.VK_U -> {
                // Upgrade selected tower
                if (selectedTower != null && selectedTower.canUpgrade()) {
                    int cost = selectedTower.getUpgradeCost();
                    if (game.getGold() >= cost) {
                        // Gold deduction handled externally — invoke via game method
                        // For now direct upgrade (game.upgradeTower would be cleaner)
                        selectedTower.upgrade();
                    }
                }
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

    // ── Getters for GamePanel ──────────────────────────────────────────────
    public int    getHoverCol()           { return hoverCol; }
    public int    getHoverRow()           { return hoverRow; }
    public String getSelectedTowerType()  { return selectedTowerType; }
    public Tower  getSelectedTower()      { return selectedTower; }
    public void   setSelectedTowerType(String t) { selectedTowerType = t; deselectAll(); }
}
