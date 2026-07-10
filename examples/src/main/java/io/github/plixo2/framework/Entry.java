package io.github.plixo2.framework;


import io.github.plixo2.Example;
import io.github.plixo2.abstraction.Camera;
import io.github.plixo2.abstraction.Capabilities;
import io.github.plixo2.abstraction.Color;
import io.github.plixo2.abstraction.GLResourceManagement;
import io.github.plixo2.box3d.*;
import io.github.plixo2.box3d.region.Region;
import lombok.val;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GLDebugMessageCallback;
import org.lwjgl.system.NativeResource;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static io.github.plixo2.box3d.internal.Internal.U64_MAX;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;
import static org.lwjgl.opengl.GL14.glBlendFuncSeparate;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Entry implements AutoCloseable {

    private int width = 1280;
    private int height = 720;

    private int windowWidth = this.width;
    private int windowHeight = this.height;

    private float mouseX, mouseY;
    private float deltaX, deltaY;

    private long lastFrameTime;
    private long lastFPSUpdate;
    private int lastUpdatedFPS = 0;
    private int fpsCounter = 0;


    private final long window;
    private final List<NativeResource> closeables = new ArrayList<>();

    private final Camera.FreeCam camera;

    private final SceneDrawing sceneRendering;

    private final MeshRenderer meshRenderer;
    private final TextRenderer.UI textRenderer2D;
    private final TextRenderer.World textRenderer3D;
    private final LineRenderer lineRenderer;

    private final Example example;
    private DebugDraw debugDraw;


    public Entry(Example example) {
        System.setProperty("joml.format", "false");

        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 5);


        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        var onMac = System.getProperty("os.name").toLowerCase().contains("mac");
        var onLinux = System.getProperty("os.name").toLowerCase().contains("linux");
        if (onMac) {
            glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        }


        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_SAMPLES, 4);
        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);

        long windowID;
        try {
            windowID = glfwCreateWindow(this.width, this.height, "Examples", NULL, NULL);
        } catch(Exception e) {
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
            windowID = glfwCreateWindow(this.width, this.height, "Examples", NULL, NULL);
        }

        this.window = windowID;
        if (this.window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwSetKeyCallback(this.window, addCloseable(GLFWKeyCallback.create(this::keyCallback)));
        glfwSetCursorPosCallback(this.window, addCloseable(GLFWCursorPosCallback.create(this::mouseMoveCallback)));
        glfwSetMouseButtonCallback(this.window, addCloseable(GLFWMouseButtonCallback.create(this::mouseButtonCallback)));
        glfwSetFramebufferSizeCallback(this.window, addCloseable(GLFWFramebufferSizeCallback.create(this::resizeCallback)));
        glfwSetWindowSizeCallback(this.window, addCloseable(GLFWWindowSizeCallback.create(this::resizeCallbackWindow)));


        centerWindow(this.window);

        glfwMakeContextCurrent(this.window);
        glfwShowWindow(this.window);

        setRealWindowSize(this.window);

        GL.createCapabilities();
        Capabilities.get(); // static init

        var vendor = GL11.glGetString(GL11.GL_VENDOR);
        var renderer = GL11.glGetString(GL11.GL_RENDERER);
        var version = GL11.glGetString(GL11.GL_VERSION);

        System.out.println("Graphics Card Vendor: " + vendor);
        System.out.println("Graphics Card Renderer: " + renderer);
        System.out.println("OpenGL Version: " + version);

        Capabilities.get().print();

        glfwSwapInterval(1);

        GL43.glEnable(GL43.GL_DEBUG_OUTPUT);
        GL43.glEnable(GL43.GL_DEBUG_OUTPUT_SYNCHRONOUS);
        var callback = addCloseable(new DebugMessageOutput(System.err));
        GL43.glDebugMessageCallback(callback, 0);

        glEnable(GL_MULTISAMPLE);
        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);

        glEnable(GL_BLEND);
        glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

        this.meshRenderer = new MeshRenderer();
        this.lineRenderer = new LineRenderer();

        var atlas = new TextAtlas();
        this.textRenderer2D = new TextRenderer.UI(atlas);
        this.textRenderer3D = new TextRenderer.World(atlas);
        this.textRenderer3D.scale(0.01f);
        this.sceneRendering = new SceneDrawing(this.lineRenderer, this.textRenderer3D);

        this.camera = new Camera.FreeCam();


        this.example = example;

        this.lastFrameTime = System.nanoTime();

        initExample();

        var cameraPosition = this.example.initialCameraPosition;
        this.camera.x = cameraPosition.x;
        this.camera.y = cameraPosition.y;
        this.camera.z = cameraPosition.z;

        // look at 0, 0, 0
        var dir = new Vector3f(-this.camera.x, -this.camera.y, -this.camera.z).normalize();
        this.camera.yaw = (float) Math.toDegrees(Math.atan2(dir.x, dir.z));
        this.camera.pitch = (float) Math.toDegrees(Math.asin(dir.y));

    }

    private void restart() {
        this.example.region.close();
        this.example.region = Region.ofConfined();
        this.meshRenderer.free();
        initExample();
    }

    private void initExample() {
        this.example.customColors.clear();
        var meshFactory = new MeshFactory(this.meshRenderer, this.example.customColors);


        this.example.init(meshFactory);
        if (this.example.worldID == null) {
            throw new IllegalStateException("Example did not set Example.worldID in init() method");
        }


        this.debugDraw = new DebugDraw(meshFactory, this.sceneRendering);
        this.example.drawConfig.reset();

        var size = Float.MAX_VALUE / 4;
        this.debugDraw.drawingBounds.lowerBound.set(-size);
        this.debugDraw.drawingBounds.upperBound.set(size);
    }


    public void loop() {
        while (!glfwWindowShouldClose(this.window)) {
            glfwPollEvents();
            frame();
            glfwSwapBuffers(this.window);
        }
    }

    private void frame() {
        GLResourceManagement.cleanup();

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        var now = System.nanoTime();
        var dt = (float) ((now - this.lastFrameTime) / 1e9d);
        this.lastFrameTime = now;

        if (now - this.lastFPSUpdate > 1e9d) {
            this.lastFPSUpdate = now;
            this.lastUpdatedFPS = this.fpsCounter;
            this.fpsCounter = 0;
        }
        this.fpsCounter++;



        this.example.update(dt);

        var afterUpdate = System.nanoTime();
        var updateTime = (float) ((afterUpdate - now) / 1e9d);


        this.camera.move(this.window, this.deltaX, this.deltaY, dt * 5f);

        this.example.drawConfig.update(this.debugDraw);

        this.example.drawText2D(this.textRenderer2D);
        this.example.drawText3D(this.textRenderer3D);
        this.example.drawConfig.render(this.textRenderer2D, this.height, this.width);


        this.example.b3.worldDraw(this.example.worldID, this.debugDraw, U64_MAX);

        this.lineRenderer.addLine(0, 0, 0, 1, 0, 0, Color.RED.argb());
        this.lineRenderer.addLine(0, 0, 0, 0, 1, 0, Color.GREEN.argb());
        this.lineRenderer.addLine(0, 0, 0, 0, 0, 1, Color.BLUE.argb());

        var projection = getViewProjection(this.width, this.height);
        this.meshRenderer.draw(projection, getCameraPosition());

        glDisable(GL_DEPTH_TEST);
        this.lineRenderer.draw(projection);

        this.textRenderer3D.draw(
                projection,
                getCameraRight().mul(-1),
                getCameraUp()
        );

        var ortho = new Matrix4f().ortho2D(0, this.width, 0, this.height);
        this.textRenderer2D.draw(ortho);

        glEnable(GL_DEPTH_TEST);

        var runtime = Runtime.getRuntime();
        var usedMemory = runtime.totalMemory() - runtime.freeMemory();
        var usedMemoryMB = usedMemory / (1024 * 1024);

        var title = String.format(
                "%4d FPS   %6.2f ms / %6.2f ms  %5d MB used",
                this.lastUpdatedFPS,
                updateTime * 1000f,
                dt * 1000f,
                usedMemoryMB
        );
        GLFW.glfwSetWindowTitle(this.window, title);

        this.deltaX = 0;
        this.deltaY = 0;
    }

    Camera.WorldCoords screenToWorldCoords(
            int width,
            int height,
            float x,
            float y
    ) {
        var inv = new Matrix4f();
        var projection = this.camera.getProjection(width, height);
        projection.invertPerspectiveView(this.camera.getView(), inv);
        var coords = Camera.screenToWorld(inv, x / width, y / height);
        return new Camera.WorldCoords(coords.dir(), this.camera.getPosition());
    }

    Matrix4f getViewProjection(int width, int height) {
        var projection = this.camera.getProjection(width, height);
        var view = this.camera.getView();
        return projection.mul(view);
    }
    Vector3f getCameraPosition() {
        return this.camera.getPosition();
    }

    Vector3f getCameraRight() {
        return Camera.getForward(new Vector3f(), this.camera.yaw + 90.0f, 0.0f);
    }

    Vector3f getCameraUp() {
        var forward = Camera.getForward(new Vector3f(), this.camera.yaw, this.camera.pitch);
        var right = getCameraRight();
        return forward.cross(right).normalize();
    }


    private void mouseMoveCallback(long window, double x_, double y_) {
        var x = (float) x_;
        var y = (float) y_;

        var dx = x - this.mouseX;
        var dy = y - this.mouseY;
        this.deltaX += dx;
        this.deltaY += dy;

        this.mouseX = x;
        this.mouseY = y;

    }

    private void mouseButtonCallback(long window, int button, int action, int mods) {
        if (button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS) {
            var coords = screenToWorldCoords(this.windowWidth, this.windowHeight, this.mouseX, this.mouseY);
            this.example.onClick(coords.dir(), coords.origin());
        }
    }

    private void keyCallback(long window, int key, int scancode, int action, int mods) {
        if (action != GLFW_PRESS && action != GLFW_REPEAT) {
            return;
        }

        if (key == GLFW_KEY_R) {
            if (action == GLFW_PRESS) {
                restart();
            }
            return;
        }

        if (key == GLFW_KEY_ESCAPE) {
            this.example.drawConfig.toggleShown();
            return;
        }

        if (key == GLFW_KEY_ENTER) {
            this.example.drawConfig.toggle();
            if (this.example.drawConfig.shown) {
                return;
            }
        }

        if (key == GLFW_KEY_DOWN) {
            this.example.drawConfig.moveDown();
            if (this.example.drawConfig.shown) {
                return;
            }
        }

        if (key == GLFW_KEY_UP) {
            this.example.drawConfig.moveUp();
            if (this.example.drawConfig.shown) {
                return;
            }
        }

        this.example.onKeyPress(key);

    }

    private void resizeCallback(long window, int width, int height) {
        if (width == 0 || height == 0) {
            return;
        }
        this.width = width;
        this.height = height;
        glViewport(0, 0, width, height);
    }
    private void resizeCallbackWindow(long window, int width, int height) {
        if (width == 0 || height == 0) {
            return;
        }
        this.windowWidth = width;
        this.windowHeight = height;
    }

    @Override
    public void close() {
        glfwSetWindowShouldClose(this.window, true);

        for (var closeable : this.closeables) {
            closeable.close();
        }

        glfwDestroyWindow(this.window);
        glfwFreeCallbacks(this.window);
        glfwTerminate();
        GL.setCapabilities(null);
    }

    private void setRealWindowSize(long window) {
        try (var stack = stackPush()) {
            val windowWidthPointer = stack.mallocInt(1);
            val windowHeightPointer = stack.mallocInt(1);
            glfwGetWindowSize(window, windowWidthPointer, windowHeightPointer);

            this.windowHeight = windowHeightPointer.get(0);
            this.windowWidth = windowWidthPointer.get(0);

        }


    }

    private void centerWindow(long window) {
        try (var stack = stackPush()) {
            val windowWidthPointer = stack.mallocInt(1);
            val windowHeightPointer = stack.mallocInt(1);
            glfwGetWindowSize(window, windowWidthPointer, windowHeightPointer);

            val video_mode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            if (video_mode == null) {
                System.err.println("Failed to get video mode for primary monitor, cannot center window");
                return;
            }

            var windowWidth = windowWidthPointer.get(0);
            var windowHeight = windowHeightPointer.get(0);

            var windowX = (video_mode.width() - windowWidth) / 2;
            var windowY = (video_mode.height() - windowHeight) / 2;
            glfwSetWindowPos(window, windowX, windowY);
        }
    }

    private <T extends NativeResource> T addCloseable(T closeable) {
        if (closeable == null) {
            return null;
        }
        this.closeables.add(closeable);
        return closeable;
    }


    private static class DebugMessageOutput extends GLDebugMessageCallback {

        private final PrintStream output;

        public DebugMessageOutput(PrintStream output) {
            this.output = output;
        }

        @Override
        public void invoke(
                int source, int type, int id, int severity, int length, long message,
                long userParam
        ) {

            if (severity == GL43.GL_DEBUG_SEVERITY_NOTIFICATION && type == GL43.GL_DEBUG_TYPE_OTHER) {
                return;
            }

            var sourceString = getSourceString(source);
            var typeString = getTypeString(type);
            var severityString = getSeverityString(severity);
            var msg = GLDebugMessageCallback.getMessage(length, message);

            if (
                    severity == GL43.GL_DEBUG_SEVERITY_MEDIUM
                    && type == GL43.GL_DEBUG_TYPE_PERFORMANCE
                    && msg.contains("Program/shader state performance warning:")
                    && msg.contains("is being recompiled based on GL state.")
            ) {
                return;
            }

            this.output.printf("""
                    GL DEBUG MESSAGE:
                    Source: %s
                    Type: %s
                    ID: %d
                    Severity: %s
                    Message: %s
                    
                    """, sourceString, typeString, id, severityString, msg
            );
        }

        private String getSourceString(int source) {
            return switch (source) {
                case GL43.GL_DEBUG_SOURCE_API -> "API";
                case GL43.GL_DEBUG_SOURCE_WINDOW_SYSTEM -> "Window System";
                case GL43.GL_DEBUG_SOURCE_SHADER_COMPILER -> "Shader Compiler";
                case GL43.GL_DEBUG_SOURCE_THIRD_PARTY -> "Third Party";
                case GL43.GL_DEBUG_SOURCE_APPLICATION -> "Application";
                case GL43.GL_DEBUG_SOURCE_OTHER -> "Other";
                default -> "Unknown";
            };
        }

        private String getTypeString(int type) {
            return switch (type) {
                case GL43.GL_DEBUG_TYPE_ERROR -> "Error";
                case GL43.GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR -> "Deprecated Behavior";
                case GL43.GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR -> "Undefined Behavior";
                case GL43.GL_DEBUG_TYPE_PORTABILITY -> "Portability";
                case GL43.GL_DEBUG_TYPE_PERFORMANCE -> "Performance";
                case GL43.GL_DEBUG_TYPE_MARKER -> "Marker";
                case GL43.GL_DEBUG_TYPE_PUSH_GROUP -> "Push Group";
                case GL43.GL_DEBUG_TYPE_POP_GROUP -> "Pop Group";
                case GL43.GL_DEBUG_TYPE_OTHER -> "Other";
                default -> "Unknown";
            };
        }

        private String getSeverityString(int severity) {
            return switch (severity) {
                case GL43.GL_DEBUG_SEVERITY_HIGH -> "High";
                case GL43.GL_DEBUG_SEVERITY_MEDIUM -> "Medium";
                case GL43.GL_DEBUG_SEVERITY_LOW -> "Low";
                case GL43.GL_DEBUG_SEVERITY_NOTIFICATION -> "Notification";
                default -> "Unknown";
            };
        }
    }



}
