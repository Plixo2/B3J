# B3J 

B3J is a handwritten Java wrapper for [Box3D](https://github.com/erincatto/box3d).
It is designed to be efficient, but convenient, and as close to the original Box3D as possible.


### [Examples](examples/src/main/java/io/github/plixo2/samples)

- `lwjgl` used for rendering with OpenGL 4.5
- Use the [instance Main Methods](https://openjdk.org/jeps/445) to run the examples.

## Usage (and Box3d differences)

- Constructors are used, instead of `b3Default...` methods
- [JOML](https://github.com/JOML-CI/JOML) is used for math and vector operations.
- Types & Methods are not prefixed with `b3` and follow Java naming conventions.
E.g. `b3Body_SetTransform` becomes `b3.bodySetTransform`

- All methods are found on the [B3](src/main/java/io/github/plixo2/box3d/B3.java) class:
  - B3J allocates a small amount of memory upfront for efficient c calls
  - `B3.get()` will give you a thread unique instance. Dont share this between threads.

- B3J allocates as few objects as possible and reuses existing objects. 
  - Some method require a `in` parameter to be filled. E.g. `Vector3f bodyGetPosition(Vector3f in, BodyID bodyId)`
  - Arguments of callbacks or custom Iterators can be mutable. Make sure to copy them if you want to keep them.

- A [Region](src/main/java/io/github/plixo2/box3d/region/Region.java) is used to manage lifetimes.
  - `Region.ofConfined()` creates a confined region, similar to `Arena.ofConfined()`
  - `Region.ofConfined(Region parent`) creates a confined region with a parent region. 
    The parent region will close the child, but the child region can be closed independently.
  - `Region.ofGlobal()` creates a global region, which is never closed. 
    Use this, for example, for objects that are destroyed by Box3D when the world is destroyed.
  - `Region.ofAuto(FreeList freeList)` creates a region managed by the garbage collector. 
    - Unlike `Arena.ofAuto()`, it takes a [FreeList](src/main/java/io/github/plixo2/box3d/region/FreeList.java) 
    to be drained on your own thread. 

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

- Repetitive c to java abstractions
- Code reviews

Elsewhere, just like the original Box3D repository, all code is developed and written by me. 
I take responsibility for every line of code.
