package io.github.squid233.tetris;

import org.overrun.glib.gl.GL;
import org.overrun.glib.util.GrowableBuffer;

import java.lang.foreign.MemorySegment;

import static java.lang.foreign.ValueLayout.*;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class Tessellator implements Disposable {
    private static final int STRIDE = (int) (JAVA_FLOAT.byteSize() * (2 + 2) + JAVA_BYTE.byteSize() * 3);
    private final GrowableBuffer buffer;
    private final GrowableBuffer indexBuffer;
    private int colorRGB = 0xffffff;
    private float x, y, u, v;
    private int vertexCount;
    private int vao, vbo, ebo;

    public Tessellator() {
        buffer = new GrowableBuffer(256 * 1024);
        indexBuffer = new GrowableBuffer(256 * 1024);
    }

    private void clear() {
        buffer.begin();
        indexBuffer.begin();
        vertexCount = 0;
    }

    public void begin() {
        clear();
    }

    public void end() {
        flush();
    }

    public void end(int mode) {
        flush(mode);
    }

    public void flush() {
        flush(GL.TRIANGLES);
    }

    public void flush(int mode) {
        if (vertexCount <= 0) return;

        final boolean bufferGrew = buffer.end();
        final boolean indexBufferGrew = indexBuffer.end();
        final boolean noVbo = vbo == 0;
        final boolean noEbo = ebo == 0;

        if (vao == 0) vao = GL.genVertexArray();
        if (noVbo) vbo = GL.genBuffer();
        if (noEbo) ebo = GL.genBuffer();

        GL.bindVertexArray(vao);

        GL.bindBuffer(GL.ARRAY_BUFFER, vbo);
        if (noVbo || bufferGrew) {
            GL.bufferData(GL.ARRAY_BUFFER, buffer.capacity(), buffer.address(), GL.STREAM_DRAW);
        } else {
            GL.bufferSubData(GL.ARRAY_BUFFER, 0, buffer);
        }
        if (noVbo) {
            GL.enableVertexAttribArray(0);
            GL.enableVertexAttribArray(1);
            GL.enableVertexAttribArray(2);
            GL.vertexAttribPointer(0, 2, GL.FLOAT, false, STRIDE, MemorySegment.NULL);
            GL.vertexAttribPointer(1, 3, GL.UNSIGNED_BYTE, true, STRIDE, MemorySegment.ofAddress(JAVA_FLOAT.byteSize() * 4));
            GL.vertexAttribPointer(2, 2, GL.FLOAT, false, STRIDE, MemorySegment.ofAddress(JAVA_FLOAT.byteSize() * 2));
        }
        GL.bindBuffer(GL.ARRAY_BUFFER, 0);

        if (noEbo) GL.bindBuffer(GL.ELEMENT_ARRAY_BUFFER, ebo);
        if (noEbo || indexBufferGrew) {
            GL.bufferData(GL.ELEMENT_ARRAY_BUFFER, indexBuffer.capacity(), indexBuffer.address(), GL.STREAM_DRAW);
        } else {
            GL.bufferSubData(GL.ELEMENT_ARRAY_BUFFER, 0, indexBuffer);
        }

        GL.drawElements(mode, (int) indexBuffer.count(), GL.UNSIGNED_INT, MemorySegment.NULL);
        GL.bindVertexArray(0);

        clear();
    }

    public Tessellator position(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Tessellator color(int colorRGB) {
        this.colorRGB = colorRGB;
        return this;
    }

    public Tessellator texCoord(float u, float v) {
        this.u = u;
        this.v = v;
        return this;
    }

    public void indices(int... indices) {
        for (int index : indices) {
            indexBuffer.put(JAVA_INT, index + vertexCount);
        }
    }

    public void emit() {
        buffer.putAll(JAVA_FLOAT_UNALIGNED, x, y, u, v)
            .putAll(JAVA_BYTE, (byte) (colorRGB >>> 16), (byte) (colorRGB >>> 8), (byte) colorRGB);
        vertexCount++;
    }

    @Override
    public void dispose() {
        buffer.close();
        indexBuffer.close();
        GL.deleteVertexArray(vao);
        GL.deleteBuffer(vbo);
        GL.deleteBuffer(ebo);
    }
}
