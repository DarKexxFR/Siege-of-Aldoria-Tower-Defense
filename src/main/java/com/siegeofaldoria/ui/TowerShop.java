package com.siegeofaldoria.ui;

import com.siegeofaldoria.Game;
import com.siegeofaldoria.entities.Tower;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Sidebar panel showing purchasable towers with cost and description.
 */
public class TowerShop implements MouseListener {

    private static final int ITEM_H  = 70;
    private static final int PADDING = 12;

    private final Game         game;
    private final InputHandler input;
    private final int          sidebarX;

    private static final ShopItem[] ITEMS = {
        new ShopItem("archer", "Archer Tower",  60,  "Fast · Single target",
                     new Color(130, 100, 50),  new Color(200, 170, 80)),
        new ShopItem("mage",   "Mage Tower",   100,  "Slows · Magic dmg",
                     new Color(70, 50, 130),   new Color(140, 100, 220)),
        new ShopItem("cannon", "Cannon Tower", 130,  "AoE splash · Slow rate",
                     new Color(80, 80, 80),    new Color(50, 50, 50)),
    };

    public TowerShop(Game game, InputHandler input) {
        this.game     = game;
        this.input    = input;
        this.sidebarX = Game.TILE_SIZE * Game.MAP_COLS;
    }

    // ── Draw ───────────────────────────────────────────────────────────────
    public void draw(Graphics2D g2) {
        int x = sidebarX;
        int w = Game.SIDEBAR_W;
        int h = Game.TILE_SIZE * Game.MAP_ROWS;

        // Background
        g2.setColor(new Color(30, 25, 20));
        g2.fillRect(x, 0, w, h);
        g2.setColor(new Color(80, 60, 40));
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(x, 0, x, h);

        // Title
        g2.setColor(new Color(230, 190, 100));
        g2.setFont(new Font("Serif", Font.BOLD, 16));
        g2.drawString("⚔  Towers", x + PADDING, 28);
        g2.setColor(new Color(80, 60, 40));
        g2.drawLine(x + PADDING, 36, x + w - PADDING, 36);

        // Tower items
        for (int i = 0; i < ITEMS.length; i++) {
            drawItem(g2, ITEMS[i], x, 46 + i * (ITEM_H + 6));
        }

        // Hotkey hint
        g2.setColor(new Color(120, 100, 80));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g2.drawString("[1] [2] [3] select · [ESC] cancel", x + 6, 46 + ITEMS.length * (ITEM_H + 6) + 14);
        g2.drawString("[S] sell · [U] upgrade · [R] restart", x + 6, 46 + ITEMS.length * (ITEM_H + 6) + 28);

        // Selected tower info panel
        Tower sel = input.getSelectedTower();
        if (sel != null) drawTowerInfo(g2, sel, x, w, h);
    }

    private void drawItem(Graphics2D g2, ShopItem item, int x, int y) {
        int w  = Game.SIDEBAR_W - PADDING * 2;
        boolean isSelected = item.id.equals(input.getSelectedTowerType());
        boolean canAfford  = game.getGold() >= item.cost;

        // Item background
        Color bg = isSelected ? new Color(80, 65, 30)
                 : canAfford  ? new Color(45, 38, 28)
                              : new Color(35, 30, 25);
        g2.setColor(bg);
        g2.fillRoundRect(x + PADDING, y, w, ITEM_H - 4, 8, 8);

        // Selection border
        if (isSelected) {
            g2.setColor(new Color(220, 180, 60));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(x + PADDING, y, w, ITEM_H - 4, 8, 8);
        }

        // Tower icon
        g2.setColor(item.baseColor);
        g2.fillRoundRect(x + PADDING + 6, y + 8, 32, 32, 6, 6);
        g2.setColor(item.topColor);
        g2.fillOval(x + PADDING + 12, y + 14, 20, 20);

        // Name
        g2.setColor(canAfford ? new Color(230, 210, 160) : new Color(120, 100, 80));
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.drawString(item.name, x + PADDING + 44, y + 20);

        // Description
        g2.setColor(new Color(160, 140, 110));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g2.drawString(item.description, x + PADDING + 44, y + 34);

        // Cost
        g2.setColor(canAfford ? new Color(255, 215, 0) : new Color(160, 80, 80));
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2.drawString("⬡ " + item.cost, x + PADDING + 44, y + 50);
    }

    private void drawTowerInfo(Graphics2D g2, Tower sel, int x, int w, int totalH) {
        int panelY = totalH - 160;
        int panelH = 155;

        g2.setColor(new Color(25, 20, 15, 230));
        g2.fillRoundRect(x + 4, panelY, w - 8, panelH, 10, 10);
        g2.setColor(new Color(80, 60, 40));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(x + 4, panelY, w - 8, panelH, 10, 10);

        g2.setColor(new Color(230, 190, 100));
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.drawString(sel.getName() + " (Lv." + sel.getLevel() + ")", x + 12, panelY + 20);

        g2.setColor(new Color(190, 170, 130));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.drawString("Range:  " + (int) sel.getRange() + " px", x + 12, panelY + 40);

        g2.setColor(new Color(255, 215, 0));
        g2.drawString("Sell:   ⬡ " + sel.getSellValue(), x + 12, panelY + 58);

        if (sel.canUpgrade()) {
            g2.setColor(game.getGold() >= sel.getUpgradeCost()
                        ? new Color(100, 220, 100) : new Color(180, 80, 80));
            g2.drawString("Upgrade: ⬡ " + sel.getUpgradeCost() + "  [U]", x + 12, panelY + 76);
        } else {
            g2.setColor(new Color(130, 120, 100));
            g2.drawString("MAX LEVEL", x + 12, panelY + 76);
        }

        g2.setColor(new Color(200, 80, 80));
        g2.drawString("[S] Sell tower", x + 12, panelY + 100);
        g2.setColor(new Color(120, 100, 80));
        g2.drawString("[ESC] Deselect", x + 12, panelY + 118);
    }

    // ── Mouse (shop clicks) ────────────────────────────────────────────────
    @Override
    public void mouseClicked(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();
        if (mx < sidebarX) return;

        for (int i = 0; i < ITEMS.length; i++) {
            int itemY = 46 + i * (ITEM_H + 6);
            if (my >= itemY && my <= itemY + ITEM_H - 4) {
                String id = ITEMS[i].id;
                if (id.equals(input.getSelectedTowerType())) {
                    input.setSelectedTowerType(null); // toggle off
                } else {
                    input.setSelectedTowerType(id);
                }
                return;
            }
        }
    }

    @Override public void mousePressed(MouseEvent e)  {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e)  {}
    @Override public void mouseExited(MouseEvent e)   {}

    // ── Inner record ──────────────────────────────────────────────────────
    private record ShopItem(String id, String name, int cost, String description,
                            Color baseColor, Color topColor) {}
}
