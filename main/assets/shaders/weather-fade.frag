#define HIGHP

varying vec4 v_color;
varying vec4 v_mix_color;
varying vec2 v_texCoords;
varying vec2 v_world;

uniform sampler2D u_texture;
uniform vec4 u_fields[MAX_FIELDS];
uniform vec4 u_arcs[MAX_FIELDS];
uniform vec4 u_lobes[MAX_FIELDS];
uniform float u_minAlpha;

const float mergeTol = 0.015;

vec2 rotate(vec2 v, float c, float s){
    return vec2(v.x * c - v.y * s, v.x * s + v.y * c);
}

float band(float radius){
    return max(radius * 0.06, 1.5);
}

vec2 faceAxis(vec2 dir, float cosHalf, float sinHalf, float side){
    return rotate(dir, cosHalf, side * sinHalf);
}

float insideWedge(vec2 rel, vec2 dir, float cosHalf, float sinHalf, float d, float tol){
    return step((cosHalf - tol * sinHalf) * d, dot(rel, dir));
}

float faceDistance(vec2 rel, float d, vec2 face){
    return dot(rel, face) >= 0.0 ? abs(face.x * rel.y - face.y * rel.x) : d;
}

float covers(vec2 rel, float d, float radius, vec2 dir, float cosHalf, float sinHalf, float present){
    return present * insideWedge(rel, dir, cosHalf, sinHalf, d, mergeTol) * (1.0 - step(radius, d));
}

float partCoverage(vec2 rel, float d, float radius, vec2 dir, float cosHalf, float sinHalf, vec2 faceA, vec2 faceB, float b, float seamA, float seamB){
    float fa = max(1.0 - smoothstep(-b, 0.0, -faceDistance(rel, d, faceA)), seamA);
    float fb = max(1.0 - smoothstep(-b, 0.0, -faceDistance(rel, d, faceB)), seamB);
    return (1.0 - smoothstep(radius - b, radius, d)) * insideWedge(rel, dir, cosHalf, sinHalf, d, 0.0) * min(fa, fb);
}

void main(){
    vec4 c = texture2D(u_texture, v_texCoords);
    vec4 col = v_color * mix(c, vec4(v_mix_color.rgb, c.a), v_mix_color.a);

    float cover = 0.0;
    for(int i = 0; i < MAX_FIELDS; i++){
        vec4 f = u_fields[i];
        vec4 l = u_lobes[i];
        float maxRadius = max(f.z, max(f.w, max(l.z, l.w)));
        if(maxRadius <= 0.0) break;
        float b = band(maxRadius);

        vec2 rel = v_world - f.xy;
        float d = max(length(rel), 0.001);
        if(d > maxRadius + b) continue;

        vec4 a = u_arcs[i];
        float hasArc = step(0.001, f.w), hasLeft = step(0.001, l.z), hasRight = step(0.001, l.w);
        vec2 dirLeft = rotate(a.xy, l.x, l.y), dirRight = rotate(a.xy, l.x, -l.y);
        vec2 arcA = faceAxis(a.xy, a.z, a.w, -1.0), arcB = faceAxis(a.xy, a.z, a.w, 1.0);
        vec2 leftA = faceAxis(dirLeft, LOBE_COS, LOBE_SIN, -1.0), leftB = faceAxis(dirLeft, LOBE_COS, LOBE_SIN, 1.0);
        vec2 rightA = faceAxis(dirRight, LOBE_COS, LOBE_SIN, -1.0), rightB = faceAxis(dirRight, LOBE_COS, LOBE_SIN, 1.0);

        float inCore = 1.0 - step(f.z, d);
        float arcSeamA = max(inCore, covers(arcA * d, d, l.w, dirRight, LOBE_COS, LOBE_SIN, hasRight));
        float arcSeamB = max(inCore, covers(arcB * d, d, l.z, dirLeft, LOBE_COS, LOBE_SIN, hasLeft));
        float leftSeamA = max(inCore, max(covers(leftA * d, d, f.w, a.xy, a.z, a.w, hasArc),
                                          covers(leftA * d, d, l.w, dirRight, LOBE_COS, LOBE_SIN, hasRight)));
        float leftSeamB = max(inCore, covers(leftB * d, d, l.w, dirRight, LOBE_COS, LOBE_SIN, hasRight));
        float rightSeamA = max(inCore, covers(rightA * d, d, l.z, dirLeft, LOBE_COS, LOBE_SIN, hasLeft));
        float rightSeamB = max(inCore, max(covers(rightB * d, d, f.w, a.xy, a.z, a.w, hasArc),
                                           covers(rightB * d, d, l.z, dirLeft, LOBE_COS, LOBE_SIN, hasLeft)));

        cover = max(cover, 1.0 - smoothstep(f.z - b, f.z, d));
        if(hasArc > 0.5) cover = max(cover, partCoverage(rel, d, f.w, a.xy, a.z, a.w, arcA, arcB, b, arcSeamA, arcSeamB));
        if(hasLeft > 0.5) cover = max(cover, partCoverage(rel, d, l.z, dirLeft, LOBE_COS, LOBE_SIN, leftA, leftB, b, leftSeamA, leftSeamB));
        if(hasRight > 0.5) cover = max(cover, partCoverage(rel, d, l.w, dirRight, LOBE_COS, LOBE_SIN, rightA, rightB, b, rightSeamA, rightSeamB));
        if(cover >= 1.0) break;
    }

    col.a *= mix(1.0, u_minAlpha, cover);
    gl_FragColor = col;
}
