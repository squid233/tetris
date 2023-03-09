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
    MAGENTA(0xff00dc),
    YELLOW(0xffff00),
    ORANGE(0xff6a00);

    private final int colorRGB;

    CellType(int colorRGB) {
        this.colorRGB = colorRGB;
    }

    public int colorRGB() {
        return colorRGB;
    }
}
