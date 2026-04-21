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

float hash(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

float cvnoise(vec2 p, float periodX) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);

    vec2 i1 = i + vec2(1.0, 0.0);
    vec2 i2 = i + vec2(0.0, 1.0);
    vec2 i3 = i + vec2(1.0, 1.0);

    i.x = mod(i.x, periodX);
    i1.x = mod(i1.x, periodX);
    i2.x = mod(i2.x, periodX);
    i3.x = mod(i3.x, periodX);

    float a = hash(i);
    float b = hash(i1);
    float c = hash(i2);
    float d = hash(i3);

    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

float dissolveNoise(vec2 ringUV) {
    float n1 = cvnoise(ringUV * vec2(40.0, 10.0), 40.0);
    float n2 = cvnoise(ringUV * vec2(120.0, 30.0), 120.0);
    float n3 = cvnoise(ringUV * vec2(300.0, 80.0), 300.0);

    return n1 * 0.53 + n2 * 0.32 + n3 * 0.15;
}

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

	if (u_opacity < 0.999) {
		float macro = dissolveNoise(vec2(normal, h));

		float pixelScale = 150.0;
		float micro = hash(floor(uv * pixelScale));

		float n = mix(macro, micro, 0.5);
		if (n > u_opacity) discard;

		float edgeDist = u_opacity - n;
		float edgeWidth = 0.05;
		if (edgeDist < edgeWidth) {
			float glow = 1.0 - (edgeDist / edgeWidth);
			color.rgb += color.rgb * glow * 1.3;
		}
	}

	gl_FragColor = mix(color, vec4(0.0, 0.0, 0.0, color.a), shadow());
}
