#version 430 core

uniform mat4 u_projection;
uniform vec3 u_right;
uniform vec3 u_up;

struct GlyphData {
    vec4 color;
    vec4 uv;
    vec4 position;
    vec4 size;
};

layout (std430, binding = 0) readonly buffer GlyphDataBuffer {
    GlyphData draws[];
};

out vec2 v_uv;
out vec4 v_color;

void main()
{
    GlyphData glyph = draws[gl_InstanceID];
    vec2 quad = vec2(gl_VertexID & 1, gl_VertexID >> 1);
    vec2 offset = glyph.size.zw + quad * glyph.size.xy;
    vec3 position = glyph.position.xyz + u_right * offset.x + u_up * offset.y;

    vec2 uvMin = vec2(glyph.uv.x, 1.0 - glyph.uv.w);
    vec2 uvMax = vec2(glyph.uv.z, 1.0 - glyph.uv.y);
    v_uv = mix(uvMin, uvMax, quad);
    v_color = glyph.color;
    gl_Position = u_projection * vec4(position, 1.0);
}
