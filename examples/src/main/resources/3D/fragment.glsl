#version 430 core

in vec3 v_normal;
in vec3 v_localPos;
in vec3 v_worldPos;
in vec4 v_color;

uniform vec3 u_cameraPos;

out vec4 fragColor;

#define DEFAULT 0
#define MATTE 1
#define SOFT 2
#define DEAD 3
#define GLOSSY 4
#define METALLIC 5



const float ambient = 0.25;
const float diffuse = 1.0 - ambient;
const float glossyStrength = 0.45;
const float glossyShininess = 64.0;
const float metallicStrength = 0.75;
const float metallicShininess = 96.0;
const vec3 lightDir = normalize(vec3(0.6, 1.0, 0.85));

const float gridSize = 1.0;
const float lineWidth = 0.001;
const float lineSmoothness = 0.02;
const float gridAlbedo = 0.95;

vec3 applyLocalGrid(vec3 color)
{
    vec3 gridPos = v_localPos / gridSize;
    vec3 cell = abs(fract(gridPos - 0.5) - 0.5);
    vec3 width = vec3(lineWidth);
    vec3 smoothWidth = vec3(lineSmoothness);
    vec3 lines = 1.0 - smoothstep(width, width + smoothWidth, cell);
    float grid = max(max(lines.x, lines.y), lines.z);

    return mix(color, color * gridAlbedo, grid);
}

void main()
{

    vec3 normal = normalize(v_normal);
    vec3 viewDir = normalize(u_cameraPos - v_worldPos);
    vec3 halfDir = normalize(lightDir + viewDir);
    vec3 baseColor = v_color.rgb;

    float lambert = max(dot(normal, lightDir), 0.0);
    float diffuseLight = lambert * diffuse;
    float softLight = (dot(normal, lightDir) * 0.5 + 0.5) * diffuse;
    float glossySpec = pow(max(dot(normal, halfDir), 0.0), glossyShininess) * glossyStrength;
    float metallicSpec = pow(max(dot(normal, halfDir), 0.0), metallicShininess) * metallicStrength;

    int mode = int(round(v_color.a * 255));
    vec3 color;
    if (mode == MATTE) {
        color = baseColor * (ambient + diffuseLight);
    } else if (mode == SOFT) {
        color = baseColor * (ambient + softLight);
    } else if (mode == DEAD) {
        vec3 gray = vec3(dot(baseColor, vec3(0.299, 0.587, 0.114)));
        color = gray * (0.15 + diffuseLight * 0.45);
    } else if (mode == GLOSSY) {
        color = baseColor * (ambient + diffuseLight) + vec3(glossySpec);
    } else if (mode == METALLIC) {
        color = baseColor * (ambient * 0.5 + diffuseLight * 0.35) + baseColor * metallicSpec;
    } else {
        // DEFAULT or other
        color = baseColor;
    }

    fragColor = vec4(applyLocalGrid(color), 1.0);

}
