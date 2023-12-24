#define HIGHP

#define NSCALE 170.0 / 2.0
#define DSCALE 130.0 / 2.0

uniform sampler2D u_texture;
uniform sampler2D u_noise;

uniform vec2 u_campos;
uniform vec2 u_resolution;
uniform float u_time;

varying vec2 v_texCoords;

const float mth = 7.0;
const float brightnessFactor = 0.94;

void main() {
    vec2 c = v_texCoords.xy;
    vec2 coords = (c * u_resolution) + u_campos;

    vec4 orig = texture2D(u_texture, c);

    float atime = u_time / 8000.0;
    float wave = abs(sin(coords.x / 22.0 + coords.y / 5.0) + 0.2 * sin(0.5 * coords.x) + 0.2 * sin(coords.y * 0.8)) / 5.0;
    float noise = wave + smoothstep(0.0, 1.0, texture2D(u_noise, (coords) / DSCALE + vec2(atime) * vec2(-0.3, 0.7) + vec2(sin(atime * 12.0 + coords.y * 0.006) / 10.0, cos(atime * 8.0 + coords.x * 0.008) / 2.0)).r);
    noise = abs(noise - 0.6) * 7.0 + 0.23;

    float btime = u_time / 1000.0;

    c += (vec2(
    texture2D(u_noise, (coords) / NSCALE + vec2(btime) * vec2(-0.3, 0.3)).r,
    texture2D(u_noise, (coords) / NSCALE + vec2(btime * 1.1) * vec2(0.3, -0.3)).r
    ) - vec2(0.5)) * 7.0 / u_resolution;

    vec4 color = texture2D(u_texture, c);

    if(noise > 0.85){
        if(color.g > mth - 0.1){
            color *= brightnessFactor;
        } else {
            color *= brightnessFactor;
        }
    }

    if(orig.g < mth){
        color *= brightnessFactor;
    }

    gl_FragColor = color;
}
