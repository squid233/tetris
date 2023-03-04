package io.github.squid233.tetris;

import org.overrun.glib.RuntimeHelper;
import org.overrun.glib.gl.GL;
import org.overrun.glib.gl.GLLoader;
import org.overrun.glib.glfw.Callbacks;
import org.overrun.glib.glfw.GLFW;
import org.overrun.glib.glfw.GLFWVidMode;
import org.overrun.glib.util.MemoryStack;
import org.overrun.glib.util.value.Value2;
import org.overrun.timer.Timer;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentScope;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class Tetris {
    public static final int WALL_WIDTH = 15;
    public static final int WALL_VISIBLE_HEIGHT = 25;
    public static final int WALL_HEIGHT = WALL_VISIBLE_HEIGHT + 3;
    public static final int CELL_SIZE = 16;
    private static final Tetris INSTANCE = new Tetris();
    private MemorySegment window;
    private Timer timer;
    private int width, height;
    private final CellType[][] wall = new CellType[WALL_HEIGHT][WALL_WIDTH];
    private GameRenderer gameRenderer;
    private Texture cellTexture;

    private void init() {
        if (!GLFW.init()) {
            throw new IllegalStateException("Failed to initialize GLFW");
        }
        GLFW.windowHint(GLFW.VISIBLE, false);
        GLFW.windowHint(GLFW.RESIZABLE, false);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            window = GLFW.createWindow(stack, 640, 640, "Tetris", MemorySegment.NULL, MemorySegment.NULL);
            if (window.address() == RuntimeHelper.NULL) {
                throw new IllegalStateException("Failed to create the window");
            }
            GLFW.setFramebufferSizeCallback(window, (window1, width, height) -> resize(width, height));
            final GLFWVidMode.Value videoMode = GLFW.getVideoMode(SegmentScope.auto(), GLFW.getPrimaryMonitor());
            if (videoMode != null) {
                final Value2.OfInt size = GLFW.getWindowSize(window);
                GLFW.setWindowPos(window,
                    (videoMode.width() - size.x()) / 2,
                    (videoMode.height() - size.y()) / 2);
            }
        }
        initGL();
        GLFW.showWindow(window);
    }

    private void initGL() {
        GLFW.makeContextCurrent(window);
        GLLoader.loadConfined(GLFW::getProcAddress);

        GL.clearColor(0, 0, 0, 1);
        gameRenderer = new GameRenderer();
        gameRenderer.init();

        cellTexture = new Texture("tetris/textures/cell.png");

        for (int y = 0; y < WALL_HEIGHT; y++) {
            for (int x = 0; x < WALL_WIDTH; x++) {
                wall[y][x] = CellType.NONE;
            }
        }
        wall[0][0] = CellType.RED;
        wall[1][1] = CellType.GREEN;
        wall[2][2] = CellType.BLUE;
        wall[3][3] = CellType.CYAN;
        wall[4][4] = CellType.PURPLE;
        wall[5][5] = CellType.YELLOW;
        wall[6][6] = CellType.WHITE;

        final Value2.OfInt framebufferSize = GLFW.getFramebufferSize(window);
        resize(framebufferSize.x(), framebufferSize.y());
    }

    private void dispose() {
        gameRenderer.dispose();
        cellTexture.dispose();

        Callbacks.free(window);
        GLFW.destroyWindow(window);
        GLFW.terminate();
    }

    public void tick() {
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        gameRenderer.projection.setOrtho2D(0, width, 0, height);
    }

    private void renderCell(Tessellator t, int x, int y, CellType type) {
        if (type == CellType.NONE) return;
        final float x0 = x * 16;
        final float y0 = y * 16;
        final float x1 = (x + 1) * 16;
        final float y1 = (y + 1) * 16;
        final int color = type.colorRGB();
        t.indices(0, 1, 2, 2, 3, 0);
        t.color(color);
        t.position(x0, y1).texCoord(0f, 0f).emit();
        t.position(x0, y0).texCoord(0f, 1f).emit();
        t.position(x1, y0).texCoord(1f, 1f).emit();
        t.position(x1, y1).texCoord(1f, 0f).emit();
    }

    private void moveSceneCamera() {
        gameRenderer.modelView.translation((width - WALL_WIDTH * CELL_SIZE) * 0.5f,
            (height - WALL_VISIBLE_HEIGHT * CELL_SIZE) * 0.5f,
            0);
    }

    private void renderWall() {
        gameRenderer.useShader(gameRenderer.positionColorTex());
        gameRenderer.modelView.pushMatrix();
        moveSceneCamera();
        gameRenderer.setupMatrices();
        gameRenderer.modelView.popMatrix();
        gameRenderer.currentShader().uploadUniforms();
        cellTexture.bind();
        final Tessellator tessellator = gameRenderer.tessellator();
        tessellator.begin();
        for (int y = 0; y < WALL_VISIBLE_HEIGHT; y++) {
            for (int x = 0; x < WALL_WIDTH; x++) {
                renderCell(tessellator, x, y, wall[y][x]);
            }
        }
        tessellator.end();
        GL.bindTexture(GL.TEXTURE_2D, 0);
        gameRenderer.useShader(null);
    }

    private void renderBorder() {
        gameRenderer.useShader(gameRenderer.positionColor());
        gameRenderer.modelView.pushMatrix();
        moveSceneCamera();
        gameRenderer.setupMatrices();
        gameRenderer.modelView.popMatrix();
        gameRenderer.currentShader().uploadUniforms();
        final Tessellator tessellator = gameRenderer.tessellator();
        tessellator.begin();
        final float x0 = -1f;
        final float y0 = -1f;
        final float x1 = WALL_WIDTH * CELL_SIZE + 1f;
        final float y1 = WALL_VISIBLE_HEIGHT * CELL_SIZE + 1f;
        tessellator.indices(0, 1, 2, 3);
        tessellator.color(0xffffff);
        tessellator.position(x0, y1).emit();
        tessellator.position(x0, y0).emit();
        tessellator.position(x1, y0).emit();
        tessellator.position(x1, y1).emit();
        tessellator.end(GL.LINE_LOOP);
        gameRenderer.useShader(null);
    }

    public void render(double partialTick) {
        GL.clear(GL.COLOR_BUFFER_BIT | GL.DEPTH_BUFFER_BIT);

        renderWall();
        renderBorder();

        GLFW.swapBuffers(window);
    }

    public void run() {
        init();
        timer = Timer.ofGetter(20, GLFW::getTime);
        while (!GLFW.windowShouldClose(window)) {
            timer.advanceTime();
            timer.performTicks(this::tick);
            GLFW.pollEvents();
            render(timer.partialTick());
            timer.calcFPS();
        }
        dispose();
    }

    public GameRenderer gameRenderer() {
        return gameRenderer;
    }

    public static Tetris getInstance() {
        return INSTANCE;
    }

    public static void main(String[] args) {
        getInstance().run();
    }
}
