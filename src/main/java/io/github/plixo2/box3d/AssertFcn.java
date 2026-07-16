package io.github.plixo2.box3d;


@FunctionalInterface
public interface AssertFcn {

    /// A [AssertionError] with the parameters will be thrown after this method,
    /// so no need to throw or log to the console yourself.
    ///
    /// We cannot exit a assertion gracefully, so we have to throw an [Error].
    /// Java will then exit the VM.
    ///
    /// [#unsafeDecideOnFailure] can override this behavior.
    void onFailure(String condition, String fileName, int lineNumber);


    /// Override this method to decide whether to continue execution.
    /// This will lead to undefined behavior / crashes.
    /// @return true to continue execution, false to exit
    default boolean unsafeDecideOnFailure(String condition, String fileName, int lineNumber) {
        onFailure(condition, fileName, lineNumber);
        return false;
    }

}
