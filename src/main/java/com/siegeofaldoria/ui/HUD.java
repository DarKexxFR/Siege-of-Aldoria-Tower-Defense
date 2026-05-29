package com.siegeofaldoria.ui;

import com.siegeofaldoria.Game;
import com.siegeofaldoria.map.WaveManager;

import java.awt.*;

/**
 * Draws the HUD bar at the bottom and overlay screens (menu, level select, prep, pause, game over).
 */
public class HUD {

    private final Game game;

    private static final Color BAR_BG  = new Color(20, 16, 12);
    private static final Color GOLD_C  = new Color(255, 215, 0);
    private static final Color LIFE_C  = new Color(220, 60, 60);
    private static final Color WAVE_C  = new Color(100, 200, 255);
    private static final Color SCORE_C = new Color(180, 255, 180);
    private static final Font  LABEL_F = new Font("SansSerif", Font.BOLD, 13);
    private static final Font  VALUE_F = new Font("SansSerif", Font.PLAIN, 13);

    // Level card metadata
    private static final String[] LEVEL_NAMES = {"Foret d'Aldoria", "Desert Brulant", "Forteresse"};
    private static final String[] LEVEL_DIFF  = {"Facile", "Moyen", "Difficile"};
    private static final String[] LEVEL_DESC  = {"Chemin en S", "Serpentin vertical", "Zigzag complexe"};
    private static final Color[]  LEVEL_COLOR = {
        new Color(60, 130, 50),
        new Color(190, 130, 40),
        new Color(90, 90, 110)
    };
    private static final int[] DIFF_STARS = {1, 2, 3};

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

        g2.setColor(BAR_BG);
        g2.fillRect(0, barY, barW, barH);
        g2.setColor(new Color(80, 60, 40));
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(0, barY, barW, barY);

        WaveManager wm = game.getWaveManager();
        int col = 20;

        drawStat(g2, "Gold", String.valueOf(game.getGold()), GOLD_C, col, barY + 22);
        col += 140;
        drawStat(g2, "Vies", String.valueOf(game.getLives()), LIFE_C, col, barY + 22);
        col += 140;
        drawStat(g2, "Vague", wm.getCurrentWave() + " / " + wm.getTotalWaves(), WAVE_C, col, barY + 22);
        col += 160;
        drawStat(g2, "Score", String.valueOf(game.getScore()), SCORE_C, col, barY + 22);
        col += 140;

        // Indicateur de vitesse
        int spd = game.getGameSpeed();
        Color spdColor = spd == 1 ? new Color(180, 180, 180)
                       : spd == 2 ? new Color(255, 200, 60)
                       : new Color(255, 90, 60);
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.setColor(spdColor);
        g2.drawString("x" + spd, col, barY + 24);
        col += 44;

        g2.setColor(new Color(160, 140, 100));
        g2.setFont(new Font("SansSerif", Font.ITALIC, 11));
        g2.drawString(getStateHint(), col, barY + 22);

        if (wm.isWaveInProgress()) {
            drawWaveProgressBar(g2, barY + 38, barW);
        } else {
            g2.setColor(new Color(100, 180, 100));
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            String msg = switch (game.getState()) {
                case PREP          -> "Posez vos tours  |  ESPACE pour demarrer la vague 1";
                case WAVE_COMPLETE -> wm.hasMoreWaves()
                        ? "Vague terminee !  |  ESPACE pour la vague " + (wm.getCurrentWave() + 1)
                        : "Toutes les vagues sont terminees !";
                default -> "";
            };
            if (!msg.isEmpty()) g2.drawString(msg, 20, barY + 50);
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
        g2.setColor(new Color(40, 30, 20));
        g2.fillRect(20, y + 4, barW - 40, 10);
        g2.setColor(new Color(100, 180, 255, 160));
        g2.fillRect(20, y + 4, (int)((barW - 40) * 0.5), 10);
        g2.setColor(new Color(60, 100, 140));
        g2.drawRect(20, y + 4, barW - 40, 10);
    }

    private String getStateHint() {
        return switch (game.getState()) {
            case PREP          -> "[ESPACE] Demarrer  [ESC] Annuler";
            case PLAYING       -> "[ESPACE] Pause  [X] Vitesse  [ESC] Annuler";
            case PAUSED        -> "[ESPACE] Reprendre";
            case WAVE_COMPLETE -> "[ESPACE] Prochaine vague";
            case GAME_OVER     -> "[R] Recommencer";
            case VICTORY       -> "Victoire ! [R] Rejouer";
            default            -> "[ESPACE] Jouer";
        };
    }

    // ── Overlays ───────────────────────────────────────────────────────────
    private void drawOverlay(Graphics2D g2) {
        switch (game.getState()) {
            case MENU         -> drawMenuOverlay(g2);
            case LEVEL_SELECT -> drawLevelSelectOverlay(g2);
            case PREP         -> drawPrepBanner(g2);
            case PAUSED       -> drawPauseOverlay(g2);
            case GAME_OVER    -> drawGameOverOverlay(g2);
            case VICTORY      -> drawVictoryOverlay(g2);
            default           -> {}
        }
    }

