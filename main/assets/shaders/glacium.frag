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

vec2 hash2(vec2 p){
    p = vec2(dot(p, vec2(127.1, 311.7)), dot(p, vec2(269.5, 183.3)));
    return fract(sin(p) * 43758.5453);
}

// Returns x: distance to edge, yz: cell ID
vec3 voronoi(vec2 x){
    vec2 n = floor(x);
    vec2 f = fract(x);

    vec2 mg, mr;
    float md = 8.0;

    // Pass 1: Find closest point
    for(int j=-1; j<=1; j++){
        for(int i=-1; i<=1; i++){
            vec2 g = vec2(float(i), float(j));
            vec2 id = n + g;
            
            // Hexagonal Grid Logic
            float hexOffset = (mod(id.y, 2.0) == 1.0) ? 0.5 : 0.0;
            
            vec2 o = hash2(id);
            
            // Synchronized Drift
            // Use same timing base as fluid waves (atime * 12.0 approx)
            float driftTime = (u_time / 8000.0) * 12.0;
            
            // Coherent Spatial Drift aligned with wave rhythm
            vec2 drift = vec2(sin(driftTime + id.x * 0.5), cos(driftTime + id.y * 0.5)) * 0.02;
            
            // Low Jitter (0.25)
            vec2 pos = g + vec2(hexOffset, 0.0) + (o - 0.5) * 0.25 + drift;
            
            vec2 r = pos - f;
            float d = dot(r, r);

            if(d < md){
                md = d;
                mr = r;
                mg = g;
            }
        }
    }

    // Pass 2: Distance to borders
    md = 8.0;
    for(int j=-2; j<=2; j++){
        for(int i=-2; i<=2; i++){
            vec2 g = mg + vec2(float(i), float(j));
            vec2 id = n + g;
            
            float hexOffset = (mod(id.y, 2.0) == 1.0) ? 0.5 : 0.0;
            vec2 o = hash2(id);
            
            float driftTime = (u_time / 8000.0) * 12.0;
            vec2 drift = vec2(sin(driftTime + id.x * 0.5), cos(driftTime + id.y * 0.5)) * 0.02;
            
            vec2 pos = g + vec2(hexOffset, 0.0) + (o - 0.5) * 0.25 + drift;
            
            vec2 r = pos - f;

            if(dot(mr - r, mr - r) > 0.0001){
                md = min(md, dot(0.5 * (mr + r), normalize(r - mr)));
            }
        }
    }
    return vec3(md, n + mg);
}

void main() {
    vec2 c = v_texCoords.xy;
    vec2 coords = (c * u_resolution) + u_campos;

    vec4 orig = texture2D(u_texture, c);

    // Basic fluid noise
    float atime = u_time / 8000.0;
    float wave = abs(sin(coords.x / 22.0 + coords.y / 5.0) + 0.2 * sin(0.5 * coords.x) + 0.2 * sin(coords.y * 0.8)) / 5.0;
    float rawNoise = texture2D(u_noise, (coords) / DSCALE + vec2(atime) * vec2(-0.3, 0.7) + vec2(sin(atime * 12.0 + coords.y * 0.006) / 10.0, cos(atime * 8.0 + coords.x * 0.008) / 2.0)).r;
    float baseNoise = wave + smoothstep(0.0, 1.0, rawNoise);
    float noise = abs(baseNoise - 0.6) * 7.0 + 0.23;

    // Voronoi crystallization
    vec2 flowOffset = vec2(atime) * vec2(-0.3, 0.7) * 3.0; 
    vec2 waveDistort = vec2(wave * 0.15);
    vec2 vCoords = (coords / 50.0) + flowOffset + waveDistort; 
    
    vec3 v = voronoi(vCoords);
    float edgeDist = v.x; 
    vec2 cellID = v.yz;
    
    // Cycle control - Massive lifecycle duration
    float t = u_time / 16000.0; 
    float cellRand = hash2(cellID).x;
    float phase = t + cellRand * 20.0;
    
    // Activation
    float cyclePos = fract(phase);
    float activeRand = hash2(cellID + floor(phase)).y;
    
    // Size determination (Big vs Small)
    float sizeType = hash2(cellID + 100.0).x; 
    bool isSmall = sizeType < 0.75; // 75% are small, 25% are big
    
    float freezeMask = 0.0;

    // Increased density: > 0.80 means 20% of cells are active
    if(activeRand > 0.80){ 
        float fillLevel = 0.0;
        
        // 10% Rise, 80% Hold, 10% Fall
        if(cyclePos < 0.1){
            fillLevel = smoothstep(0.0, 1.0, cyclePos / 0.1);
        } else if(cyclePos < 0.9){
            fillLevel = 1.0; 
        } else {
            fillLevel = 1.0 - smoothstep(0.0, 1.0, (cyclePos - 0.9) / 0.1);
        }
        
        if(fillLevel > 0.01){
            // Organic Filling Logic
            float gradient = sin(coords.x * 0.05) * cos(coords.y * 0.05);
            float localThreshold = 1.0 - fillLevel;
            float solidMask = smoothstep(localThreshold - 0.15, localThreshold + 0.15, rawNoise + gradient * 0.2);
            
            // Size Clipping Logic:
            float edgeBuffer = isSmall ? 0.25 : 0.02;
            
            // Softer edges
            float borderMask = smoothstep(edgeBuffer, edgeBuffer + 0.12, edgeDist);
            
            float finalMask = solidMask * borderMask;
            
            freezeMask = finalMask;

            // Absence of Noise
            noise = mix(noise, 0.0, finalMask);
        }
    }

    float btime = u_time / 1000.0;

    // Partial Freeze
    c += (vec2(
    texture2D(u_noise, (coords) / NSCALE + vec2(btime) * vec2(-0.3, 0.3)).r,
    texture2D(u_noise, (coords) / NSCALE + vec2(btime * 1.1) * vec2(0.3, -0.3)).r
    ) - vec2(0.5)) * 7.0 / u_resolution * (1.0 - freezeMask * 0.35);

    vec4 color = texture2D(u_texture, c);

    // Apply icy tint to polygons
    // Boost brightness and shift towards cyan/blue
    // freezeMask contains the alpha of the polygon shape
    if(freezeMask > 0.01){
        color.rgb = mix(color.rgb, color.rgb * vec3(1.2, 1.25, 1.4) + vec3(0.0, 0.05, 0.1), freezeMask * 0.7);
    }

    if (noise > 0.85){
        if (color.g > mth - 0.1){
            color *= brightnessFactor;
        } else {
            color *= brightnessFactor;
        }
    }

    if (orig.g < mth){
        color *= brightnessFactor;
    }

    gl_FragColor = color;
}
