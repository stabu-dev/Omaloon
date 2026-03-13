#define HIGHP
#define ALPHA 0.48
#define RADIUS 2.0
#define SAMPLES 32.0

uniform sampler2D u_texture;
uniform vec2 u_invsize;

varying vec2 v_texCoords;

void main() {
    vec2 uv = v_texCoords.xy;
    float coreAlpha = texture2D(u_texture, uv).a;
    float maxAlpha = 0.0;

    for (float r = 1.0; r <= RADIUS; r += 1.0) {
        for (float i = 0.0; i < SAMPLES; i += 1.0) {
            float angle = i * 0.196349;
            vec2 offset = vec2(cos(angle), sin(angle)) * r * u_invsize;
            maxAlpha = max(maxAlpha, texture2D(u_texture, uv + offset).a);
        }
    }

    float outline = clamp(maxAlpha - coreAlpha, 0.0, 1.0);
    outline = step(0.5, outline);

    vec3 strokeColor = vec3(0.5, 0.9, 0.5);
    gl_FragColor = vec4(strokeColor, outline * ALPHA);
}