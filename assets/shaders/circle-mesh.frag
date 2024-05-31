varying vec2 v_texCoords;
varying vec4 v_col;
varying vec3 v_position;

uniform sampler2D u_texture;
uniform vec4 u_color;

uniform vec4 u_sun_info;
uniform vec4 u_planet_info;

void main(){
    vec4 color = texture2D(u_texture, v_texCoords);

    vec3 p1 = u_sun_info.xyz;
    float r1 = u_sun_info.w;
    vec3 p2 = u_planet_info.xyz;
    float r2 = u_planet_info.w;
    float d = length(p1 - p2);

    float c = -(d * r1) / (r1 - r2);

    float k = r1 / (sqrt(c * c - r1 * r1));

    vec3 t = (p2 - p1) / d;

    vec3 p4 = -(t * c + p1);

    vec3 p4_2 = -p4 + p2;

    float len2_4 = length(p4_2);

    vec3 point = v_position;
    float dot_ = dot(point - p4, p4_2) / len2_4;
    float b = length((p4 - t * dot_) - point);
    float i = b - k * dot_;
    float ip = i / (k * dot_);
    float unitDistance = b / (k * dot_);
    vec4 col = vec4(1.0);

    if (length(point - p4) > length(p2 - p4)){
    } else {
        float ci = clamp(i, -1.0, 1.0);
        if (i <= 0.0){
            float aip = -ip;
            float scale = 0.75;
            float a = abs(unitDistance) - 0.5;
            float it = 0.5 + (1.0 - pow(1.0 - abs(a) * 2.0, 2.0)) * sign(a) / 2.0;
            col.rgb *= smoothstep(0.0, 1.0, it);
        }
        col.rgb /= col.r;
        col.rgb *= smoothstep(0.0, 1.0, i);
    }

    gl_FragColor = color * u_color * v_col * col;
}
