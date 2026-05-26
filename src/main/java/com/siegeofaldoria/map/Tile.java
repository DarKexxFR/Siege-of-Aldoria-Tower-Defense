package com.siegeofaldoria.map;

/**
 * A single cell on the game grid.
 */
public class Tile {

    public enum TileType {
        GRASS,   // buildable
        PATH,    // enemy path – not buildable
        STONE,   // decorative – not buildable
        WATER    // decorative – not buildable
    }

    private TileType type;
    private boolean  occupied; // a tower is placed here

    public Tile(TileType type) {
        this.type     = type;
        this.occupied = false;
    }

    public boolean isBuildable() {
        return type == TileType.GRASS && !occupied;
    }

    public TileType getType()               { return type; }
    public boolean  isOccupied()            { return occupied; }
    public void     setOccupied(boolean o)  { occupied = o; }
}
