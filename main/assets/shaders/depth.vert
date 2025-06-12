#define HIGHP

attribute vec3 a_position;

varying float v_depth;

uniform mat4 u_proj;
uniform mat4 u_trans;

uniform vec3 u_camPos;

void main(){
    vec4 pos = u_trans * vec4(a_position, 1.0);
    v_depth = length(pos.xyz - u_camPos);
    gl_Position = u_proj * pos;
}