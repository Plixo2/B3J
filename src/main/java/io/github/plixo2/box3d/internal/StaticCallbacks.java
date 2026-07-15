package io.github.plixo2.box3d.internal;

import io.github.plixo2.box3d.AllocNFreeFcn;
import io.github.plixo2.box3d.AssertFcn;
import io.github.plixo2.box3d.LogFcn;
import org.box2d.box3d.*;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Objects;

/// Class for callback functions that last the entire lifetime of the program:
///
/// - [AssertFcn] for `b3AssertFcn`
/// - [LogFcn] for `b3LogFcn`
/// - [AllocNFreeFcn] for `b3AllocFcn` and `b3FreeFcn`
///
///
public class StaticCallbacks {
    private final static Object lock = new Object();

    // nothing to log, `AssertionError` will be thrown anyway
    private static final AssertFcn defaultAssertFcn = (_, _, _) -> {};

    private static final LogFcn defaultLogFcn = B3JUtil::log;

    private static final @Nullable AllocNFreeFcn defaultAllocNFreeFcn = null;

    @SuppressWarnings("unused")
    private static class KeepAlive {
        private static @Nullable AllocCallback assertCallback;
        private static @Nullable MemorySegment assertFcnSegment;

        private static @Nullable LogCallback logCallback;
        private static @Nullable MemorySegment logFcnSegment;

        private static @Nullable AllocNFreeCallback allocNFreeCallback;
        private static @Nullable MemorySegment allocFcnSegment;
        private static @Nullable MemorySegment freeFcnSegment;
    }

    public static void installDefault() {
        installAssersionHandler(defaultAssertFcn);
        installLogHandler(defaultLogFcn);
        installAllocNFreeHandler(defaultAllocNFreeFcn);
    }

    public static void installAssersionHandler(@Nullable AssertFcn fcn) {

        synchronized (lock) {
            // `fcn` cannot be null by box3d, so we fall back to the default
            var assertFcn = Objects.requireNonNullElse(fcn, defaultAssertFcn);
            var callback = KeepAlive.assertCallback = new AllocCallback(assertFcn);
            var segment = KeepAlive.assertFcnSegment = b3AssertFcn.allocate(callback, Arena.ofAuto());
            box3d_h.b3SetAssertFcn(segment);
        }

    }

    public static void installLogHandler(@Nullable LogFcn fcn) {

        synchronized (lock) {
            // `fcn` cannot be null by box3d, so we fall back to the default
            var logFcn = Objects.requireNonNullElse(fcn, defaultLogFcn);
            var callback = KeepAlive.logCallback = new LogCallback(logFcn);
            var segment = KeepAlive.logFcnSegment = b3LogFcn.allocate(callback, Arena.ofAuto());
            box3d_h.b3SetLogFcn(segment);
        }

    }

    public static void installAllocNFreeHandler(@Nullable AllocNFreeFcn fcn) {

        synchronized (lock) {
            if (fcn == null) {
                KeepAlive.allocNFreeCallback = null;
                var allocSegment = KeepAlive.allocFcnSegment = MemorySegment.NULL;
                var freeSegment = KeepAlive.freeFcnSegment = MemorySegment.NULL;
                box3d_h.b3SetAllocator(allocSegment, freeSegment);
            } else {
                var callback = KeepAlive.allocNFreeCallback = new AllocNFreeCallback(fcn);
                var arena = Arena.ofAuto();
                var allocSegment = KeepAlive.allocFcnSegment = b3AllocFcn.allocate(callback, arena);
                var freeSegment = KeepAlive.freeFcnSegment = b3FreeFcn.allocate(callback, arena);
                box3d_h.b3SetAllocator(allocSegment, freeSegment);
            }
        }

    }



    private record AllocCallback(AssertFcn fcn) implements b3AssertFcn.Function {
        @Override
        public int apply(MemorySegment conditionSegment, MemorySegment fileNameSegment, int lineNumber) {
            var condition = B3JUtil.getNullString(conditionSegment, "");
            var fileName = B3JUtil.getNullString(fileNameSegment, "");

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

        private static void bail(String condition, String fileName, int lineNumber) {
            var message = "[B3J]: Assertion failed: " + condition + " at " + fileName + ":" + lineNumber;
            throw new AssertionError(message);
        }

    }

    private record LogCallback(LogFcn fcn) implements b3LogFcn.Function {

        @Override
        public void apply(MemorySegment message) {
            var msg = B3JUtil.getNullString(message, "");
            try {
                this.fcn.onLog(msg);
            } catch(Exception e) {
                B3JUtil.unhandledCallbackException(e);
            }
        }
    }

    private record AllocNFreeCallback(AllocNFreeFcn fcn) implements b3AllocFcn.Function , b3FreeFcn.Function {

        /// b3AllocFcn
        @Override
        public MemorySegment apply(int size, int alignment) {
            try {
                return this.fcn.alloc(size, alignment);
            } catch(Exception e) {
                B3JUtil.unhandledCallbackException(e);

                // we have to bail, returning MemorySegment.NULL would
                // cause a assertion in box3d
                throw new AssertionError(e);
            }
        }

        /// b3FreeFcn
        @Override
        public void apply(MemorySegment mem) {
            try {
                this.fcn.free(mem);
            } catch(Exception e) {
                B3JUtil.unhandledCallbackException(e);
                // may leak, but thats on you
            }
        }
    }

}
