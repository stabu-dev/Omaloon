uniform sampler2D u_texture;
uniform vec4 u_textureUV;

varying vec2 v_texCoords;

const float pi = 3.14159265358979323;

void main(){
    float tresh = 1.0 - 0.35;

	vec2 uv = (v_texCoords - 0.5) * 2.0;
	float len = length(uv);
	if (len < tresh || len > 1.0) discard;

	float normal = acos(dot(normalize(uv), vec2(1.0, 0.0))) / pi;

	if (uv.y > 0) normal *= -1.0;

	normal += 1.0;
	normal /= 2.0;

	normal *= 16.0;

	float h = 1.0 - (1.0 - len) / (1.0 - tresh);

	float u = u_textureUV.x * mod(normal, 1.0) + u_textureUV.z * (1.0 - mod(normal, 1.0));
	float v = u_textureUV.y * h + u_textureUV.w * (1.0 - h);

	//gl_FragColor = vec4(v_texCoords, 0.0, 1.0);
	gl_FragColor = texture2D(u_texture, vec2(u, v));


}
