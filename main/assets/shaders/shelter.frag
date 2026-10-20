#define HIGHP

uniform vec4 u_maskView;
uniform vec2 u_fieldTime;

varying vec2 v_texCoords;

#if defined(MASK)

uniform vec4 u_fieldPos[MAX_FIELDS];
uniform vec4 u_parts[MAX_FIELDS * 3];
uniform vec4 u_star[MAX_FIELDS * 2];
uniform vec4 u_fieldMotion[MAX_FIELDS];
uniform vec4 u_fieldBounds;

const float seam = 3.0;

float smin(float a, float b, float k){
    float h = clamp(0.5 + 0.5 * (b - a) / k, 0.0, 1.0);
    return mix(b, a, h) - k * h * (1.0 - h);
}

float smax(float a, float b, float k){
    return -smin(-a, -b, k);
}

vec2 rotate(vec2 v, float a){
    float c = cos(a), s = sin(a);
    return vec2(v.x * c - v.y * s, v.x * s + v.y * c);
}

float sdWedge(vec2 p, vec2 c){
    p.x = abs(p.x);
    float m = length(p - c * max(dot(p, c), 0.0));
    return m * sign(c.y * p.x - c.x * p.y);
}

float sdPart(vec2 rel, vec4 part, float lag, float rounding){
    if(part.w <= 0.0) return 1e5;
    vec2 dir = rotate(part.xy, lag);
    vec2 local = vec2(rel.x * dir.y - rel.y * dir.x, dot(rel, dir));
    float k = max(min(rounding, min(part.w * 0.5, part.w * sin(min(part.z, 1.5)))), 0.01);
    return smax(length(rel) - part.w, sdWedge(local, vec2(sin(part.z), cos(part.z))), k);
}

float sdField(vec2 rel, float core, vec4 arc, vec4 left, vec4 right, float lag, float rounding){
    float d = length(rel) - core;
    d = smin(d, sdPart(rel, arc, lag, rounding), seam);
    d = smin(d, sdPart(rel, left, lag, rounding), seam);
    d = smin(d, sdPart(rel, right, lag, rounding), seam);
    return d;
}

float side(vec2 p, float angle, float a, float b){
    vec2 u = vec2(cos(angle), sin(angle));
    return length(p - clamp(dot(p, u), min(a, b), max(a, b)) * u);
}

float sdStar(vec2 rel, float rho, vec4 star, vec4 radii, float lag){
    float a = mod(atan(rel.y, rel.x) - lag - star.x, 6.2831853);
    float r = a < star.y ? radii.x : a < star.z ? radii.y : a < star.w ? radii.z : radii.w;
    float start = star.x + lag;
    float d = min(abs(rho - r), side(rel, start, radii.w, radii.x));
    d = min(d, side(rel, start + star.y, radii.x, radii.y));
    d = min(d, side(rel, start + star.z, radii.y, radii.z));
    d = min(d, side(rel, start + star.w, radii.z, radii.w));
    return rho < r ? -d : d;
}

float membrane(vec2 world, out vec2 info){
    float t = u_fieldTime.x, k = u_fieldTime.y, d = 1e5;
    info = vec2(1.0, 16.0);
    if(world.x < u_fieldBounds.x || world.y < u_fieldBounds.y || world.x > u_fieldBounds.z || world.y > u_fieldBounds.w) return d;

    for(int i = 0; i < MAX_FIELDS; i++){
        vec4 pos = u_fieldPos[i];
        if(pos.z < 0.0) break;

        vec2 rel = world - pos.xy;
        float rho = length(rel);
        vec4 motion = u_fieldMotion[i];
        vec4 star = u_star[i * 2], radii = u_star[i * 2 + 1];
        float reach = max(max(radii.x, radii.y), max(radii.z, radii.w));
        float fd;

        if(rho > reach + k + 4.0){
            fd = rho - reach;
        }else{
            vec4 arc = u_parts[i * 3], left = u_parts[i * 3 + 1], right = u_parts[i * 3 + 2];
            fd = sdField(rel, pos.z, arc, left, right, 0.0, pos.w);
            float exact = sdStar(rel, rho, star, radii, 0.0);
            if(abs(motion.x) > 0.001){
                fd = smax(fd, sdField(rel, pos.z, arc, left, right, -motion.x, pos.w), seam);
                exact = max(exact, sdStar(rel, rho, star, radii, -motion.x));
            }
            float slack = pos.w * 0.42 + 0.1;
            fd = smin(fd, exact + slack, slack);

            float a = atan(rel.y, rel.x);
            fd += (0.4 * (0.5 + 0.5 * sin(a * 3.0 + t * 0.03)) + motion.y * (0.5 + 0.5 * sin(a * 5.0 - t * 0.16)) * (0.5 + 0.5 * sin(t * 0.11 + a * 2.0)))
                * smoothstep(4.0, 16.0, rho);
        }

        float h = clamp(0.5 + 0.5 * (fd - d) / k, 0.0, 1.0);
        d = mix(fd, d, h) - k * h * (1.0 - h);
        info = mix(motion.wz, info, h);
    }
    return d;
}

