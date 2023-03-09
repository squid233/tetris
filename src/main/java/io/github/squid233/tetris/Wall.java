package io.github.squid233.tetris;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class Wall {
    public static final int WALL_WIDTH = 15;
    public static final int WALL_VISIBLE_HEIGHT = 25;
    public static final int WALL_HEIGHT = WALL_VISIBLE_HEIGHT + 4;
    public static final int CELL_SIZE = 16;
    private final CellType[][] wall = new CellType[WALL_HEIGHT][WALL_WIDTH];

    public Wall() {
        for (int y = 0; y < WALL_HEIGHT; y++) {
            for (int x = 0; x < WALL_WIDTH; x++) {
                setCell(CellType.NONE, x, y);
            }
        }
    }

    public CellType getCell(int x, int y) {
        return wall[y][x];
    }

    public void setCell(CellType cell, int x, int y) {
        wall[y][x] = cell;
    }
}
