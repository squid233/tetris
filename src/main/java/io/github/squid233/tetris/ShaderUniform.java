package io.github.squid233.tetris;

import org.joml.Matrix4fc;
import org.overrun.glib.gl.GL;
import org.overrun.glib.joml.Matrixn;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentScope;
import java.lang.foreign.ValueLayout;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class ShaderUniform {
    public static final int TYPE_INT = 0;
    public static final int TYPE_FLOAT = 4;
    public static final int TYPE_MAT4 = 10;

    private final int location;
    private final int type;
    private final MemorySegment buffer;
    private boolean dirty;

    public ShaderUniform(int location, int type) {
        this.location = location;
        this.type = type;
        this.buffer = MemorySegment.allocateNative(getSize(type), SegmentScope.auto());
    }

    private static long getSize(int type) {
        return switch (type) {
            case TYPE_INT -> ValueLayout.JAVA_INT.byteSize();
            case TYPE_FLOAT -> ValueLayout.JAVA_FLOAT.byteSize();
            case TYPE_MAT4 -> Matrixn.MAT4F.byteSize();
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

    private void markDirty() {
        dirty = true;
    }

    public void set(int value) {
        buffer.set(ValueLayout.JAVA_INT, 0, value);
        markDirty();
    }

    public void set(float value) {
        buffer.set(ValueLayout.JAVA_FLOAT, 0, value);
        markDirty();
    }

    public void set(Matrix4fc value) {
        Matrixn.put(value, buffer);
        markDirty();
    }

    public void upload() {
        if (!dirty) return;
        switch (type) {
            case TYPE_INT -> GL.uniform1iv(location, 1, buffer);
            case TYPE_FLOAT -> GL.uniform1fv(location, 1, buffer);
            case TYPE_MAT4 -> GL.uniformMatrix4fv(location, 1, false, buffer);
        }
    }
}