void main(){
    vec2 info;
    float d = membrane(u_maskView.xy + v_texCoords * u_maskView.zw, info);
    float v = clamp(d / (MASK_RANGE * 2.0) + 0.5, 0.0, 0.9999) * 255.0;
    gl_FragColor = vec4(floor(v) / 255.0, fract(v), clamp(info.y / 128.0, 0.0, 1.0), clamp(info.x, 0.0, 1.0));
}

#else

uniform sampler2D u_texture;
uniform sampler2D u_fieldMask;
uniform vec2 u_maskTexel;
uniform float u_pixel;

varying vec4 v_color;
varying vec4 v_mix_color;
varying vec2 v_world;

float maskDistance(vec4 texel){
    return ((texel.r * 255.0 + texel.g) / 255.0 - 0.5) * MASK_RANGE * 2.0;
}

float field(vec2 world, out vec2 info){
    vec2 p = (world - u_maskView.xy) / u_maskView.zw / u_maskTexel - 0.5;
    vec2 f = fract(p);
    vec2 base = (floor(p) + 0.5) * u_maskTexel;
    vec4 a = texture2D(u_fieldMask, base), b = texture2D(u_fieldMask, base + vec2(u_maskTexel.x, 0.0));
    vec4 c = texture2D(u_fieldMask, base + vec2(0.0, u_maskTexel.y)), e = texture2D(u_fieldMask, base + u_maskTexel);
    vec4 mixed = mix(mix(a, b, f.x), mix(c, e, f.x), f.y);
    info = vec2(mixed.a, mixed.b * 128.0);
    return mix(mix(maskDistance(a), maskDistance(b), f.x), mix(maskDistance(c), maskDistance(e), f.x), f.y);
}

float domeHeight(float depth, float height){
    float r = height - clamp(depth, 0.0, height);
    return sqrt(max(height * height - r * r, 0.0));
}

float domeTilt(float depth, float height){
    float r = height - clamp(depth, 0.0, height), z = max(domeHeight(depth, height), 0.5);
    return 1.0 - inversesqrt(1.0 + r * r / (z * z));
}

#if defined(FADE)

uniform float u_minAlpha;

void main(){
    vec4 c = texture2D(u_texture, v_texCoords);
    vec4 col = v_color * mix(c, vec4(v_mix_color.rgb, c.a), v_mix_color.a);

    vec2 info;
    float d = field(v_world, info);
    float cover = (1.0 - smoothstep(-6.0, 2.0, d)) * (0.6 + 0.4 * smoothstep(0.0, 0.35, info.x));

    col.a *= mix(1.0, u_minAlpha, cover);
    gl_FragColor = col;
}

#elif defined(RING)

void main(){
    float age = v_color.r, strength = v_color.g * 3.0, reach = v_color.b * 128.0;
    vec2 local = (v_texCoords * 2.0 - 1.0) * (reach + 4.0);
    float straight = length(local);
    float radius = reach * (1.0 - (1.0 - age) * (1.0 - age));
    float width = 1.5 + 3.5 * age;

    float rh = max(v_mix_color.b * 255.0, 1.0);
    vec2 hit = -normalize(v_mix_color.rg * 2.0 - 1.0) * rh, rel = hit + local;
    float rp = max(length(rel), 0.0001);
    vec2 radial = rel / rp;
    vec2 arc = vec2(atan(hit.x * rel.y - hit.y * rel.x, dot(hit, rel)) * rh, rp - rh);
    float arcDist = length(arc);
    float shortest = min(straight, arcDist);
    if(shortest > radius + width * 3.0 && shortest > 10.0) discard;

    vec2 info, there;
    float d = field(v_world, info);
    float inside = 1.0 - smoothstep(-u_pixel, u_pixel, d);
    if(inside <= 0.001) discard;

    float centerDepth = -field(v_world - local, there);
    float follow = smoothstep(0.45, 0.85, rh / (rh + max(centerDepth, 0.0)));
    vec2 along = (vec2(-radial.y, radial.x) * arc.x + radial * arc.y) / max(arcDist, 0.0001);
    vec2 outward = normalize(mix(local / max(straight, 0.0001), along, follow) + 0.00001);

    float rise = domeHeight(-d, info.y) - domeHeight(centerDepth, there.y);
    float dist = length(vec2(mix(straight, arcDist, follow), rise));

    float x = (dist - radius) / width;
    float fade = 1.0 - age;
    float ring = exp(-x * x) * fade * sqrt(fade);
    float dent = (dist / 2.5) * exp(1.0 - dist / 2.5) * fade * fade * fade * fade * 0.6;

    float edgeOn = domeTilt(-d, info.y);
    edgeOn *= edgeOn;
    float amount = (ring * (0.4 + 0.6 * edgeOn) + dent) * min(strength, 2.0) * 1.6 * inside;

    vec2 shift = outward * amount / RIPPLE_RANGE;
    gl_FragColor = vec4(max(shift.x, 0.0), max(-shift.x, 0.0), max(shift.y, 0.0), max(-shift.y, 0.0));
}

