package io.github.squid233.tetris;

/**
 * @author squid233
 * @since 0.1.0
 */
public enum CellType {
    NONE(0),
    RED(0xff0000),
    GREEN(0x00ff00),
    BLUE(0x0094ff),
    CYAN(0x00ffff),
    PURPLE(0x8000ff),
    YELLOW(0xffff00),
    WHITE(0xffffff);

    private final int colorRGB;

    CellType(int colorRGB) {
        this.colorRGB = colorRGB;
    }

    public int colorRGB() {
        return colorRGB;
    }
}
