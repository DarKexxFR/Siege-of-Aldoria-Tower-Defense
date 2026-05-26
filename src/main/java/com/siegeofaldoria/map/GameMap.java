package com.siegeofaldoria.map;

import com.siegeofaldoria.Game;
import com.siegeofaldoria.map.Tile.TileType;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds the grid of tiles and the enemy path.
 * Layout: 20 cols × 14 rows. Path winds across the map.
 */
public class GameMap {

    private final Tile[][]  grid;
    private final List<int[]> path; // ordered [col, row] waypoints

    // Colours
    private static final Color C_GRASS  = new Color(86, 130, 58);
    private static final Color C_GRASS2 = new Color(96, 145, 64);
    private static final Color C_PATH   = new Color(185, 155, 100);
    private static final Color C_PATH2  = new Color(170, 142, 88);
    private static final Color C_STONE  = new Color(130, 120, 115);
    private static final Color C_WATER  = new Color(70, 130, 180);

    public GameMap() {
        grid = new Tile[Game.MAP_ROWS][Game.MAP_COLS];
        path = buildPath();
        initGrid();
    }

    // ── Path definition ────────────────────────────────────────────────────
    /**
     * Defines the winding path as a sequence of [col, row] waypoints.
     * Enemies enter from the left and exit to the right.
     */
    private List<int[]> buildPath() {
        List<int[]> p = new ArrayList<>();
        // Entry from left edge, row 2
        for (int c = 0; c <= 4; c++)  p.add(new int[]{c, 2});
        // Turn down
        for (int r = 2; r <= 6; r++)  p.add(new int[]{4, r});
        // Turn right
        for (int c = 4; c <= 10; c++) p.add(new int[]{c, 6});
        // Turn up
        for (int r = 6; r >= 2; r--)  p.add(new int[]{10, r});
        // Turn right
        for (int c = 10; c <= 15; c++) p.add(new int[]{c, 2});
        // Turn down
        for (int r = 2; r <= 11; r++) p.add(new int[]{15, r});
        // Turn right to exit
        for (int c = 15; c < Game.MAP_COLS; c++) p.add(new int[]{c, 11});
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
    private void initGrid() {
        // Fill all with grass
        for (int r = 0; r < Game.MAP_ROWS; r++)
            for (int c = 0; c < Game.MAP_COLS; c++)
                grid[r][c] = new Tile(TileType.GRASS);

        // Mark path tiles
        for (int[] wp : path)
            grid[wp[1]][wp[0]] = new Tile(TileType.PATH);

        // Add some stone decorations
        int[][] stones = {
            {1,5},{2,5},{3,8},{7,3},{8,10},{12,4},{13,9},{17,5},{18,8}
        };
        for (int[] s : stones)
            if (s[1] < Game.MAP_ROWS && s[0] < Game.MAP_COLS
                    && grid[s[1]][s[0]].getType() == TileType.GRASS)
                grid[s[1]][s[0]] = new Tile(TileType.STONE);

        // Add water patches
        int[][] waters = {
            {0,8},{0,9},{1,8},{1,9},{18,1},{19,1},{18,2},{6,11},{7,12}
        };
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
                        // Subtle edge lines
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

                // Grid lines on grass
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
