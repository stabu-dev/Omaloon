varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform vec4 u_color;

void main(){
    vec4 color = texture2D(u_texture, v_texCoords);
    gl_FragColor = color*u_color;
}
