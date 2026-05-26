package com.siegeofaldoria;

import com.siegeofaldoria.entities.Enemy;
import com.siegeofaldoria.entities.Projectile;
import com.siegeofaldoria.entities.Tower;
import com.siegeofaldoria.towers.ArcherTower;
import com.siegeofaldoria.towers.CannonTower;
import com.siegeofaldoria.towers.MageTower;
import com.siegeofaldoria.ui.HUD;
import com.siegeofaldoria.ui.InputHandler;
import com.siegeofaldoria.ui.TowerShop;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Swing panel — owns rendering and wires up all UI components.
 */
public class GamePanel extends JPanel {

    private final Game         game;
    private final InputHandler input;
    private final TowerShop    shop;
    private final HUD          hud;

    // Off-screen buffer for smooth rendering
    private BufferedImage buffer;
    private Graphics2D    bufferG;

    public GamePanel(Game game) {
        this.game  = game;
        this.input = new InputHandler(game);
        this.shop  = new TowerShop(game, input);
        this.hud   = new HUD(game);

        setPreferredSize(new Dimension(Game.SCREEN_W, Game.SCREEN_H));
        setBackground(Color.BLACK);
        setDoubleBuffered(false); // We handle our own buffer

        addMouseListener(input);
        addMouseMotionListener(input);
        addKeyListener(input);
        addMouseListener(shop);
        setFocusable(true);
        requestFocusInWindow();
    }

    // ── Rendering ──────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        ensureBuffer();

        // Clear
        bufferG.setColor(Color.BLACK);
        bufferG.fillRect(0, 0, Game.SCREEN_W, Game.SCREEN_H);

        // Enable anti-aliasing
        bufferG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,    RenderingHints.VALUE_ANTIALIAS_ON);
        bufferG.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Draw layers
        game.getGameMap().draw(bufferG);
        drawTowers(bufferG);
        drawEnemies(bufferG);
        drawProjectiles(bufferG);
        drawPlacementPreview(bufferG);
        shop.draw(bufferG);
        hud.draw(bufferG);

        // Blit to screen
        g.drawImage(buffer, 0, 0, null);
    }

    private void ensureBuffer() {
        if (buffer == null || buffer.getWidth() != Game.SCREEN_W || buffer.getHeight() != Game.SCREEN_H) {
            buffer  = new BufferedImage(Game.SCREEN_W, Game.SCREEN_H, BufferedImage.TYPE_INT_ARGB);
            bufferG = buffer.createGraphics();
        }
    }

    private void drawTowers(Graphics2D g2) {
        for (Tower t : game.getTowers()) t.draw(g2);
    }

    private void drawEnemies(Graphics2D g2) {
        for (Enemy e : game.getEnemies()) e.draw(g2);
    }

    private void drawProjectiles(Graphics2D g2) {
        for (Projectile p : game.getProjectiles()) p.draw(g2);
    }

    private void drawPlacementPreview(Graphics2D g2) {
        String type = input.getSelectedTowerType();
        if (type == null) return;

        int col = input.getHoverCol();
        int row = input.getHoverRow();
        if (col < 0 || row < 0) return;

        boolean canPlace = game.getGameMap().isBuildable(col, row);
        boolean canAfford = true;

        Tower preview = switch (type) {
            case "archer" -> new ArcherTower(col, row);
            case "mage"   -> new MageTower(col, row);
            case "cannon" -> new CannonTower(col, row);
            default       -> null;
        };
        if (preview == null) return;

        canAfford = game.getGold() >= preview.getCost();

        // Tint the cell
        int px = col * Game.TILE_SIZE;
        int py = row * Game.TILE_SIZE;
        Color tint = (!canPlace || !canAfford)
                     ? new Color(220, 50, 50, 100)
                     : new Color(50, 220, 50, 80);
        g2.setColor(tint);
        g2.fillRect(px, py, Game.TILE_SIZE, Game.TILE_SIZE);

        // Range ring
        if (canPlace && canAfford) {
            preview.drawRangePreview(g2);
            preview.draw(g2);
        }
    }

    public InputHandler getInput() { return input; }
}
