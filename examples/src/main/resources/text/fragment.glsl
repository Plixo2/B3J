#version 430 core

uniform sampler2D u_texture;

in vec2 v_uv;
in vec4 v_color;

out vec4 fragColor;

void main()
{
    vec4 sampleColor = texture(u_texture, v_uv);
    float alpha = sampleColor.a * v_color.a;
    if (alpha <= 0.01) {
        discard;
    }
    fragColor = vec4(v_color.rgb, alpha);

}
