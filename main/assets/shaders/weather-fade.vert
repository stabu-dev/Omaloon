attribute vec4 a_position;
attribute vec4 a_color;
attribute vec2 a_texCoord0;
attribute vec4 a_mix_color;

uniform mat4 u_projTrans;

varying vec4 v_color;
varying vec4 v_mix_color;
varying vec2 v_texCoords;
varying vec2 v_world;

void main(){
    v_color = a_color;
    v_color.a = v_color.a * (255.0 / 254.0);
    v_mix_color = a_mix_color;
    v_mix_color.a *= (255.0 / 254.0);
    v_texCoords = a_texCoord0;
    v_world = a_position.xy;
    gl_Position = u_projTrans * a_position;
}
