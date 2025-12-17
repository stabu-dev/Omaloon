#define HIGHP
#define NSCALE 170.0 / 2.0
#define DSCALE 130.0 / 2.0

uniform sampler2D u_texture;
uniform sampler2D u_noise;
uniform vec2 u_campos;
uniform vec2 u_resolution;
uniform float u_time;
uniform float u_seed;

varying vec2 v_texCoords;

const float mth = 7.0;
const float brightnessFactor = 0.94;

vec2 hash2(vec2 p) {
    p = vec2(dot(p, vec2(127.1, 311.7)), dot(p, vec2(269.5, 183.3)));
    return fract(sin(p) * 43758.5453);
}

/*
//TODO: create an ideal visuals, once figuring out how it should look
vec4 voronoi(vec2 x) {
    vec2 n = floor(x);
    vec2 f = fract(x);

    float md = -100.0;
    vec2 finalID = vec2(0.0);
    float ma = 0.0;

    float seedHash = fract(sin(u_seed) * 43758.5453);
    float seedHash2 = fract(sin(u_seed + 1.34) * 12345.67);
    float seedHash3 = fract(sin(u_seed + 42.1) * 7891.23);

    float smallProb = 0.5 + 0.35 * seedHash;
    float smallBase = 0.15 + 0.10 * seedHash2;
    float largeBase = 0.40 + 0.25 * seedHash3;

    float t = u_time / 40000.0;

    for (int j = -3; j <= 3; j++) {
        for (int i = -3; i <= 3; i++) {
            vec2 g = vec2(float(i), float(j));
            vec2 id = n + g;

            float cellRand = hash2(id).x;
            float phase = t + cellRand * 20.0;
            float activeRand = hash2(id + floor(phase)).y;

            if (activeRand <= 0.80) continue;

            float hexOffset = (abs(mod(id.y, 2.0)) >= 1.0) ? 0.5 : 0.0;
            vec2 o = hash2(id + vec2(u_seed, u_seed * 0.5));

            vec2 drift = vec2(
                sin(u_time * 0.0002 + o.x * 10.0 + id.y * 0.5),
                cos(u_time * 0.0003 + o.y * 10.0 + id.x * 0.5)
            ) * 0.35;

            vec2 centerPos = g + vec2(hexOffset, 0.0) + (o - 0.5) * 0.35 + drift;
            vec2 p = f - centerPos;

            if (dot(p, p) > 9.0) continue;

            vec2 randVec = hash2(id + vec2(u_seed, 0.0));
            float N = floor(mix(5.0, 9.0, randVec.x));
            float rot = randVec.y * 6.28;

            float typeRand = hash2(id + vec2(u_seed, 100.0)).x;
            bool isSmallPoly = typeRand < smallProb;

            float baseSize = isSmallPoly ? smallBase : largeBase;
            float size = baseSize + 0.08 * hash2(id).x;

            float a = atan(p.x, p.y) + rot;
            float r = 6.28318 / N;

            float distToEdge = cos(floor(0.5 + a / r) * r - a) * length(p);
            float currentMd = size - distToEdge;

            if (currentMd > md) {
                md = currentMd;
                finalID = id;
                float sector = floor(0.5 + a / r);
                ma = sector * r - rot;
            }
        }
    }
    return vec4(md, finalID, ma);
}
*/

void main() {
    vec2 c = v_texCoords.xy;
    vec2 coords = (c * u_resolution) + u_campos;

    float atime = u_time / 8000.0;
    float wave = abs(sin(coords.x / 22.0 + coords.y / 5.0) + 0.2 * sin(0.5 * coords.x) + 0.2 * sin(coords.y * 0.8)) / 5.0;

    vec2 noiseOffset = vec2(atime) * vec2(-0.3, 0.7) + vec2(sin(atime * 12.0 + coords.y * 0.006) / 10.0, cos(atime * 8.0 + coords.x * 0.008) / 2.0);
    float rawNoise = texture2D(u_noise, (coords) / DSCALE + noiseOffset).r;

    float baseNoise = wave + smoothstep(0.0, 1.0, rawNoise);
    float noise = abs(baseNoise - 0.6) * 7.0 + 0.23;

    float btime = u_time / 1000.0;
    vec2 distortOffset = (vec2(
        texture2D(u_noise, (coords) / NSCALE + vec2(btime) * vec2(-0.3, 0.3)).r,
        texture2D(u_noise, (coords) / NSCALE + vec2(btime * 1.1) * vec2(0.3, -0.3)).r
    ) - vec2(0.5)) * 7.0 / u_resolution;

    c += distortOffset;

    vec4 color = texture2D(u_texture, c);
    vec4 orig = color;

    /*
    vec2 vCoords = coords / 50.0;
    vec4 v = voronoi(vCoords);

    float edgeDist = v.x;
    vec2 cellID = v.yz;
    float edgeAngle = v.w;

    float t = u_time / 40000.0;
    float cellRand = hash2(cellID).x;
    float phase = t + cellRand * 20.0;
    float cyclePos = fract(phase);
    float activeRand = hash2(cellID + floor(phase)).y;

    float sizeType = hash2(cellID + vec2(u_seed, 100.0)).x;
    bool isSmall = sizeType < 0.75;

    if (activeRand > 0.80 && edgeDist > -0.05) {
        float fillLevel = 0.0;

        if (cyclePos < 0.1) {
            fillLevel = smoothstep(0.0, 1.0, cyclePos / 0.1);
        } else if (cyclePos < 0.9) {
            fillLevel = 1.0;
        } else {
            fillLevel = 1.0 - smoothstep(0.0, 1.0, (cyclePos - 0.9) / 0.1);
        }

        if (fillLevel > 0.01) {
            float gradient = sin(coords.x * 0.05) * cos(coords.y * 0.05);
            float localThreshold = 1.0 - fillLevel;
            float erosion = isSmall ? 0.55 : 0.0;
            float solidMask = smoothstep(localThreshold + erosion - 0.15, localThreshold + erosion + 0.15, rawNoise + gradient * 0.2);
            float borderMask = smoothstep(-0.01, 0.01, edgeDist);
            float finalMask = solidMask * borderMask;

            float bevelWidth = 0.15;
            float bevelMask = smoothstep(bevelWidth, 0.0, edgeDist);

            vec2 polyNormal = vec2(cos(edgeAngle), sin(edgeAngle));
            vec2 lightDir = normalize(vec2(1.0, 1.0));
            float lighting = dot(polyNormal, lightDir);
            float lightMix = smoothstep(-0.02, 0.02, lighting);

            vec3 colBody = vec3(0.4, 0.6, 0.8);
            vec3 colDark = vec3(0.05, 0.1, 0.2);
            vec3 colLight = vec3(0.9, 0.95, 1.0);

            vec3 edgeCol = mix(colDark, colLight, lightMix);
            vec3 polyCol = mix(colBody, edgeCol, bevelMask);

            color.rgb = mix(color.rgb, polyCol, finalMask * 0.6);

            noise = mix(noise, 0.0, finalMask);
        }
    }
    */

    if (noise > 0.85) {
        color *= brightnessFactor;
    }

    if (orig.g < mth) {
        color *= brightnessFactor;
    }

    gl_FragColor = color;
}