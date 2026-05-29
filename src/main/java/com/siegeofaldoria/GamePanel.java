package com.siegeofaldoria;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import com.siegeofaldoria.entities.AlliedUnit;
import com.siegeofaldoria.entities.Enemy;
import com.siegeofaldoria.entities.Projectile;
import com.siegeofaldoria.entities.Tower;
import com.siegeofaldoria.towers.ArcherTower;
import com.siegeofaldoria.towers.CannonTower;
import com.siegeofaldoria.towers.Caserne;
import com.siegeofaldoria.towers.DruideTower;
import com.siegeofaldoria.towers.MageTower;
import com.siegeofaldoria.ui.HUD;
import com.siegeofaldoria.ui.InputHandler;
import com.siegeofaldoria.ui.TowerShop;

/**
 * Swing panel — owns rendering and wires up all UI components.
 */
public class GamePanel extends JPanel {

    private static final int POPUP_W = 215;
    private static final int POPUP_H = 310;

    private final Game         game;
    private final InputHandler input;
    private final TowerShop    shop;
    private final HUD          hud;

    private BufferedImage buffer;
    private Graphics2D    bufferG;

    public GamePanel(Game game) {
        this.game  = game;
        this.input = new InputHandler(game);
        this.shop  = new TowerShop(game, input);
        this.hud   = new HUD(game);

        setPreferredSize(new Dimension(Game.SCREEN_W, Game.SCREEN_H));
        setBackground(Color.BLACK);
        setDoubleBuffered(false);

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

        bufferG.setColor(Color.BLACK);
        bufferG.fillRect(0, 0, Game.SCREEN_W, Game.SCREEN_H);

        bufferG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,     RenderingHints.VALUE_ANTIALIAS_ON);
        bufferG.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        game.getGameMap().draw(bufferG);
        drawTowers(bufferG);
        drawAlliedUnits(bufferG);
        drawEnemies(bufferG);
        drawProjectiles(bufferG);
        drawPlacementPreview(bufferG);
        drawTowerPopup(bufferG);
        shop.draw(bufferG);
        hud.draw(bufferG);

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

    private void drawAlliedUnits(Graphics2D g2) {
        for (AlliedUnit u : game.getAlliedUnits()) u.draw(g2);
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

        Tower preview = switch (type) {
            case "archer"  -> new ArcherTower(col, row);
            case "druide"  -> new DruideTower(col, row);
            case "mage"    -> new MageTower(col, row);
            case "cannon"  -> new CannonTower(col, row);
            case "caserne" -> new Caserne(col, row);
            default        -> null;
        };
        if (preview == null) return;

        boolean canAfford = game.getGold() >= preview.getCost();

        int px = col * Game.TILE_SIZE;
        int py = row * Game.TILE_SIZE;
        Color tint = (!canPlace || !canAfford)
                     ? new Color(220, 50, 50, 100)
                     : new Color(50, 220, 50, 80);
        g2.setColor(tint);
        g2.fillRect(px, py, Game.TILE_SIZE, Game.TILE_SIZE);

        if (canPlace && canAfford) {
            preview.drawRangePreview(g2);
            preview.draw(g2);
        }
    }

