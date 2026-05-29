package com.siegeofaldoria.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import com.siegeofaldoria.Game;

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
        new ShopItem("archer",  "Archer Tower",   60,  "Rapide · Cible unique",
                     new Color(130, 100, 50),  new Color(200, 170, 80)),
        new ShopItem("druide",  "Druide Tower",   90,  "Lianes · Ralentit AoE",
                     new Color(70, 250, 130),  new Color(90, 230, 20)),
        new ShopItem("mage",    "Mage Tower",    100,  "Magie · Ralentit",
                     new Color(70, 50, 130),   new Color(140, 100, 220)),
        new ShopItem("cannon",  "Cannon Tower",  130,  "AoE · Lent mais puissant",
                     new Color(80, 80, 80),    new Color(50, 50, 50)),
        new ShopItem("caserne", "Caserne",       110,  "Invoque des soldats",
                     new Color(110, 80, 40),   new Color(180, 140, 70)),
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
        g2.drawString("[1]-[5] select  [ESC] annuler", x + 6, 46 + ITEMS.length * (ITEM_H + 6) + 14);
        g2.drawString("[U] upgrade  [S] vendre", x + 6, 46 + ITEMS.length * (ITEM_H + 6) + 28);
        g2.drawString("[A] acheter capacite  [R] restart", x + 6, 46 + ITEMS.length * (ITEM_H + 6) + 42);

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
