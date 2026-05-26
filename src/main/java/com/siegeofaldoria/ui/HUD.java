package com.siegeofaldoria.ui;

import com.siegeofaldoria.Game;
import com.siegeofaldoria.GameState;
import com.siegeofaldoria.map.WaveManager;

import java.awt.*;

/**
 * Draws the HUD bar at the bottom and overlay screens (menu, pause, game over).
 */
public class HUD {

    private final Game game;

    private static final Color BAR_BG   = new Color(20, 16, 12);
    private static final Color GOLD_C   = new Color(255, 215, 0);
    private static final Color LIFE_C   = new Color(220, 60, 60);
    private static final Color WAVE_C   = new Color(100, 200, 255);
    private static final Color SCORE_C  = new Color(180, 255, 180);
    private static final Font  LABEL_F  = new Font("SansSerif", Font.BOLD, 13);
    private static final Font  VALUE_F  = new Font("SansSerif", Font.PLAIN, 13);

    public HUD(Game game) {
        this.game = game;
    }

    // ── Draw ───────────────────────────────────────────────────────────────
    public void draw(Graphics2D g2) {
        drawBottomBar(g2);
        drawOverlay(g2);
    }

    private void drawBottomBar(Graphics2D g2) {
        int barY = Game.TILE_SIZE * Game.MAP_ROWS;
        int barW = Game.TILE_SIZE * Game.MAP_COLS;
        int barH = 60;

        // Background
        g2.setColor(BAR_BG);
        g2.fillRect(0, barY, barW, barH);
        g2.setColor(new Color(80, 60, 40));
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(0, barY, barW, barY);

        WaveManager wm = game.getWaveManager();
        int col = 20;

        // Gold
        drawStat(g2, "⬡ Gold", String.valueOf(game.getGold()), GOLD_C, col, barY + 22);
        col += 140;

        // Lives
        drawStat(g2, "♥ Lives", String.valueOf(game.getLives()), LIFE_C, col, barY + 22);
        col += 140;

        // Wave
        String waveStr = wm.getCurrentWave() + " / " + wm.getTotalWaves();
        drawStat(g2, "⚔ Wave", waveStr, WAVE_C, col, barY + 22);
        col += 160;

        // Score
        drawStat(g2, "★ Score", String.valueOf(game.getScore()), SCORE_C, col, barY + 22);
        col += 160;

        // State hint
        String hint = getStateHint();
        g2.setColor(new Color(160, 140, 100));
        g2.setFont(new Font("SansSerif", Font.ITALIC, 11));
        g2.drawString(hint, col, barY + 22);

        // Second row — wave progress bar
        if (wm.isWaveInProgress()) {
            drawWaveProgressBar(g2, barY + 38, barW);
        } else {
            g2.setColor(new Color(100, 180, 100));
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            String msg = wm.hasMoreWaves() ? "▶ Press SPACE to start Wave " + (wm.getCurrentWave() + 1)
                                           : "All waves complete!";
            g2.drawString(msg, 20, barY + 50);
        }
    }

    private void drawStat(Graphics2D g2, String label, String value, Color valueColor, int x, int y) {
        g2.setColor(new Color(160, 140, 110));
        g2.setFont(LABEL_F);
        g2.drawString(label + ":", x, y);
        g2.setColor(valueColor);
        g2.setFont(VALUE_F);
        g2.drawString(value, x + 10, y + 16);
    }

    private void drawWaveProgressBar(Graphics2D g2, int y, int barW) {
        // Simple animated fill — just draw a pulsing stripe
        g2.setColor(new Color(40, 30, 20));
        g2.fillRect(20, y + 4, barW - 40, 10);
        g2.setColor(new Color(100, 180, 255, 160));
        g2.fillRect(20, y + 4, (int)((barW - 40) * 0.5), 10); // placeholder fill
        g2.setColor(new Color(60, 100, 140));
        g2.drawRect(20, y + 4, barW - 40, 10);
    }

