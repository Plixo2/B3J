package io.github.plixo2;


import io.github.plixo2.abstraction.GLResourceManagement;
import io.github.plixo2.box3d.*;
import io.github.plixo2.box3d.region.Region;
import lombok.val;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
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


    int width = 800;
    int height = 600;
    String title = "Box3D examples";
    long window;

    private float mouseX, mouseY;
    private float deltaX, deltaY;

    private final List<NativeResource> closeables = new ArrayList<>();

    private final SceneRendering sceneRendering;

    private final Example example;
    private DebugDraw debugDraw;

    private long lastFrameTime;
    private long lastFPSUpdate;
    private int lastUpdatedFPS = 0;
    private int fpsCounter = 0;


    public Entry(Example example) {
        System.setProperty("joml.format", "false");

        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 5);

        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_COMPAT_PROFILE);

        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        }

        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_SAMPLES, 4);
        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);


        this.window = glfwCreateWindow(this.width, this.height, this.title, NULL, NULL);
        if (this.window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        addCloseable(GLFWKeyCallback.create(this::keyCallback));
        glfwSetKeyCallback(this.window, (GLFWKeyCallback) this.closeables.getLast());

        addCloseable(GLFWCursorPosCallback.create(this::mouseMoveCallback));
        glfwSetCursorPosCallback(this.window, (GLFWCursorPosCallback) this.closeables.getLast());

        addCloseable(GLFWMouseButtonCallback.create(this::mouseButtonCallback));
        glfwSetMouseButtonCallback(this.window, (GLFWMouseButtonCallback) this.closeables.getLast());

        addCloseable(GLFWFramebufferSizeCallback.create(this::resizeCallback));
        glfwSetFramebufferSizeCallback(this.window, (GLFWFramebufferSizeCallback) this.closeables.getLast());


        centerWindow(this.window);

        glfwMakeContextCurrent(this.window);
        glfwShowWindow(this.window);

        GL.createCapabilities();

        glfwSwapInterval(1);

        GL43.glEnable(GL43.GL_DEBUG_OUTPUT);
        GL43.glEnable(GL43.GL_DEBUG_OUTPUT_SYNCHRONOUS);
        var callback = addCloseable(new DebugMessageOutput(System.err));
        GL43.glDebugMessageCallback(callback, 0);

        glEnable(GL_MULTISAMPLE);
        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_LINE_SMOOTH);

        glEnable(GL_BLEND);
        glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

        this.lastFrameTime = System.nanoTime();

        this.sceneRendering = new SceneRendering(this.window);

        this.example = example;

        initExample();
    }

    private void restart() {
        this.example.region.close();
        this.example.region = Region.ofConfined();
        initExample();
    }

    private void initExample() {
        var meshFactory = new MeshFactory();

        this.example.init(meshFactory);

        this.debugDraw = new DebugDraw(meshFactory, this.sceneRendering);
        this.debugDraw.drawShapes = true;

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

        this.sceneRendering.update(
                this.width,
                this.height,
                this.deltaX,
                this.deltaY,
                dt
        );

        var worldID = this.example.worldID;
        if (worldID != null) {
            this.example.b3.worldDraw(worldID, this.debugDraw, U64_MAX);
        }

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
            var coords = this.sceneRendering.screenToWorldCoords(this.width, this.height, this.mouseX, this.mouseY);
            this.example.onClick(coords.dir(), coords.origin());
        }
    }

    private void keyCallback(long window, int key, int scancode, int action, int mods) {
        if (action == GLFW_PRESS) {
            if (key == GLFW_KEY_ESCAPE) {
                glfwSetWindowShouldClose(window, true);
            } else if (key == GLFW_KEY_R) {
                restart();
            } else {
                this.example.onKeyPress(key);
            }
        }
    }

    private void resizeCallback(long window, int width, int height) {
        if (width == 0 || height == 0) {
            return;
        }
        this.width = width;
        this.height = height;
        glViewport(0, 0, width, height);
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
            if (severity == GL43.GL_DEBUG_SEVERITY_NOTIFICATION &&
                    type == GL43.GL_DEBUG_TYPE_OTHER) {
                return;
            }


            var sourceString = getSourceString(source);
            var typeString = getTypeString(type);
            var severityString = getSeverityString(severity);
            var msg = GLDebugMessageCallback.getMessage(length, message);

            if (severity == GL43.GL_DEBUG_SEVERITY_MEDIUM
                    && type == GL43.GL_DEBUG_TYPE_PERFORMANCE
                    && msg.contains("Program/shader state performance warning:")
                    && msg.contains("is being recompiled based on GL state.")
            ) {
                return;
            }

            this.output.printf(
                    """
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
