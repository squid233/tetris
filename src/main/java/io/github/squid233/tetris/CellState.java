package io.github.squid233.tetris;

import org.joml.Vector2i;

import java.util.Arrays;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class CellState {
    public static final int DEFAULT_X = Wall.WALL_WIDTH / 2 - 2;
    public static final int DEFAULT_Y = Wall.WALL_HEIGHT - 4;
    public static final int STATE_WIDTH = 4;
    public static final int STATE_HEIGHT = 4;
    public static final int STATE_SIZE = STATE_WIDTH * STATE_HEIGHT;
    public Direction direction = Direction.UP;
    public final CellType[] cellMatrix = new CellType[STATE_SIZE];
    public final Vector2i[] posMatrix = new Vector2i[STATE_SIZE];

    public CellState() {
        for (int i = 0; i < posMatrix.length; i++) {
            posMatrix[i] = new Vector2i();
        }
        resetToNone();
    }

    private void resetToNone() {
        direction = Direction.UP;
        Arrays.fill(cellMatrix, CellType.NONE);
        for (int y = 0; y < STATE_HEIGHT; y++) {
            for (int x = 0; x < STATE_WIDTH; x++) {
                posMatrix[y * STATE_HEIGHT + x].set(x + DEFAULT_X, y + DEFAULT_Y);
            }
        }
    }

    public void reset(CellStateType type) {
        resetToNone();
        final CellType color = type.color();
        switch (type) {
            case I -> set(1, 0, color)
                .set(1, 1, color)
                .set(1, 2, color)
                .set(1, 3, color);
            case O -> set(1, 1, color)
                .set(1, 2, color)
                .set(2, 1, color)
                .set(2, 2, color);
            case L -> set(1, 1, color)
                .set(1, 2, color)
                .set(1, 3, color)
                .set(2, 1, color);
            case J -> set(1, 1, color)
                .set(2, 1, color)
                .set(2, 2, color)
                .set(2, 3, color);
            case T -> set(1, 1, color)
                .set(2, 1, color)
                .set(3, 1, color)
                .set(2, 2, color);
            case S -> set(1, 1, color)
                .set(2, 1, color)
                .set(2, 2, color)
                .set(3, 2, color);
            case Z -> set(1, 2, color)
                .set(2, 2, color)
                .set(2, 1, color)
                .set(3, 1, color);
        }
    }

    private CellState set(int x, int y, CellType type) {
        cellMatrix[y * STATE_WIDTH + x] = type;
        return this;
    }

    public void moveLeft(Wall wall) {
        for (int i = 0; i < STATE_SIZE; i++) {
            if (cellMatrix[i] == CellType.NONE) continue;
            final Vector2i pos = posMatrix[i];
            if (pos.x() <= 0 ||
                wall.getCell(pos.x() - 1, pos.y()) != CellType.NONE) return;
        }
        for (Vector2i vec : posMatrix) {
            vec.x--;
        }
    }

    public void moveRight(Wall wall) {
        for (int i = 0; i < STATE_SIZE; i++) {
            if (cellMatrix[i] == CellType.NONE) continue;
            final Vector2i pos = posMatrix[i];
            if (pos.x() >= Wall.WALL_WIDTH - 1 ||
                wall.getCell(pos.x() + 1, pos.y()) != CellType.NONE) return;
        }
        for (Vector2i vec : posMatrix) {
            vec.x++;
        }
    }

    public void moveDown(Wall wall) {
        if (isOnGround(wall)) return;
        for (Vector2i vec : posMatrix) {
            vec.y--;
        }
    }

    public void dropDown(Wall wall) {
        while (!isOnGround(wall)) moveDown(wall);
    }

    public boolean isOnGround(Wall wall) {
        for (int i = 0; i < STATE_SIZE; i++) {
            if (cellMatrix[i] == CellType.NONE) continue;
            final Vector2i pos = posMatrix[i];
            if (pos.y() < 1 ||
                wall.getCell(pos.x(), pos.y() - 1) != CellType.NONE) {
                return true;
            }
        }
        return false;
    }

    public void fixToWall(Wall wall) {
        for (int i = 0; i < STATE_SIZE; i++) {
            final CellType cell = cellMatrix[i];
            if (cell == CellType.NONE) continue;
            final Vector2i pos = posMatrix[i];
            wall.setCell(cell, pos.x(), pos.y());
        }
    }
}
