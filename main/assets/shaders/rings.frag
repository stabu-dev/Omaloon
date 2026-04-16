uniform sampler2D u_texture;
uniform vec4 u_textureUV;

uniform float u_opacity;

uniform vec3 u_planet_pos;
uniform vec3 u_sun_pos;

varying vec2 v_texCoords;
varying vec3 v_position;

const float pi = 3.14159265358979323;

void main(){

    float tresh = 1.0 - 0.35;

	vec2 uv = (v_texCoords - 0.5) * 2.0;
	float len = length(uv);
	if (len < tresh || len > 1.0) discard;

//	if (
//	    v_position.y < 1.0 && v_position.y > -1.0 &&
//	    v_position.x < 1.0 && v_position.x > -1.0
//	) discard;

	float normal = acos(dot(normalize(uv), vec2(1.0, 0.0))) / pi;

	if (uv.y > 0) normal *= -1.0;

	normal += 1.0;
	normal /= 2.0;

	normal *= 16.0;

	float h = 1.0 - (1.0 - len) / (1.0 - tresh);

	float u = u_textureUV.x * mod(normal, 1.0) + u_textureUV.z * (1.0 - mod(normal, 1.0));
	float v = u_textureUV.y * h + u_textureUV.w * (1.0 - h);

	gl_FragColor = texture2D(u_texture, vec2(u, v)) * vec4(vec3(1.0), u_opacity);

	vec2 ringNor = normalize(v_position.xz);
	vec2 planetNor = normalize(u_planet_pos.xz - u_sun_pos.xz);
	float ringAngle = acos(dot(ringNor, planetNor));

	float ringRadius = sin(ringAngle) * length(v_position.xz);
	float ringDistance = cos(ringAngle) * length(v_position.xz);

	//float maxRad = 0 / 3 = 17 / 1 = 17 + ringDistance / x
	float maxRad = 3 + (ringDistance + 17) / 17 * -2;

	if (dot(ringNor, planetNor) < 0) {
	    gl_FragColor = vec4(0.5);
	} else {
	    if (ringRadius < maxRad) gl_FragColor = vec4(0.25);
	}
}
