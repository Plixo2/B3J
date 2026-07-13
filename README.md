# B3J

[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](https://opensource.org/licenses/MIT)

![Box3D Logo](https://box2d.org/images/logo.svg)



B3J is a Java binding library for [Box3D](https://github.com/erincatto/box3d).


It is designed to be efficient yet convenient, 
staying close to the original Box3D API. 

> [!WARNING]  
> Still in development. Somewhat usable, but far from feature complete.


### [Examples](examples/src/main/java/io/github/plixo2/samples)

- `lwjgl` is used for rendering with OpenGL > 4.3
- Start them via `.\gradlew :examples:run` or the [instance main methods](https://openjdk.org/jeps/445)

## Usage (and Box3d differences)

- [JOML](https://github.com/JOML-CI/JOML) is used for fast math and vector operations.
  - A `Matrix4f` is used as a rigid transform instead of `b3Transform`.
  - `Vector3f` and `Quaternionf` are used for `b3Vec3` and `b3Quat`, `Matrix3f` for `b3Matrix3`.
  - JOML is already widely used in the Java (game) ecosystem and can offer more than Box3D's math.

  
- Similar API to Box3D, but not identical.
  - Constructors are used, instead of `b3Default...` methods and zero-initialized structs.
  - All other methods are found on the [B3](src/main/java/io/github/plixo2/box3d/B3.java) class.
  - Types & Methods are not prefixed with 'b3' and follow Java naming conventions.
    - E.g. `b3Body_SetTransform` becomes `b3.bodySetTransform`.
  - [UserData](src/main/java/io/github/plixo2/box3d/UserData.java) and [PointerData](src/main/java/io/github/plixo2/box3d/PointerData.java) 
    can be used as a replacement for `userData` to associate data with Box3D objects.  
  - [WorldDef](src/main/java/io/github/plixo2/box3d/WorldDef.java) has some differences to `b3WorldDef`:
    - [TaskScheduler](src/main/java/io/github/plixo2/box3d/tasks/TaskScheduler.java) replaces `enqueueTask`, `finishTask`, `userTaskContext` and `workerCount`.
    - [DebugShapeCallbacks](src/main/java/io/github/plixo2/box3d/DebugShapeCallbacks.java) replaces `createDebugShape` and `destroyDebugShape`.
      - It requires a `UserData.OfShape<T>` to be passed to store the `userShape` for each shape.
      - Provide the same `UserData.OfShape<T>` to [DebugDraw](src/main/java/io/github/plixo2/box3d/DebugDraw.java) so the `drawShapeFcn` can be called with the correct `userShape`.
  - `JointID` has a generic type for the `JointType`. You can use `jointId.reinterpret(JointType)` to freely reinterpret it.


- Thread safety
  - B3J allocates a small amount of memory upfront for efficient c calls.
  - `B3.get()` will give you a thread unique instance. 
    - Dont share a instance of `B3` between threads.
    - You probably operate on a single thread, so can declare a `B3` instance as a global once.


- Memory Efficient
  - B3J allocates as few objects as possible and reuses existing objects. 
  - Some method require a `dest` parameter to be filled in. E.g. `Vector3f bodyGetPosition(Vector3f dest, BodyID bodyId)`.
    In that case, you can just pass in a new object using the default constructor, or reuse a existing one.
  - Arguments of callbacks or custom Iterators can be mutable. 
    Make sure to copy the values, if you need the data beyond the lifetime of the callback or iterator.
    All mutable objects have a copy constructor that you can use, although it will be cheaper to just copy the fields you need.
  - Some arguments can be `null` when they are not needed.
  - Some methods that require callbacks are implemented in c. All arguments are recorded and replayed in java avoid the overhead of upcalls.


- Fearless Resource Management
  - A [Region](src/main/java/io/github/plixo2/box3d/region/Region.java) can be used to manage resources. 
  - `BodyID`, `WorldID`, `MeshData` and `HeightFieldData` are managed by regions. 
    - The lifetime of these objects is dictated by the region.
    - See [Region](src/main/java/io/github/plixo2/box3d/region/Region.java) for more details and available regions.
  - You can also destroy all objects manually.


- Null Safety
  - B3J uses `@Nullable` annotations for values that can be null. Otherwise, assume non-null. 
    Dont break this contract!

    
- Unsigned values are annotated with `@Unsigned`
  - Make sure you operate with the correct types. [Read more](src/main/java/io/github/plixo2/box3d/internal/Unsigned.java)



## Compatibility

B3J currently supports Windows and Linux. 
I plan to support MacOS and other platforms/architectures in the future, if there is enough interest.

B3J currently builds with Java 25. May switch to Java 21 for compatibility in the future.   

## License

Box3D is developed by Erin Catto and uses the [MIT license](https://en.wikipedia.org/wiki/MIT_License).

This Project also uses the MIT license.

## Sponsorship

Consider supporting the **original** development of Box3D through [Github Sponsors](https://github.com/sponsors/erincatto).

Please consider giving this repository a star and dont forget to star the [original](https://github.com/erincatto/box3d).

## LLM Usage

I used LLMs in the following areas:

- Code reviews
- Repetitive, simple function implementations

All other code is developed and written by me and i take responsibility for every line of code.
