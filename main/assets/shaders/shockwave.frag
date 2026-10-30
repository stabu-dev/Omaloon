#define MAX_WAVES 8
#ifndef SHARED_NOISE
#define SHARED_NOISE false
#endif

uniform sampler2D u_texture;
uniform vec4 u_view;
uniform vec4 u_waves[MAX_WAVES];
uniform vec4 u_states[MAX_WAVES];
uniform int u_waves_count;
uniform float u_time;

varying vec2 v_texCoords;

const float trail = 3.0;
const float dustPush = 22.0;
const float dustDrag = 14.0;
const float dustInner = 0.4;

float hash(vec2 p){
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

float noise(vec2 p){
    vec2 i = floor(p), f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash(i), hash(i + vec2(1.0, 0.0)), f.x), mix(hash(i + vec2(0.0, 1.0)), hash(i + 1.0), f.x), f.y);
}

float fbm(vec2 p){
    return noise(p) * 0.55 + noise(p * 2.1 + 17.0) * 0.3 + noise(p * 4.3 + 41.0) * 0.15;
}

vec2 toTex(vec2 world){
    return (world - u_view.xy) / u_view.zw;
}

void main(){
    vec2 world = u_view.xy + v_texCoords * u_view.zw;
    vec2 offset = vec2(0.0);
    float glow = 0.0, dust = 0.0;
    vec3 dustColor = vec3(0.0);
    bool noiseReady = false;
    vec2 wobble = vec2(0.0);

    for(int i = 0; i < MAX_WAVES; i++){
        if(i >= u_waves_count) break;

        vec4 wave = u_waves[i], state = u_states[i];
        vec2 diff = world - wave.xy;
        float dst = length(diff);
        if(dst <= 0.0) continue;
        vec2 dir = diff / dst;

        float fin = clamp(state.x / wave.w, 0.0, 1.0), inv = 1.0 - fin;
        float radius = wave.z * (1.0 - inv * inv * inv);

        if(state.x < wave.w){
            float fade = 1.0 - fin * fin;
            float width = wave.z * 0.02 + radius * 0.08;
            float k = (dst - radius) / width;

            if(abs(k) < 1.0){
                offset += dir * state.y * fade * sin(k * 3.14159) * (1.0 - abs(k));
            }

            float thin = 1.0 - radius / wave.z;
            float opacity = state.z * pow(thin, 0.8) * 1.4;
            float behind = -k;
            if(behind > -0.2 && behind < trail){
                float edge = smoothstep(-0.2, 0.0, behind);
                float fall = 1.0 - smoothstep(0.0, trail, behind);
                float shimmer = 0.7 + 0.3 * noise(world / (wave.z * 0.03) + vec2(u_time * 0.02, -u_time * 0.015));
                glow = max(glow, edge * fall * fall * shimmer * opacity);
            }

            if(dst < radius){
                float sphere = sqrt(max(1.0 - (dst / radius) * (dst / radius), 0.0));
                offset += dir * dst * 0.3 * sphere * thin * thin * thin;
                glow = max(glow, (0.55 + 0.45 * sphere) * pow(thin, 1.5) * state.z * 1.8);
            }
        }

        float rel = dst / wave.z;
        if(dst < radius && rel < 1.0){
            float haze = smoothstep(0.0, wave.z * 0.1, radius - dst) * state.w;
            if(rel < dustInner){
                if(!SHARED_NOISE || !noiseReady){
                    wobble = vec2(noise(world / (wave.z * 0.05) + vec2(u_time * 0.03, 0.0)), noise(world / (wave.z * 0.05) + vec2(31.0, u_time * 0.03))) - 0.5;
                    noiseReady = true;
                }
                offset += wobble * state.y * 0.5 * haze * (1.0 - smoothstep(dustInner * 0.6, dustInner, rel));
            }
            glow = max(glow, state.z * 0.12 * haze * (1.0 - rel));
        }

        if(dst < radius && rel < 1.0 && rel > dustInner * 0.8){
            float age = state.x - (1.0 - pow(1.0 - rel, 1.0 / 3.0)) * wave.w;
            if(age > 0.0){
                float reach = 1.0 - rel;
                vec2 source = world - dir * dustPush * (0.4 + 0.6 * reach) * (1.0 - exp(-age / dustDrag));
                float clumps = smoothstep(0.38, 0.8, fbm(source / (wave.z * 0.024) + vec2(age * 0.004)));
                float amount = clumps * 0.7 * pow(reach, 0.8) * smoothstep(dustInner * 0.8, dustInner, rel);
                amount *= smoothstep(0.0, 4.0, age) * state.w;

                if(amount > dust){
                    dust = amount;
                    dustColor = mix(texture2D(u_texture, toTex(source)).rgb, vec3(0.86, 0.82, 0.75), 0.35);
                }
            }
        }
    }

    vec4 scene = texture2D(u_texture, toTex(world - offset));
    vec3 color = mix(scene.rgb, vec3(1.0), min(glow, 1.0));
    color = mix(color, dustColor, min(dust, 0.95));
    gl_FragColor = vec4(color, max(scene.a, min(dust, 0.95)));
}