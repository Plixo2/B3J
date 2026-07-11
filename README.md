# B3J

[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](https://opensource.org/licenses/MIT)

![Box3D Logo](https://box2d.org/images/logo.svg)



B3J is a Java binding library for [Box3D](https://github.com/erincatto/box3d).

It is designed to be efficient yet convenient, 
staying as close as possible to the original Box3D API. 

> [!WARNING]  
> Still in development. Probably not usable and far from feature complete.


### [Examples](examples/src/main/java/io/github/plixo2/samples)

- `lwjgl` used for rendering with OpenGL > 4.3
- Use the [instance Main Methods](https://openjdk.org/jeps/445) to run the examples.

## Usage (and Box3d differences)

- [JOML](https://github.com/JOML-CI/JOML) is used for fast math and vector operations.
  - A `Matrix4f` is used as a rigid transform instead of `b3Transform`.
  - `Vector3f` and `Quaternionf` are used for `b3Vec3` and `b3Quat`.

- Types & Methods are not prefixed with 'b3' and follow Java naming conventions.
  - E.g. `b3Body_SetTransform` becomes `b3.bodySetTransform`

- Similar API to Box3D, but no identical.
  - Constructors are used, instead of `b3Default...` methods 
  - All other methods are found on the [B3](src/main/java/io/github/plixo2/box3d/B3.java) class:

- Thread safety 
  - B3J allocates a small amount of memory upfront for efficient c calls
  - `B3.get()` will give you a thread unique instance. Dont share this across threads.

- Memory Efficient
  - B3J allocates as few objects as possible and reuses existing objects. 
  - Some method require a `in` parameter to be filled in. E.g. `Vector3f bodyGetPosition(Vector3f in, BodyID bodyId)`
  - Arguments of callbacks or custom Iterators can be mutable. Make sure to copy them if you want to keep the objects.
  - Some arguments can be `null`, when they are not needed.

- Fearless Resource Management
  - A [Region](src/main/java/io/github/plixo2/box3d/region/Region.java) is used to manage resources. 
  - `BodyID`, `WorldID`, `MeshData` and `HeightFieldData` are managed by regions. 
    - They will be automatically destroyed when the region is closed.
    - You can also destroy them manually beforehand.
  - See [Region](src/main/java/io/github/plixo2/box3d/region/Region.java) for more details and available regions.

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
