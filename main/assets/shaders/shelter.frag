#define HIGHP
#define ALPHA 0.28
#define STEPS 32.0
#define HEIGHT_SCL 0.06

uniform sampler2D u_texture;
uniform vec2 u_invsize;
uniform vec2 u_texsize;
uniform vec2 u_offset;
uniform float u_time;

varying vec2 v_texCoords;

void main() {
    vec2 uv = v_texCoords.xy;
    vec2 rel = (uv - 0.5) * HEIGHT_SCL;
    vec3 strokeColor = vec3(0.5, 0.9, 0.5);
    
    float acc = 0.0;
    float prevA = step(0.1, texture2D(u_texture, uv).a);
    
    for (float i = 1.0; i <= STEPS; i += 1.0) {
        float t = i / STEPS;
        vec2 p = uv - rel * t;
        float currA = step(0.1, texture2D(u_texture, p).a);
        
        float edge = abs(currA - prevA);
        float fade = pow(1.0 - t, 1.2);
        
        vec2 coords = (p * u_texsize) + u_offset;
        float react = 1.0 + 0.25 * (sin(coords.x * 0.04 + coords.y * 0.04 + u_time * 0.1) + sin(coords.x * 0.07 - coords.y * 0.03 + u_time * 0.07));
        float pulse = 1.0 + 0.2 * sin(t * 8.0 - u_time * 0.15);
        
        acc += edge * fade * pulse * react;
        prevA = currA;
    }

    gl_FragColor = vec4(strokeColor, acc * ALPHA);
}
