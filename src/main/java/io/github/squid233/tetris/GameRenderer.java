package io.github.squid233.tetris;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fStack;
import org.overrun.glib.gl.GL;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class GameRenderer implements Disposable {
    private Shader positionColor;
    private Shader positionColorTex;
    private Shader currentShader;
    private Tessellator tessellator;
    public final Matrix4fStack projection = new Matrix4fStack(2);
    public final Matrix4fStack modelView = new Matrix4fStack(8);

    private static Shader load(String name) {
        return new Shader(FileUtil.loadString("tetris/shaders/" + name + ".vert"),
            FileUtil.loadString("tetris/shaders/" + name + ".frag"));
    }

    public void init() {
        positionColor = load("pos_color");
        positionColorTex = load("pos_color_tex");
        tessellator = new Tessellator();
    }

    public void useShader(@Nullable Shader shader) {
        currentShader = shader;
        if (shader == null) GL.useProgram(0);
        else shader.use();
    }

    public Shader positionColor() {
        return positionColor;
    }

    public Shader positionColorTex() {
        return positionColorTex;
    }

    public Shader currentShader() {
        return currentShader;
    }

    public void setupMatrices() {
        currentShader.getUniform("Projection").ifPresent(u -> u.set(projection));
        currentShader.getUniform("ModelView").ifPresent(u -> u.set(modelView));
    }

    public Tessellator tessellator() {
        return tessellator;
    }

    @Override
    public void dispose() {
        positionColor.dispose();
        positionColorTex.dispose();
        tessellator.dispose();
    }
}
