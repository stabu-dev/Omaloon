attribute vec4 a_position;
attribute vec2 a_texCoord0;

uniform mat4 u_proj;
uniform mat4 u_trans;
uniform vec3 u_planet_pos;

varying vec2 v_texCoords;
varying vec3 v_position;

void main(){
  v_texCoords = a_texCoord0;
  v_position = (u_trans * a_position).xyz - u_planet_pos;
  gl_Position = u_proj * u_trans * a_position;
}