#else

uniform sampler2D u_screen;
uniform sampler2D u_ripples;
uniform sampler2D u_dark;
uniform vec4 u_viewport;
uniform vec4 u_color;
uniform vec3 u_storm;
uniform vec2 u_darkSize;
uniform float u_darkAbsorb;
uniform float u_intensity;

const float lens = 3.0;
const float pillarSpacing = 14.0;
const float pillarLength = 11.0;
const float pillarGlow = 0.45;

float hash(vec2 p){
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

float noise(vec2 p){
    vec2 i = floor(p), f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash(i), hash(i + vec2(1.0, 0.0)), f.x), mix(hash(i + vec2(0.0, 1.0)), hash(i + 1.0), f.x), f.y);
}

void main(){
    vec2 screen = gl_FragCoord.xy - u_viewport.xy;
    vec4 shifted = texture2D(u_ripples, screen / u_viewport.zw);
    vec2 ripple = vec2(shifted.r - shifted.g, shifted.b - shifted.a) * RIPPLE_RANGE * u_intensity;

    vec2 info, unused;
    float d = field(v_world, info);
    float inside = 1.0 - smoothstep(-u_pixel, u_pixel, d);
    if(inside <= 0.001) discard;

    float depth = max(-d, 0.0);
    float tilt = domeTilt(depth, info.y);
    tilt *= tilt * smoothstep(0.0, 3.0, depth);

    float t = u_fieldTime.x;
    vec2 bend = ripple;
    float lean = 0.0;
    if(tilt > 0.0){
        vec2 grad = vec2(field(v_world + vec2(4.0, 0.0), unused), field(v_world + vec2(0.0, 4.0), unused)) - d;
        vec2 normal = grad / max(length(grad), 0.0001) * smoothstep(0.55, 0.9, length(grad) / 4.0);
        bend -= normal * lens * u_intensity * tilt;
        lean = dot(normal, -u_storm.xy) * u_storm.z;
    }
    vec3 scene = texture2D(u_screen, (screen + bend / u_pixel) / u_viewport.zw).rgb;

    float pillar = 0.0;
    if(depth < pillarLength * 3.0){
        float rise = smoothstep(0.1, 0.8, (field(v_world - vec2(0.0, 6.0), unused) - d) / 6.0);
        float n = smoothstep(0.45, 0.85, noise(vec2(v_world.x / pillarSpacing + t * 0.002, t * 0.0015)));
        pillar = n * n * rise * exp(-depth / pillarLength) * smoothstep(0.0, 3.0, depth);
    }

    float drift = 0.85 + 0.15 * sin(dot(v_world, vec2(0.031, 0.022)) + t * 0.02) * sin(dot(v_world, vec2(-0.017, 0.029)) - t * 0.015);
    float flicker = 1.0 - (1.0 - smoothstep(0.0, 0.35, info.x)) * (0.3 + 0.3 * sin(t * 0.7 + v_world.x * 0.05 + v_world.y * 0.04));
    float wave = min(length(ripple), 2.0);

    float lit = mix(1.0, texture2D(u_dark, (v_world + 4.0) / u_darkSize).r, u_darkAbsorb);
    float amount = (0.075 + tilt * (0.2 + 0.32 * drift) * (1.0 + 0.65 * lean) + pillar * pillarGlow * (1.0 + 0.5 * lean) + 0.7 * wave) * flicker * u_intensity * lit;
    gl_FragColor = vec4(mix(scene, u_color.rgb, min(amount, 0.75)), inside * u_color.a);
}

#endif

#endif