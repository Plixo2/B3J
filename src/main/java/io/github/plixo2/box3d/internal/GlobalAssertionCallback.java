package io.github.plixo2.box3d.internal;

import io.github.plixo2.box3d.AssertFcn;
import org.box2d.box3d.b3AssertFcn;
import org.box2d.box3d.box3d_h;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Objects;

public class GlobalAssertionCallback {
    private final static Object lock = new Object();

    private static final AssertFcn defaultAssertFcn = (_, _, _) -> {};

    private static @Nullable Callback function;              // keep alive
    private static @Nullable MemorySegment assertFcnSegment; // keep alive

    public static void installDefault() {
        install(defaultAssertFcn);
    }

    public static void install(@Nullable AssertFcn fcn) {

        synchronized (lock) {
            var assertFcn = Objects.requireNonNullElse(fcn, defaultAssertFcn);
            function = new Callback(assertFcn);
            assertFcnSegment = b3AssertFcn.allocate(function, Arena.ofAuto());
            box3d_h.b3SetAssertFcn(assertFcnSegment);
        }

    }

    private static void bail(String condition, String fileName, int lineNumber) {
        var message = "[B3J] Assertion failed: " + condition + " at " + fileName + ":" + lineNumber;
        throw new AssertionError(message);
    }


    private record Callback(AssertFcn fcn) implements b3AssertFcn.Function {
        @Override
        public int apply(MemorySegment conditionSegment, MemorySegment fileNameSegment, int lineNumber) {
            var condition = conditionSegment.getString(0);
            var fileName = fileNameSegment.getString(0);

            boolean continueExecution = false;
            try {
                continueExecution = this.fcn.unsafeDecideOnFailure(
                        condition,
                        fileName,
                        lineNumber
                );
            } catch (Exception e) {
                B3JUtil.unhandledCallbackException(e);
            }

            if (continueExecution) {
                return 0;
            }

            bail(condition, fileName, lineNumber);
            return 0; // unreachable
        }
    }

}
