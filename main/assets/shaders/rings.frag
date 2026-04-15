varying vec2 v_texCoords;

void main(){
    float tresh = 0.1;
    if (
      v_texCoords.x > tresh &&
      v_texCoords.x < 1.0 - tresh &&
      v_texCoords.y > tresh &&
      v_texCoords.y < 1.0 - tresh
    ) discard;
	gl_FragColor = vec4(v_texCoords, 0.0, 1.0);
}
