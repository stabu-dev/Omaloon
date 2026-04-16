attribute vec4 a_position;
attribute vec2 a_texCoord0;

uniform mat4 u_proj;
uniform mat4 u_trans;

varying vec2 v_texCoords;
varying vec3 v_position;

void main(){
  v_texCoords = a_texCoord0;
  v_position = a_position.xyz;
  gl_Position = u_proj * u_trans * a_position;
}
