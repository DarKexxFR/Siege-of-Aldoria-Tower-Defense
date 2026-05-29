package com.siegeofaldoria.map;

import com.siegeofaldoria.Game;
import com.siegeofaldoria.map.Tile.TileType;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds the grid of tiles and the enemy path.
 * Layout: 20 cols × 14 rows. Three levels with different paths.
 */
public class GameMap {

    private final Tile[][]    grid;
    private final List<int[]> path;

    // Colours
    private static final Color C_GRASS  = new Color(86, 130, 58);
    private static final Color C_GRASS2 = new Color(96, 145, 64);
    private static final Color C_PATH   = new Color(185, 155, 100);
    private static final Color C_PATH2  = new Color(170, 142, 88);
    private static final Color C_STONE  = new Color(130, 120, 115);
    private static final Color C_WATER  = new Color(70, 130, 180);

    public GameMap(int level) {
        grid = new Tile[Game.MAP_ROWS][Game.MAP_COLS];
        path = buildPath(level);
        initGrid(level);
    }

    // ── Path definitions ───────────────────────────────────────────────────
    private List<int[]> buildPath(int level) {
        return switch (level) {
            case 2  -> buildPathLevel2();
            case 3  -> buildPathLevel3();
            default -> buildPathLevel1();
        };
    }

    /** Level 1 — Foret d'Aldoria : chemin simple en S */
    private List<int[]> buildPathLevel1() {
        List<int[]> p = new ArrayList<>();
        for (int c = 0; c <= 4; c++)   p.add(new int[]{c, 2});
        for (int r = 2; r <= 6; r++)   p.add(new int[]{4, r});
        for (int c = 4; c <= 10; c++)  p.add(new int[]{c, 6});
        for (int r = 6; r >= 2; r--)   p.add(new int[]{10, r});
        for (int c = 10; c <= 15; c++) p.add(new int[]{c, 2});
        for (int r = 2; r <= 11; r++)  p.add(new int[]{15, r});
        for (int c = 15; c < Game.MAP_COLS; c++) p.add(new int[]{c, 11});
        return deduplicate(p);
    }

    /** Level 2 — Desert Brulant : entree par le bas, serpentin vertical */
    private List<int[]> buildPathLevel2() {
        List<int[]> p = new ArrayList<>();
        for (int c = 0; c <= 3; c++)   p.add(new int[]{c, 12});
        for (int r = 12; r >= 3; r--)  p.add(new int[]{3, r});
        for (int c = 3; c <= 11; c++)  p.add(new int[]{c, 3});
        for (int r = 3; r <= 10; r++)  p.add(new int[]{11, r});
        for (int c = 11; c <= 16; c++) p.add(new int[]{c, 10});
        for (int r = 10; r >= 2; r--)  p.add(new int[]{16, r});
        for (int c = 16; c < Game.MAP_COLS; c++) p.add(new int[]{c, 2});
        return deduplicate(p);
    }

    /** Level 3 — Forteresse : zigzag complexe */
    private List<int[]> buildPathLevel3() {
        List<int[]> p = new ArrayList<>();
        for (int c = 0; c <= 4; c++)   p.add(new int[]{c, 6});
        for (int r = 6; r >= 1; r--)   p.add(new int[]{4, r});
        for (int c = 4; c <= 9; c++)   p.add(new int[]{c, 1});
        for (int r = 1; r <= 11; r++)  p.add(new int[]{9, r});
        for (int c = 9; c <= 14; c++)  p.add(new int[]{c, 11});
        for (int r = 11; r >= 3; r--)  p.add(new int[]{14, r});
        for (int c = 14; c <= 18; c++) p.add(new int[]{c, 3});
        for (int r = 3; r <= 10; r++)  p.add(new int[]{18, r});
        for (int c = 18; c < Game.MAP_COLS; c++) p.add(new int[]{c, 10});
        return deduplicate(p);
    }

    private List<int[]> deduplicate(List<int[]> raw) {
        List<int[]> out = new ArrayList<>();
        for (int[] wp : raw) {
            if (out.isEmpty()) { out.add(wp); continue; }
            int[] last = out.get(out.size() - 1);
            if (last[0] != wp[0] || last[1] != wp[1]) out.add(wp);
        }
        return out;
    }

