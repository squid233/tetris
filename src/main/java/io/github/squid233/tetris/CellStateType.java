package io.github.squid233.tetris;

/**
 * @author squid233
 * @since 0.1.0
 */
public enum CellStateType {
    I(CellType.CYAN),
    O(CellType.YELLOW),
    L(CellType.ORANGE),
    J(CellType.BLUE),
    T(CellType.MAGENTA),
    S(CellType.GREEN),
    Z(CellType.RED);

    private static final CellStateType[] VALUES = values();
    public static final int VALUE_COUNT = VALUES.length;

    private final CellType color;

    CellStateType(CellType color) {
        this.color = color;
    }

    public static CellStateType byId(int id) {
        return VALUES[id];
    }

    public CellType color() {
        return color;
    }
}