    // ── Tower popup ────────────────────────────────────────────────────────
    private void drawTowerPopup(Graphics2D g2) {
        Tower t = input.getSelectedTower();
        if (t == null) return;

        int mapW = Game.TILE_SIZE * Game.MAP_COLS;
        int mapH = Game.TILE_SIZE * Game.MAP_ROWS;

        // Position: above the tower, clamped to map area
        int tx = (int) t.getCenterX();
        int ty = (int) t.getCenterY();

        int px = Math.max(4, Math.min(tx - POPUP_W / 2, mapW - POPUP_W - 4));
        int rawPy = ty - Game.TILE_SIZE / 2 - POPUP_H - 10;
        int py = Math.min(rawPy < 4 ? ty + Game.TILE_SIZE / 2 + 10 : rawPy, mapH - POPUP_H - 4);

        Color accent = t.getBaseColor();
        Color accentBright = t.getTopColor();
        int gold = game.getGold();

        // ── Shadow
        g2.setColor(new Color(0, 0, 0, 80));
        g2.fillRoundRect(px + 4, py + 4, POPUP_W, POPUP_H, 12, 12);

        // ── Background
        g2.setColor(new Color(22, 18, 14, 245));
        g2.fillRoundRect(px, py, POPUP_W, POPUP_H, 12, 12);

        // ── Accent header strip
        g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 200));
        g2.fillRoundRect(px, py, POPUP_W, 42, 12, 12);
        g2.fillRect(px, py + 26, POPUP_W, 16);

        // ── Border
        g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 180));
        g2.setStroke(new BasicStroke(1.8f));
        g2.drawRoundRect(px, py, POPUP_W, POPUP_H, 12, 12);

        // ── Tower name
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.setColor(Color.WHITE);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(t.getName(), px + 10, py + 17);

        // ── Level label (right side of header)
        String lvlStr = "Nv. " + t.getLevel();
        g2.setColor(new Color(255, 240, 160));
        g2.drawString(lvlStr, px + POPUP_W - fm.stringWidth(lvlStr) - 10, py + 17);

        // ── Level stars
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < t.getLevel(); i++)      stars.append("* ");
        for (int i = t.getLevel(); i < 3; i++)      stars.append(". ");
        g2.setColor(new Color(255, 215, 50));
        g2.drawString(stars.toString().trim(), px + 10, py + 36);

        int y = py + 56;

        // ── Gold disponible
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.setColor(new Color(150, 135, 110));
        g2.drawString("Or disponible", px + 10, y);
        g2.setColor(new Color(255, 215, 0));
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        String goldStr = "⬡ " + gold;
        g2.drawString(goldStr, px + POPUP_W - g2.getFontMetrics().stringWidth(goldStr) - 10, y);
        y += 4;

        // ── Separator
        drawSeparator(g2, px, y, accent);
        y += 10;

        // ── Stats
        y = drawStatRow(g2, px, y, "Degats",   fmt1(t.getDamage()), accentBright);
        y = drawStatRow(g2, px, y, "Portee",   (int) t.getRange() + " px", accentBright);
        y = drawStatRow(g2, px, y, "Cadence",  fmt1(t.getFireRate()) + " /s", accentBright);

        if (t.getSlowFactor() < 1.0) {
            int pct = (int)((1.0 - t.getSlowFactor()) * 100);
            y = drawStatRow(g2, px, y, "Ralentit", "-" + pct + "%  " + fmt1(t.getSlowDuration()) + "s", new Color(100, 180, 255));
        }
        if (t.getSplashRadius() > 0) {
            y = drawStatRow(g2, px, y, "AoE rayon", (int) t.getSplashRadius() + " px", new Color(255, 150, 80));
        }

        // ── Separator
        drawSeparator(g2, px, y, accent);
        y += 12;

        // ── Upgrade row
        if (t.canUpgrade()) {
            boolean canAfford = gold >= t.getUpgradeCost();
            Color upColor = canAfford ? new Color(80, 220, 80) : new Color(200, 70, 70);
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.setColor(upColor);
            g2.drawString("Ameliorer  ⬡ " + t.getUpgradeCost(), px + 10, y);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2.setColor(canAfford ? new Color(120, 200, 120) : new Color(160, 80, 80));
            g2.drawString("[U]", px + POPUP_W - 28, y);
        } else {
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.setColor(new Color(180, 160, 100));
            g2.drawString("NIVEAU MAX", px + 10, y);
        }
        y += 20;

        // ── Sell row
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.setColor(new Color(220, 100, 80));
        g2.drawString("Vendre  ⬡ " + t.getSellValue(), px + 10, y);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g2.setColor(new Color(180, 80, 60));
        g2.drawString("[S]", px + POPUP_W - 28, y);
        y += 16;

        // ── Separator
        drawSeparator(g2, px, y, accent);
        y += 12;

        // ── Compétence spéciale
        if (t.getSpecialCost() > 0) {
            drawSeparator(g2, px, y, accent);
            y += 12;

            if (!t.isSpecialUnlocked()) {
                // Option d'achat
                boolean canAffordSp = gold >= t.getSpecialCost();
                Color spColor = canAffordSp ? new Color(80, 210, 210) : new Color(200, 80, 80);
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                g2.setColor(spColor);
                g2.drawString("Capacite: " + t.getSpecialName(), px + 10, y);
                String spCost = "⬡ " + t.getSpecialCost();
                FontMetrics fmSp = g2.getFontMetrics();
                g2.drawString(spCost, px + POPUP_W - fmSp.stringWidth(spCost) - 10, y);
                y += 15;
                g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
                g2.setColor(canAffordSp ? new Color(100, 190, 190) : new Color(140, 70, 70));
                g2.drawString("[A] Acheter la capacite", px + 10, y);
                y += 14;
            } else if (t.getSpecialCooldown() > 0) {
                // Barre de progression (compétences actives)
                double remaining = t.getSpecialTimer();
                boolean ready = remaining <= 0;
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                g2.setColor(new Color(255, 215, 80));
                g2.drawString("SPECIAL: " + t.getSpecialName(), px + 10, y);
                String readyStr = ready ? "PRETE !" : String.format("%.1fs", remaining);
                g2.setFont(new Font("SansSerif", Font.BOLD, 10));
                g2.setColor(ready ? new Color(80, 255, 80) : new Color(160, 140, 110));
                FontMetrics fmSp = g2.getFontMetrics();
                g2.drawString(readyStr, px + POPUP_W - fmSp.stringWidth(readyStr) - 10, y);
                y += 14;
                double prog = 1.0 - remaining / t.getSpecialCooldown();
                int barW2 = POPUP_W - 20;
                g2.setColor(new Color(30, 25, 15));
                g2.fillRect(px + 10, y, barW2, 6);
                g2.setColor(ready ? new Color(80, 220, 80) : accentBright);
                g2.fillRect(px + 10, y, (int)(barW2 * Math.min(1.0, prog)), 6);
                g2.setColor(new Color(80, 60, 40));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRect(px + 10, y, barW2, 6);
                y += 18;
            } else {
                // Passif actif (ex: Fureur de la Caserne)
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                g2.setColor(new Color(255, 180, 50));
                g2.drawString(t.getSpecialName() + "  ACTIF", px + 10, y);
                y += 16;
            }

            drawSeparator(g2, px, y, accent);
            y += 12;
        }

        // ── ESC hint
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g2.setColor(new Color(110, 95, 75));
        String esc = "[ESC] Fermer  |  [Clic] Deplacer la vue";
        g2.drawString(esc, px + (POPUP_W - g2.getFontMetrics().stringWidth(esc)) / 2, y);
    }

    private int drawStatRow(Graphics2D g2, int px, int y, String label, String value, Color valueColor) {
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.setColor(new Color(150, 135, 110));
        g2.drawString(label, px + 10, y);
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2.setColor(valueColor);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(value, px + POPUP_W - fm.stringWidth(value) - 10, y);
        return y + 18;
    }

    private void drawSeparator(Graphics2D g2, int px, int y, Color accent) {
        g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 80));
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(px + 8, y + 4, px + POPUP_W - 8, y + 4);
    }

    private String fmt1(double v) {
        return String.valueOf(Math.round(v * 10) / 10.0);
    }

    public InputHandler getInput() { return input; }
}
