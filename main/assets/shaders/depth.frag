#define HIGHP

varying float v_depth;

uniform vec2 u_camRange;

vec4 pack(float value){
    value = clamp((value - u_camRange.x) / u_camRange.y, 0.0, 1.0);

    vec4 enc = vec4(1.0, 255.0, 65025.0, 16581375.0) * value;
    enc = fract(enc);
    enc -= enc.yzww * vec2(1.0 / 255.0, 0.0).xxxy;
    return enc;
}

void main(){
    gl_FragColor = pack(v_depth);
}