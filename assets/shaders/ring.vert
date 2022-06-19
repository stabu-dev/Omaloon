attribute vec4 a_position;
attribute vec3 a_normal;
attribute vec4 a_color;

uniform mat4 u_proj;
uniform mat4 u_trans;
uniform vec2 u_lightdir;
uniform vec2 u_ambientColor;
uniform float u_alpha;

varying vec4 v_col;

const vec2 diffuse = vec2(0.01);

void main(){
    vec2 norc = u_ambientColor * (diffuse + vec2(clamp((dot(a_normal, u_lightdir) + 1.0) / 2.0, 0.0, 1.0)));

    v_col = a_color * vec4(u_alpha);
    gl_Position = u_proj * u_trans * a_position;
}
