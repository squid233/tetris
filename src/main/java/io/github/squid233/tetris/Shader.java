package io.github.squid233.tetris;

import org.overrun.glib.gl.GL;

import java.lang.foreign.Arena;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class Shader implements Disposable {
    private final int id;
    private final Map<String, ShaderUniform> uniformMap = new HashMap<>();

    public Shader(String vertexShader, String fragmentShader) {
        id = GL.createProgram();
        try (Arena arena = Arena.openConfined()) {
            final int vsh = compileShader(arena, "vertex", GL.VERTEX_SHADER, vertexShader);
            final int fsh = compileShader(arena, "fragment", GL.FRAGMENT_SHADER, fragmentShader);
            GL.attachShader(id, vsh);
            GL.attachShader(id, fsh);
            GL.bindAttribLocation(arena, id, 0, "Position");
            GL.bindAttribLocation(arena, id, 1, "Color");
            GL.bindAttribLocation(arena, id, 2, "UV");
            GL.linkProgram(id);
            if (GL.getProgrami(id, GL.LINK_STATUS) == GL.FALSE)
                throw new IllegalStateException("Failed to link the program: " + GL.getProgramInfoLog(arena, id));
            GL.detachShader(id, vsh);
            GL.detachShader(id, fsh);
            GL.deleteShader(vsh);
            GL.deleteShader(fsh);

            addUniform(arena, "Projection", ShaderUniform.TYPE_MAT4);
            addUniform(arena, "ModelView", ShaderUniform.TYPE_MAT4);
            addUniform(arena, "Sampler", ShaderUniform.TYPE_INT).ifPresent(u -> u.set(0));
        }
    }

    private static int compileShader(Arena arena, String name, int type, String src) {
        int shader = GL.createShader(type);
        GL.shaderSource(arena, shader, src);
        GL.compileShader(shader);
        if (GL.getShaderi(shader, GL.COMPILE_STATUS) == GL.FALSE)
            throw new IllegalStateException("Failed to compile the " + name + " shader: " + GL.getShaderInfoLog(arena, shader));
        return shader;
    }

    private Optional<ShaderUniform> addUniform(Arena arena, String name, int type) {
        final int loc = GL.getUniformLocation(arena, id, name);
        if (loc != -1) {
            final ShaderUniform uniform = new ShaderUniform(loc, type);
            uniformMap.put(name, uniform);
            return Optional.of(uniform);
        }
        return Optional.empty();
    }

    public Optional<ShaderUniform> getUniform(String name) {
        return Optional.ofNullable(uniformMap.get(name));
    }

    public void use() {
        GL.useProgram(id);
    }

    public void uploadUniforms() {
        uniformMap.values().forEach(ShaderUniform::upload);
    }

    public int id() {
        return id;
    }

    @Override
    public void dispose() {
        GL.deleteProgram(id);
    }
}
