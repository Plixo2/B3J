#version 430

layout (location = 0) out vec4 v_color;


uniform mat4 u_projView;
uniform float u_width;

struct LineData
{
    vec4 start;
    vec4 end;
    vec4 color;
};

layout (std430, binding = 0) readonly buffer Instances {
    LineData data[];
};

void main() {
    LineData line = data[gl_InstanceID];

    vec4 clipStart = u_projView * vec4(line.start.xyz, 1.0);
    vec4 clipEnd   = u_projView * vec4(line.end.xyz, 1.0);

    vec2 ndcStart = clipStart.xy / clipStart.w;
    vec2 ndcEnd   = clipEnd.xy / clipEnd.w;

    vec2 dir = normalize(ndcEnd - ndcStart);
    vec2 normal = vec2(-dir.y, dir.x) * (u_width * 0.01);

    vec4 offset = vec4(0.0);
    if (gl_VertexID == 0) offset = vec4( normal, 0.0, 0.0) * clipStart.w;
    if (gl_VertexID == 1) offset = vec4(-normal, 0.0, 0.0) * clipStart.w;
    if (gl_VertexID == 2) offset = vec4( normal, 0.0, 0.0) * clipEnd.w;
    if (gl_VertexID == 3) offset = vec4(-normal, 0.0, 0.0) * clipEnd.w;

    gl_Position = (gl_VertexID < 2) ? (clipStart + offset) : (clipEnd + offset);
    v_color = line.color;

   

}
