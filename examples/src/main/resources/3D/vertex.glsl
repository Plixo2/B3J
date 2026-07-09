#version 460 core

layout (location = 0) in vec3 a_position;
layout (location = 1) in vec3 a_normal;

uniform mat4 u_projView;

struct DrawData {
    mat4 model;
    mat4 normal;
    vec4 color;
};

layout (std430, binding = 0) readonly buffer DrawDataBuffer {
    DrawData draws[];
};

out vec3 v_normal;
out vec3 v_worldPos;
out vec4 v_color;

void main()
{
    DrawData draw = draws[gl_DrawID];
    vec4 worldPos = draw.model * vec4(a_position, 1.0);
    gl_Position = u_projView * worldPos;
    v_worldPos = worldPos.xyz;
    v_normal = normalize(mat3(draw.normal) * a_normal);
    v_color = draw.color;
}
