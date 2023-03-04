package io.github.squid233.tetris;

import org.overrun.glib.RuntimeHelper;
import org.overrun.glib.gl.GL;
import org.overrun.glib.stb.STBImage;
import org.overrun.glib.util.MemoryStack;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class Texture implements Disposable {
    private final int id;

    public Texture(String filename) {
        id = GL.genTexture();
        GL.bindTexture(GL.TEXTURE_2D, id);
        GL.texParameteri(GL.TEXTURE_2D, GL.TEXTURE_MAG_FILTER, GL.NEAREST);
        GL.texParameteri(GL.TEXTURE_2D, GL.TEXTURE_MIN_FILTER, GL.NEAREST);
        GL.texParameteri(GL.TEXTURE_2D, GL.TEXTURE_BASE_LEVEL, 0);
        GL.texParameteri(GL.TEXTURE_2D, GL.TEXTURE_MAX_LEVEL, 0);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            final MemorySegment pw = stack.calloc(ValueLayout.JAVA_INT);
            final MemorySegment ph = stack.calloc(ValueLayout.JAVA_INT);
            final MemorySegment pc = stack.calloc(ValueLayout.JAVA_INT);
            final MemorySegment data;
            try (Arena arena = Arena.openConfined()) {
                data = STBImage.loadFromMemory(IOUtil.ioResourceToSegment(arena.scope(), filename, 8192), pw, ph, pc, STBImage.RGB_ALPHA);
                if (data.address() == RuntimeHelper.NULL) {
                    throw new IllegalStateException("Failed to load image " + filename + ": " + STBImage.failureReason());
                }
            } catch (Exception e) {
                throw new IllegalStateException("Failed to load image " + filename, e);
            }
            GL.texImage2D(GL.TEXTURE_2D,
                0,
                GL.RGBA,
                pw.get(ValueLayout.JAVA_INT, 0),
                ph.get(ValueLayout.JAVA_INT, 0),
                0,
                GL.RGBA,
                GL.UNSIGNED_BYTE,
                data);
        }
        GL.bindTexture(GL.TEXTURE_2D, 0);
    }

    public int id() {
        return id;
    }

    public void bind() {
        GL.bindTexture(GL.TEXTURE_2D, id);
    }

    @Override
    public void dispose() {
        GL.deleteTexture(id);
    }
}
