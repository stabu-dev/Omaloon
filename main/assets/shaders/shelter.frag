#define HIGHP
#define ALPHA 0.28
#define STEPS 32
#define HEIGHT_SCL 60.0

uniform sampler2D u_texture;
uniform vec2 u_invsize;
uniform vec2 u_texsize;
uniform vec2 u_offset;
uniform float u_time;

varying vec2 v_texCoords;

float sampleAlpha(vec2 uv) {
    return texture2D(u_texture, uv).a;
}

float sampleEdge(vec2 uv) {
    return step(0.1, sampleAlpha(uv));
}

void main() {
    vec2 uv = v_texCoords;
    vec2 centerOffset = uv - 0.5;
    vec2 rel = centerOffset * (HEIGHT_SCL * u_invsize.x);
    vec3 strokeColor = vec3(0.5, 0.9, 0.5);
    
    vec2 world = (uv * u_texsize) + u_offset;
    float waves = sin(world.x * 0.04 + world.y * 0.04 + u_time * 0.06) + 
                  sin(world.x * 0.07 - world.y * 0.03 + u_time * 0.05);
    float react = 1.0 + 0.12 * waves;

    float acc = 0.0;
    float prevA = sampleEdge(uv);
    
    for (int i = 1; i <= STEPS; i++) {
        float t = float(i) / float(STEPS);
        float currA = sampleEdge(uv - rel * t);
        
        float fade = pow(1.0 - t, 1.2) * smoothstep(0.0, 0.1, t);
        float pulse = 1.0 + 0.2 * sin(t * 8.0 - u_time * 0.15);
        
        acc += abs(currA - prevA) * fade * pulse;
        prevA = currA;
    }

    gl_FragColor = vec4(strokeColor, acc * ALPHA * react);
}