    // ── Grid init ──────────────────────────────────────────────────────────
    private void initGrid(int level) {
        for (int r = 0; r < Game.MAP_ROWS; r++)
            for (int c = 0; c < Game.MAP_COLS; c++)
                grid[r][c] = new Tile(TileType.GRASS);

        for (int[] wp : path)
            grid[wp[1]][wp[0]] = new Tile(TileType.PATH);

        int[][] stones;
        int[][] waters;

        switch (level) {
            case 2 -> {
                stones = new int[][]{{1,7},{2,7},{5,5},{6,8},{8,6},{13,5},{14,8},{18,5},{19,7}};
                waters = new int[][]{{0,2},{0,3},{1,2},{1,3},{17,12},{18,12},{19,12},{5,11},{6,12}};
            }
            case 3 -> {
                stones = new int[][]{{2,3},{2,9},{6,4},{7,9},{11,4},{12,8},{15,5},{16,8},{1,12}};
                waters = new int[][]{{0,3},{0,4},{1,4},{7,6},{8,6},{11,7},{16,6},{17,11},{18,12}};
            }
            default -> {
                stones = new int[][]{{1,5},{2,5},{3,8},{7,3},{8,10},{12,4},{13,9},{17,5},{18,8}};
                waters = new int[][]{{0,8},{0,9},{1,8},{1,9},{18,1},{19,1},{18,2},{6,11},{7,12}};
            }
        }

        for (int[] s : stones)
            if (s[1] < Game.MAP_ROWS && s[0] < Game.MAP_COLS
                    && grid[s[1]][s[0]].getType() == TileType.GRASS)
                grid[s[1]][s[0]] = new Tile(TileType.STONE);

        for (int[] w : waters)
            if (w[1] < Game.MAP_ROWS && w[0] < Game.MAP_COLS
                    && grid[w[1]][w[0]].getType() == TileType.GRASS)
                grid[w[1]][w[0]] = new Tile(TileType.WATER);
    }

    // ── Draw ───────────────────────────────────────────────────────────────
    public void draw(Graphics2D g2) {
        int ts = Game.TILE_SIZE;
        for (int r = 0; r < Game.MAP_ROWS; r++) {
            for (int c = 0; c < Game.MAP_COLS; c++) {
                int px = c * ts;
                int py = r * ts;
                Tile tile = grid[r][c];

                switch (tile.getType()) {
                    case GRASS -> {
                        Color gc = ((r + c) % 2 == 0) ? C_GRASS : C_GRASS2;
                        g2.setColor(gc);
                        g2.fillRect(px, py, ts, ts);
                    }
                    case PATH -> {
                        Color pc = ((r + c) % 2 == 0) ? C_PATH : C_PATH2;
                        g2.setColor(pc);
                        g2.fillRect(px, py, ts, ts);
                        g2.setColor(new Color(0, 0, 0, 20));
                        g2.drawRect(px, py, ts - 1, ts - 1);
                    }
                    case STONE -> {
                        g2.setColor(((r + c) % 2 == 0) ? C_GRASS : C_GRASS2);
                        g2.fillRect(px, py, ts, ts);
                        g2.setColor(C_STONE);
                        g2.fillRoundRect(px + 6, py + 6, ts - 12, ts - 12, 8, 8);
                        g2.setColor(C_STONE.darker());
                        g2.drawRoundRect(px + 6, py + 6, ts - 12, ts - 12, 8, 8);
                    }
                    case WATER -> {
                        g2.setColor(C_WATER);
                        g2.fillRect(px, py, ts, ts);
                        g2.setColor(new Color(100, 160, 210));
                        g2.drawLine(px + 4, py + ts / 3, px + ts - 4, py + ts / 3);
                        g2.drawLine(px + 4, py + 2 * ts / 3, px + ts - 4, py + 2 * ts / 3);
                    }
                }

                if (tile.getType() == TileType.GRASS) {
                    g2.setColor(new Color(0, 0, 0, 18));
                    g2.drawRect(px, py, ts - 1, ts - 1);
                }
            }
        }
    }

    // ── Queries ────────────────────────────────────────────────────────────
    public boolean isBuildable(int col, int row) {
        if (col < 0 || col >= Game.MAP_COLS || row < 0 || row >= Game.MAP_ROWS) return false;
        return grid[row][col].isBuildable();
    }

    public void setOccupied(int col, int row, boolean occupied) {
        if (col < 0 || col >= Game.MAP_COLS || row < 0 || row >= Game.MAP_ROWS) return;
        grid[row][col].setOccupied(occupied);
    }

    public TileType getTileType(int col, int row) {
        if (col < 0 || col >= Game.MAP_COLS || row < 0 || row >= Game.MAP_ROWS)
            return TileType.GRASS;
        return grid[row][col].getType();
    }

    public List<int[]> getPath() { return path; }
}
