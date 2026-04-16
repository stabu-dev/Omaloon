uniform sampler2D u_texture;
uniform vec4 u_textureUV;

uniform float u_opacity;

uniform vec3 u_planet_pos;
uniform vec3 u_sun_pos;

uniform vec2 u_stroke;
uniform float u_planet_radius;

varying vec2 v_texCoords;
varying vec3 v_position;

const float pi = 3.14159265358979323;

float shadow() {
    float ringAngle = acos(dot(normalize(v_position), normalize(u_planet_pos - u_sun_pos)));
	float ringRadius = sin(ringAngle) * length(v_position);
	float ringDistance = cos(ringAngle) * length(v_position);

	float maxRad = u_planet_radius;
	float softness = 0.1;

	float radAlpha = 1.0 - smoothstep(maxRad, maxRad + softness, ringRadius);
	float distAlpha = smoothstep(-softness, 0.0, ringDistance);

	return radAlpha * distAlpha;
}

void main(){
	vec2 uv = (v_texCoords - 0.5) * 2.0;
	float len = length(uv);
	if (len < u_stroke.x || len > u_stroke.y) discard;

	float normal = acos(dot(normalize(uv), vec2(1.0, 0.0))) / pi;

	if (uv.y > 0) normal *= -1.0;

	normal += 1.0;
	normal /= 2.0;

	float h = (len - u_stroke.x) / (u_stroke.y - u_stroke.x);

	float u = mix(u_textureUV.x, u_textureUV.z, 1.0 - mod(normal, 1.0));
	float v = mix(u_textureUV.y, u_textureUV.w, 1.0 - h);

	vec4 color = texture2D(u_texture, vec2(u, v));
	if(color.a < 0.01) discard;

	gl_FragColor = mix(color, vec4(0.0, 0.0, 0.0, color.a), shadow());
}