    private void drawMenuOverlay(Graphics2D g2) {
        int w = Game.TILE_SIZE * Game.MAP_COLS;
        int h = Game.TILE_SIZE * Game.MAP_ROWS;

        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRect(0, 0, w, h);

        g2.setFont(new Font("Serif", Font.BOLD, 52));
        String title = "Siege of Aldoria";
        FontMetrics fm = g2.getFontMetrics();
        int tx = (w - fm.stringWidth(title)) / 2;
        g2.setColor(new Color(80, 40, 0));
        g2.drawString(title, tx + 3, h / 2 - 40 + 3);
        g2.setColor(new Color(230, 190, 80));
        g2.drawString(title, tx, h / 2 - 40);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 18));
        String sub = "A Tower Defense Game";
        g2.setColor(new Color(180, 160, 120));
        g2.drawString(sub, (w - g2.getFontMetrics().stringWidth(sub)) / 2, h / 2 + 10);

        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        String prompt = "ESPACE pour selectionner un niveau";
        g2.setColor(new Color(100, 200, 100));
        g2.drawString(prompt, (w - g2.getFontMetrics().stringWidth(prompt)) / 2, h / 2 + 60);
    }

    private void drawLevelSelectOverlay(Graphics2D g2) {
        int w = Game.TILE_SIZE * Game.MAP_COLS;
        int h = Game.TILE_SIZE * Game.MAP_ROWS;

        g2.setColor(new Color(0, 0, 0, 190));
        g2.fillRect(0, 0, w, h);

        // Title
        g2.setFont(new Font("Serif", Font.BOLD, 30));
        String title = "Choisissez un niveau";
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(new Color(230, 190, 80));
        g2.drawString(title, (w - fm.stringWidth(title)) / 2, Game.LEVEL_CARD_Y - 30);

        // Cards
        for (int i = 0; i < 3; i++) {
            int cx = Game.LEVEL_CARD_X0 + i * (Game.LEVEL_CARD_W + Game.LEVEL_CARD_GAP);
            int cy = Game.LEVEL_CARD_Y;
            drawLevelCard(g2, cx, cy, i);
        }

        // Footer hint
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        String hint = "Cliquez sur une carte pour commencer  |  [ESC] Retour";
        g2.setColor(new Color(140, 120, 90));
        g2.drawString(hint, (w - g2.getFontMetrics().stringWidth(hint)) / 2,
                Game.LEVEL_CARD_Y + Game.LEVEL_CARD_H + 30);
    }

    private void drawLevelCard(Graphics2D g2, int x, int y, int index) {
        int w = Game.LEVEL_CARD_W;
        int h = Game.LEVEL_CARD_H;
        Color accent = LEVEL_COLOR[index];

        // Card background
        g2.setColor(new Color(30, 25, 20, 230));
        g2.fillRoundRect(x, y, w, h, 14, 14);

        // Accent border
        g2.setColor(accent);
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawRoundRect(x, y, w, h, 14, 14);

        // Color band at top
        g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 180));
        g2.fillRoundRect(x, y, w, 44, 14, 14);
        g2.fillRect(x, y + 26, w, 18);

        // Level number
        g2.setFont(new Font("Serif", Font.BOLD, 22));
        g2.setColor(Color.WHITE);
        String num = "Niveau " + (index + 1);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(num, x + (w - fm.stringWidth(num)) / 2, y + 30);

        // Name
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.setColor(new Color(230, 210, 160));
        fm = g2.getFontMetrics();
        g2.drawString(LEVEL_NAMES[index], x + (w - fm.stringWidth(LEVEL_NAMES[index])) / 2, y + 68);

        // Difficulty stars
        g2.setFont(new Font("SansSerif", Font.PLAIN, 20));
        StringBuilder stars = new StringBuilder();
        for (int s = 0; s < DIFF_STARS[index]; s++) stars.append("*");
        for (int s = DIFF_STARS[index]; s < 3; s++) stars.append(".");
        g2.setColor(new Color(255, 200, 50));
        fm = g2.getFontMetrics();
        g2.drawString(stars.toString(), x + (w - fm.stringWidth(stars.toString())) / 2, y + 100);

        // Difficulty label
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        Color diffColor = switch (index) {
            case 0 -> new Color(80, 200, 80);
            case 1 -> new Color(220, 160, 40);
            default -> new Color(220, 60, 60);
        };
        g2.setColor(diffColor);
        fm = g2.getFontMetrics();
        g2.drawString(LEVEL_DIFF[index], x + (w - fm.stringWidth(LEVEL_DIFF[index])) / 2, y + 122);

        // Separator
        g2.setColor(new Color(80, 65, 45));
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(x + 16, y + 136, x + w - 16, y + 136);

        // Description
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.setColor(new Color(160, 145, 120));
        fm = g2.getFontMetrics();
        g2.drawString(LEVEL_DESC[index], x + (w - fm.stringWidth(LEVEL_DESC[index])) / 2, y + 158);

        // Start prompt
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2.setColor(new Color(100, 200, 100));
        String start = "Cliquer pour jouer";
        fm = g2.getFontMetrics();
        g2.drawString(start, x + (w - fm.stringWidth(start)) / 2, y + h - 16);
    }

    private void drawPrepBanner(Graphics2D g2) {
        int w = Game.TILE_SIZE * Game.MAP_COLS;
        int bh = 36;

        g2.setColor(new Color(20, 50, 20, 210));
        g2.fillRect(0, 0, w, bh);
        g2.setColor(new Color(60, 160, 60));
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(0, bh, w, bh);

        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        String msg = "Phase de preparation  —  Posez vos tours  |  ESPACE pour demarrer";
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(new Color(150, 230, 150));
        g2.drawString(msg, (w - fm.stringWidth(msg)) / 2, 23);
    }

    private void drawPauseOverlay(Graphics2D g2) {
        drawDimOverlay(g2, "PAUSE", new Color(200, 200, 255), "[ESPACE] Reprendre");
    }

    private void drawGameOverOverlay(Graphics2D g2) {
        drawDimOverlay(g2, "GAME OVER", new Color(220, 60, 60),
                "Score: " + game.getScore() + "   [R] Recommencer");
    }

    private void drawVictoryOverlay(Graphics2D g2) {
        drawDimOverlay(g2, "VICTOIRE !", new Color(255, 215, 0),
                "Score: " + game.getScore() + "   [R] Rejouer");
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
