package io.github.squid233.tetris;

import org.joml.Vector2i;
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
import java.util.Random;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class Tetris {
    private static final Tetris INSTANCE = new Tetris();
    private MemorySegment window;
    private Timer timer;
    private int width, height;
    private final Wall wall = new Wall();
    private final CellState holdState = new CellState();
    private GameRenderer gameRenderer;
    private Texture cellTexture;
    private int fallDownTicker = 0;
    private final Random random = new Random();

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
            GLFW.setKeyCallback(window, (window1, key, scancode, action, mods) -> onKey(key, action));
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

        holdState.reset(getRandomType());

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

    private CellStateType getRandomType() {
        return CellStateType.byId(random.nextInt(CellStateType.VALUE_COUNT));
    }

    public void tick() {
        fallDownTicker++;
        if (fallDownTicker > 8) {
            holdState.moveDown(wall);
            fallDownTicker = 0;
            return;
        }
        if (GLFW.getKey(window, GLFW.KEY_LEFT) == GLFW.PRESS) {
            holdState.moveLeft(wall);
        }
        if (GLFW.getKey(window, GLFW.KEY_RIGHT) == GLFW.PRESS) {
            holdState.moveRight(wall);
        }
        if (GLFW.getKey(window, GLFW.KEY_DOWN) == GLFW.PRESS) {
            holdState.moveDown(wall);
        }
        if (holdState.isOnGround(wall)) {
            holdState.fixToWall(wall);
            holdState.reset(getRandomType());
        }
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        gameRenderer.projection.setOrtho2D(0, width, 0, height);
    }

    public void onKey(int key, int action) {
        if (action == GLFW.PRESS) {
            if (key == GLFW.KEY_SPACE) {
                holdState.dropDown(wall);
            }
        }
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
        gameRenderer.modelView.translation((width - Wall.WALL_WIDTH * Wall.CELL_SIZE) * 0.5f,
            (height - Wall.WALL_VISIBLE_HEIGHT * Wall.CELL_SIZE) * 0.5f,
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
        for (int y = 0; y < Wall.WALL_VISIBLE_HEIGHT; y++) {
            for (int x = 0; x < Wall.WALL_WIDTH; x++) {
                renderCell(tessellator, x, y, wall.getCell(x, y));
            }
        }
        for (int i = 0; i < CellState.STATE_SIZE; i++) {
            final Vector2i pos = holdState.posMatrix[i];
            renderCell(tessellator, pos.x(), pos.y(), holdState.cellMatrix[i]);
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
        final float x1 = Wall.WALL_WIDTH * Wall.CELL_SIZE + 1f;
        final float y1 = Wall.WALL_VISIBLE_HEIGHT * Wall.CELL_SIZE + 1f;
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

    public Wall wall() {
        return wall;
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