    private String getStateHint() {
        return switch (game.getState()) {
            case PLAYING       -> "[SPACE] Pause  [ESC] Cancel placement";
            case PAUSED        -> "[SPACE] Resume";
            case WAVE_COMPLETE -> "Wave complete! ▶ [SPACE] Next wave";
            case GAME_OVER     -> "[R] Restart";
            case VICTORY       -> "Victory! [R] Play again";
            default            -> "[SPACE] Start";
        };
    }

    // ── Overlays ───────────────────────────────────────────────────────────
    private void drawOverlay(Graphics2D g2) {
        GameState state = game.getState();
        switch (state) {
            case MENU        -> drawMenuOverlay(g2);
            case PAUSED      -> drawPauseOverlay(g2);
            case GAME_OVER   -> drawGameOverOverlay(g2);
            case VICTORY     -> drawVictoryOverlay(g2);
            default          -> {}
        }
    }

    private void drawMenuOverlay(Graphics2D g2) {
        int w = Game.TILE_SIZE * Game.MAP_COLS;
        int h = Game.TILE_SIZE * Game.MAP_ROWS;

        // Semi-transparent dark veil
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRect(0, 0, w, h);

        // Title
        g2.setFont(new Font("Serif", Font.BOLD, 52));
        String title = "Siege of Aldoria";
        FontMetrics fm = g2.getFontMetrics();
        int tx = (w - fm.stringWidth(title)) / 2;
        // Shadow
        g2.setColor(new Color(80, 40, 0));
        g2.drawString(title, tx + 3, h / 2 - 40 + 3);
        g2.setColor(new Color(230, 190, 80));
        g2.drawString(title, tx, h / 2 - 40);

        // Subtitle
        g2.setFont(new Font("SansSerif", Font.PLAIN, 18));
        String sub = "A Tower Defense Game";
        g2.setColor(new Color(180, 160, 120));
        g2.drawString(sub, (w - g2.getFontMetrics().stringWidth(sub)) / 2, h / 2 + 10);

        // Prompt
        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        String prompt = "▶  Press SPACE to begin";
        g2.setColor(new Color(100, 200, 100));
        g2.drawString(prompt, (w - g2.getFontMetrics().stringWidth(prompt)) / 2, h / 2 + 60);
    }

    private void drawPauseOverlay(Graphics2D g2) {
        drawDimOverlay(g2, "⏸  PAUSED", new Color(200, 200, 255), "[SPACE] Resume");
    }

    private void drawGameOverOverlay(Graphics2D g2) {
        drawDimOverlay(g2, "☠  GAME OVER", new Color(220, 60, 60),
                       "Score: " + game.getScore() + "   [R] Restart");
    }

    private void drawVictoryOverlay(Graphics2D g2) {
        drawDimOverlay(g2, "🏆  VICTORY!", new Color(255, 215, 0),
                       "Score: " + game.getScore() + "   [R] Play Again");
    }

    private void drawDimOverlay(Graphics2D g2, String heading, Color headColor, String sub) {
        int w = Game.TILE_SIZE * Game.MAP_COLS;
        int h = Game.TILE_SIZE * Game.MAP_ROWS;
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRect(0, 0, w, h);

        g2.setFont(new Font("Serif", Font.BOLD, 42));
        FontMetrics fm = g2.getFontMetrics();
        int hx = (w - fm.stringWidth(heading)) / 2;
        g2.setColor(Color.BLACK);
        g2.drawString(heading, hx + 2, h / 2 - 20 + 2);
        g2.setColor(headColor);
        g2.drawString(heading, hx, h / 2 - 20);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
        g2.setColor(new Color(200, 180, 140));
        int sx = (w - g2.getFontMetrics().stringWidth(sub)) / 2;
        g2.drawString(sub, sx, h / 2 + 30);
    }
}
