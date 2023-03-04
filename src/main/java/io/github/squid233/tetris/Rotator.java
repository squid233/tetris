package io.github.squid233.tetris;

import org.overrun.glib.util.value.Value2;

/**
 * @author squid233
 * @since 0.1.0
 */
@FunctionalInterface
public interface Rotator {
    Value2<int[], int[]> apply(Direction direction);
}
