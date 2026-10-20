attribute vec4 a_position;
attribute vec3 a_normal;

uniform mat4 u_proj;
uniform mat4 u_trans;

varying vec3 v_normal;
varying float v_wall;

void main(){
    v_normal = (u_trans * vec4(a_normal, 0.0)).xyz;
    v_wall = 1.0 - step(0.5, dot(normalize(a_position.xyz), a_normal));
    gl_Position = u_proj * u_trans * a_position;
}
