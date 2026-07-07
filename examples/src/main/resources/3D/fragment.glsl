#version 330 core

in vec3 v_normal;
in vec3 v_worldPos;

uniform vec4 u_color;
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
const vec3 lightDir = normalize(vec3(0.5, 1.0, 0.75));

void main()
{
    vec3 normal = normalize(v_normal);
    vec3 viewDir = normalize(u_cameraPos - v_worldPos);
    vec3 halfDir = normalize(lightDir + viewDir);
    vec3 baseColor = u_color.rgb;

    float lambert = max(dot(normal, lightDir), 0.0);
    float diffuseLight = lambert * diffuse;
    float softLight = (dot(normal, lightDir) * 0.5 + 0.5) * diffuse;
    float glossySpec = pow(max(dot(normal, halfDir), 0.0), glossyShininess) * glossyStrength;
    float metallicSpec = pow(max(dot(normal, halfDir), 0.0), metallicShininess) * metallicStrength;

    int mode = int(round(u_color.a * 255));
    if (mode == MATTE) {
        fragColor = vec4(baseColor * (ambient + diffuseLight), 1.0);
//        fragColor = vec4(0.0, 0.0, 1.0, 1.0f);
    } else if (mode == SOFT) {
        fragColor = vec4(baseColor * (ambient + softLight), 1.0);
//        fragColor = vec4(0.0, 1.0, 1.0, 1.0f);
    } else if (mode == DEAD) {
        vec3 gray = vec3(dot(baseColor, vec3(0.299, 0.587, 0.114)));
        fragColor = vec4(gray * (0.15 + diffuseLight * 0.45), 1.0);
//        fragColor = vec4(1.0, 0.0, 1.0, 1.0f);
    } else if (mode == GLOSSY) {
        vec3 color = baseColor * (ambient + diffuseLight) + vec3(glossySpec);
        fragColor = vec4(color, 1.0);
//        fragColor = vec4(1.0, 1.0, 1.0, 1.0f);
    } else if (mode == METALLIC) {
        vec3 color = baseColor * (ambient * 0.5 + diffuseLight * 0.35) + baseColor * metallicSpec;
        fragColor = vec4(color, 1.0);
//        fragColor = vec4(1.0, 1.0, 0.0, 1.0f);
    } else {
        // DEFAULT or other
        fragColor = vec4(baseColor, 1.0);
//        fragColor = vec4(1.0, 0.0, 0.0, 1.0f);
    }



    //fragColor = u_color; // vec4(v_normal, 1.0);
}
