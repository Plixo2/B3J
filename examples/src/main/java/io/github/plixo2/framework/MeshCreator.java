package io.github.plixo2.framework;

import io.github.plixo2.abstraction.Mesh;
import io.github.plixo2.abstraction.Shader;
import io.github.plixo2.box3d.*;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class MeshCreator {

    private static final int VERTEX_FLOATS = 6;
    private static final int SPHERE_LATITUDE_COUNT = 16;
    private static final int SPHERE_LONGITUDE_COUNT = 32;
    private static final int CAPSULE_RADIAL_COUNT = 24;
    private static final int CAPSULE_CAP_RING_COUNT = 8;

    public record MeshArgs(
            float[] verticies,
            int[] indices
    ) {}


    static MeshArgs createCapsule(Capsule capsule) {

        float[] c1 = new float[]{capsule.x1, capsule.y1, capsule.z1};
        float[] c2 = new float[]{capsule.x2, capsule.y2, capsule.z2};
        float radius = capsule.radius;

        float ax = c2[0] - c1[0];
        float ay = c2[1] - c1[1];
        float az = c2[2] - c1[2];
        float length = (float) Math.sqrt(ax * ax + ay * ay + az * az);
        if (length < 1.0e-6f) {
            return createUvSphereMesh(c1[0], c1[1], c1[2], radius, SPHERE_LATITUDE_COUNT, SPHERE_LONGITUDE_COUNT);
        }

        ax /= length;
        ay /= length;
        az /= length;

        float[] tangent = perpendicular(ax, ay, az);
        float tx = tangent[0];
        float ty = tangent[1];
        float tz = tangent[2];
        float bx = ay * tz - az * ty;
        float by = az * tx - ax * tz;
        float bz = ax * ty - ay * tx;

        List<float[]> vertices = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        addCapsuleRing(vertices, c1, ax, ay, az, tx, ty, tz, bx, by, bz, -1.0f, 0.0f, radius);

        for (int i = 1; i <= CAPSULE_CAP_RING_COUNT; i++) {
            float phi = (float) (-Math.PI * 0.5 + i * (Math.PI * 0.5 / CAPSULE_CAP_RING_COUNT));
            addCapsuleRing(vertices, c1, ax, ay, az, tx, ty, tz, bx, by, bz, (float) Math.sin(phi), (float) Math.cos(phi), radius);
        }

        addCapsuleRing(vertices, c2, ax, ay, az, tx, ty, tz, bx, by, bz, 0.0f, 1.0f, radius);

        for (int i = 1; i <= CAPSULE_CAP_RING_COUNT; i++) {
            float phi = i * (float) (Math.PI * 0.5 / CAPSULE_CAP_RING_COUNT);
            addCapsuleRing(vertices, c2, ax, ay, az, tx, ty, tz, bx, by, bz, (float) Math.sin(phi), (float) Math.cos(phi), radius);
        }

        int ringCount = 2 * CAPSULE_CAP_RING_COUNT + 2;
        for (int ring = 0; ring + 1 < ringCount; ring++) {
            int base0 = ring * CAPSULE_RADIAL_COUNT;
            int base1 = (ring + 1) * CAPSULE_RADIAL_COUNT;
            for (int segment = 0; segment < CAPSULE_RADIAL_COUNT; segment++) {
                int next = (segment + 1) % CAPSULE_RADIAL_COUNT;
                addTriangle(indices, base0 + segment, base1 + next, base1 + segment);
                addTriangle(indices, base0 + segment, base0 + next, base1 + next);
            }
        }

        return makeMesh(vertices, indices);
    }

    static MeshArgs createHeightField(HeightFieldData heightField) {

        int columns = heightField.columnCount();
        int rows = heightField.rowCount();
        var heights = heightField.heightIterator().collect();

        if (columns <= 0 || rows <= 0 || heights.length == 0) {
            return emptyMesh();
        }

        var scale = heightField.scale(new Vector3f());
        float sx = scale.x;
        float sy = scale.y;
        float sz = scale.z;
        float minHeight = heightField.minHeight();
        float heightScale = heightField.heightScale();

        int vertexCount = rows * columns;
        float[] positions = new float[vertexCount * 3];
        float[] normals = new float[vertexCount * 3];


        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                int index = row * columns + column;
                int compressedHeight = heights[index];
                positions[index * 3] = column * sx;
                positions[index * 3 + 1] = (minHeight + compressedHeight * heightScale) * sy;
                positions[index * 3 + 2] = row * sz;
            }
        }

        List<Integer> indices = new ArrayList<>();
        var materials = heightField.materialIterator().collect();
        boolean clockwise = heightField.clockwise();

        for (int row = 0; row + 1 < rows; row++) {
            for (int column = 0; column + 1 < columns; column++) {

                if (materials.length > 0) {
                    int cell = row * (columns - 1) + column;
                    var material = materials[cell];
                    if (material == B3.HEIGHT_FIELD_HOLE) {
                        continue;
                    }
                }

                int i00 = row * columns + column;
                int i10 = row * columns + column + 1;
                int i01 = (row + 1) * columns + column;
                int i11 = (row + 1) * columns + column + 1;

                if (clockwise) {
                    addTriangle(indices, i00, i10, i11);
                    addTriangle(indices, i00, i11, i01);
                    accumulateTriangleNormal(positions, normals, i00, i10, i11);
                    accumulateTriangleNormal(positions, normals, i00, i11, i01);
                } else {
                    addTriangle(indices, i00, i11, i10);
                    addTriangle(indices, i00, i01, i11);
                    accumulateTriangleNormal(positions, normals, i00, i11, i10);
                    accumulateTriangleNormal(positions, normals, i00, i01, i11);
                }
            }
        }

        normalizeNormals(normals);
        return makeMesh(positions, normals, toIntArray(indices));
    }

    static MeshArgs createHull(HullData hull) {

        var faces = hull.faceIterator(new HullFace(0));
        var points = hull.pointIterator(new Vector3f());
        var edges = hull.edgeIterator(new HullHalfEdge(0, 0, 0, 0));
        var planes = hull.planeIterator(new Plane(0, 0, 0, 0));

        if (faces.length() == 0 || points.length() == 0 || edges.length() == 0 || planes.length() == 0) {
            return emptyMesh();
        }

        List<float[]> vertices = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        for (int faceIndex = 0; faceIndex < faces.length(); faceIndex++) {
            int firstEdge = faces.get(faceIndex).edge;
            int edgeIndex = firstEdge;
            int guard = 0;

            List<Integer> facePointIndices = new ArrayList<>();
            do {
                var edge = edges.get(edgeIndex);
                facePointIndices.add(edge.origin);
                edgeIndex = edge.next;
                guard++;
            } while (edgeIndex != firstEdge && guard <= 256);

            if (facePointIndices.size() < 3 || guard > 256) {
                continue;
            }

            var plane = planes.get(faceIndex);

            int baseVertex = vertices.size();
            for (int pointIndex : facePointIndices) {
                var point = points.get(pointIndex);
                vertices.add(new float[] {
                        point.x, point.y, point.z,
                        plane.normalX, plane.normalY, plane.normalZ
                });
            }

            for (int i = 1; i + 1 < facePointIndices.size(); i++) {
                indices.add(baseVertex);
                indices.add(baseVertex + i);
                indices.add(baseVertex + i + 1);
            }
        }

        return makeMesh(vertices, indices);
    }

    static MeshArgs createMesh(io.github.plixo2.box3d.Mesh mesh) {

        var data = mesh.data;

        var vertices = data.vertexIterator(new Vector3f());
        var triangles = data.triangleIterator(new MeshTriangle(0, 0, 0));

        var vertexCount = vertices.length();
        var triangleCount = triangles.length();

        if (vertexCount == 0 || triangleCount == 0) {
            return emptyMesh();
        }

        float sx = mesh.scaleX;
        float sy = mesh.scaleY;
        float sz = mesh.scaleZ;

        float[] positions = new float[vertexCount * 3];
        float[] normals = new float[vertexCount * 3];
        for (int i = 0; i < vertexCount; i++) {
            var point = vertices.get(i);
            positions[i * 3 + 0] = point.x * sx;
            positions[i * 3 + 1] = point.y * sy;
            positions[i * 3 + 2] = point.z * sz;
        }

        int[] indices = new int[triangleCount * 3];
        for (int i = 0; i < triangleCount; i++) {
            var triangle = triangles.get(i);
            indices[i * 3 + 0] = triangle.index1;
            indices[i * 3 + 1] = triangle.index2;
            indices[i * 3 + 2] = triangle.index3;
            accumulateTriangleNormal(
                    positions,
                    normals,
                    triangle.index1,
                    triangle.index2,
                    triangle.index3
            );
        }

        normalizeNormals(normals);
        return makeMesh(positions, normals, indices);
    }

    static MeshArgs createSphere(Sphere sphere) {

        return createUvSphereMesh(
                sphere.x,
                sphere.y,
                sphere.z,
                sphere.radius,
                SPHERE_LATITUDE_COUNT,
                SPHERE_LONGITUDE_COUNT
        );
    }

    private static MeshArgs createUvSphereMesh(
            float cx,
            float cy,
            float cz,
            float radius,
            int latitudeCount,
            int longitudeCount
    ) {
        List<float[]> vertices = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        for (int lat = 0; lat <= latitudeCount; lat++) {
            float theta = (float) (-Math.PI * 0.5 + Math.PI * lat / latitudeCount);
            float y = (float) Math.sin(theta);
            float ringRadius = (float) Math.cos(theta);

            for (int lon = 0; lon < longitudeCount; lon++) {
                float phi = (float) (2.0 * Math.PI * lon / longitudeCount);
                float nx = ringRadius * (float) Math.cos(phi);
                float ny = y;
                float nz = ringRadius * (float) Math.sin(phi);
                vertices.add(new float[]{
                        cx + radius * nx, cy + radius * ny, cz + radius * nz,
                        nx, ny, nz
                });
            }
        }

        for (int lat = 0; lat < latitudeCount; lat++) {
            int row0 = lat * longitudeCount;
            int row1 = (lat + 1) * longitudeCount;
            for (int lon = 0; lon < longitudeCount; lon++) {
                int next = (lon + 1) % longitudeCount;
                addTriangle(indices, row0 + lon, row1 + lon, row1 + next);
                addTriangle(indices, row0 + lon, row1 + next, row0 + next);
            }
        }

        return makeMesh(vertices, indices);
    }

    private static void addCapsuleRing(
            List<float[]> vertices,
            float[] center,
            float ax,
            float ay,
            float az,
            float tx,
            float ty,
            float tz,
            float bx,
            float by,
            float bz,
            float axisNormal,
            float radialScale,
            float radius
    ) {
        for (int segment = 0; segment < CAPSULE_RADIAL_COUNT; segment++) {
            float angle = (float) (2.0 * Math.PI * segment / CAPSULE_RADIAL_COUNT);
            float radialX = (float) Math.cos(angle) * tx + (float) Math.sin(angle) * bx;
            float radialY = (float) Math.cos(angle) * ty + (float) Math.sin(angle) * by;
            float radialZ = (float) Math.cos(angle) * tz + (float) Math.sin(angle) * bz;
            float nx = axisNormal * ax + radialScale * radialX;
            float ny = axisNormal * ay + radialScale * radialY;
            float nz = axisNormal * az + radialScale * radialZ;
            float inverseLength = inverseLength(nx, ny, nz);
            nx *= inverseLength;
            ny *= inverseLength;
            nz *= inverseLength;
            vertices.add(new float[]{
                    center[0] + radius * nx,
                    center[1] + radius * ny,
                    center[2] + radius * nz,
                    nx, ny, nz
            });
        }
    }

    private static MeshArgs makeMesh(List<float[]> vertices, List<Integer> indices) {
        float[] vertexData = new float[vertices.size() * VERTEX_FLOATS];
        for (int i = 0; i < vertices.size(); i++) {
            System.arraycopy(vertices.get(i), 0, vertexData, i * VERTEX_FLOATS, VERTEX_FLOATS);
        }

        int[] indexArray = toIntArray(indices);

        return new MeshArgs(vertexData, indexArray);
    }

    private static MeshArgs makeMesh(float[] positions, float[] normals, int[] indices) {
        if (positions.length != normals.length) {
            throw new IllegalArgumentException("Positions and normals must have the same length");
        }
        int vertexCount = positions.length / 3;

        float[] vertexData = new float[vertexCount * VERTEX_FLOATS];
        for (int i = 0; i < vertexCount; i++) {
            vertexData[i * VERTEX_FLOATS + 0] = positions[i * 3 + 0];
            vertexData[i * VERTEX_FLOATS + 1] = positions[i * 3 + 1];
            vertexData[i * VERTEX_FLOATS + 2] = positions[i * 3 + 2];

            vertexData[i * VERTEX_FLOATS + 3] = normals[i * 3 + 0];
            vertexData[i * VERTEX_FLOATS + 4] = normals[i * 3 + 1];
            vertexData[i * VERTEX_FLOATS + 5] = normals[i * 3 + 2];
        }

        return new MeshArgs(vertexData, indices);
    }

    private static MeshArgs emptyMesh() {
        return new MeshArgs(new float[0], new int[0]);
    }

    private static void addTriangle(List<Integer> indices, int i1, int i2, int i3) {
        indices.add(i1);
        indices.add(i2);
        indices.add(i3);
    }

    private static int[] toIntArray(List<Integer> values) {
        int[] result = new int[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i);
        }
        return result;
    }

    private static void accumulateTriangleNormal(float[] positions, float[] normals, int i1, int i2, int i3) {
        float ax = positions[i2 * 3] - positions[i1 * 3];
        float ay = positions[i2 * 3 + 1] - positions[i1 * 3 + 1];
        float az = positions[i2 * 3 + 2] - positions[i1 * 3 + 2];
        float bx = positions[i3 * 3] - positions[i1 * 3];
        float by = positions[i3 * 3 + 1] - positions[i1 * 3 + 1];
        float bz = positions[i3 * 3 + 2] - positions[i1 * 3 + 2];
        float nx = ay * bz - az * by;
        float ny = az * bx - ax * bz;
        float nz = ax * by - ay * bx;

        addNormal(normals, i1, nx, ny, nz);
        addNormal(normals, i2, nx, ny, nz);
        addNormal(normals, i3, nx, ny, nz);
    }

    private static void addNormal(float[] normals, int index, float nx, float ny, float nz) {
        normals[index * 3] += nx;
        normals[index * 3 + 1] += ny;
        normals[index * 3 + 2] += nz;
    }

    private static void normalizeNormals(float[] normals) {
        for (int i = 0; i < normals.length / 3; i++) {
            float nx = normals[i * 3];
            float ny = normals[i * 3 + 1];
            float nz = normals[i * 3 + 2];
            float inverseLength = inverseLength(nx, ny, nz);
            normals[i * 3] = nx * inverseLength;
            normals[i * 3 + 1] = ny * inverseLength;
            normals[i * 3 + 2] = nz * inverseLength;
        }
    }

    private static float[] perpendicular(float x, float y, float z) {
        float px;
        float py;
        float pz;
        if (Math.abs(y) < 0.9f) {
            px = z;
            py = 0.0f;
            pz = -x;
        } else {
            px = 0.0f;
            py = -z;
            pz = y;
        }
        float inverseLength = inverseLength(px, py, pz);
        return new float[]{px * inverseLength, py * inverseLength, pz * inverseLength};
    }

    private static float inverseLength(float x, float y, float z) {
        float lengthSquared = x * x + y * y + z * z;
        if (lengthSquared > 1.0e-12f) {
            return 1.0f / (float) Math.sqrt(lengthSquared);
        } else {
            return 1.0f;
        }
    }

}
