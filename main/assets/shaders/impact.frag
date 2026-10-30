uniform sampler2D u_texture;
varying vec2 v_texCoords;

#if defined(CUT)
varying vec4 v_color;

void main(){
    float alpha = (texture2D(u_texture, v_texCoords).a * v_color.a - 0.75) / (1.0 - 0.75);
    gl_FragColor = vec4(v_color.rgb, alpha);
}

#elif defined(UNSATURATED)
void main(){
    if(texture2D(u_texture, v_texCoords).a >= 1.0) discard;
    gl_FragColor = vec4(0.0);
}

#elif defined(SHADOW)
#define MAX_LIGHTS 16

uniform vec2 u_texsize;
uniform vec3 u_lights[MAX_LIGHTS];
uniform int u_lights_count;
uniform vec2 u_invsize;
uniform int u_invert;


void main(){
    vec4 c = texture2D(u_texture, v_texCoords);
    if(c.a <= 0.01){
        gl_FragColor = vec4(0.0);
        return;
    }

    bool white = false;
    bool white2 = false;
    bool black = false;

    for(int i = 0; i < MAX_LIGHTS; i++){
        vec2 pos = u_lights[i].xy;
        float intensity = u_lights[i].z;

        vec2 dir = normalize((v_texCoords * u_texsize) - pos) * u_invsize;

        vec2 off = dir * intensity;

        if(texture2D(u_texture, v_texCoords - off).a <= 0.01){
            white = true;
        }
        if(texture2D(u_texture, v_texCoords + dir * 2.0).a <= 0.01){
            black = true;
        }
        if(texture2D(u_texture, v_texCoords - dir * 2.5).a <= 0.01){
            white2 = true;
        }

        if(white && white2 || i >= u_lights_count - 1){
            break;
        }
    }

    bool lit = white && (!black || white2);
    if(u_invert == 1) lit = !lit;
    float v = lit ? 1.0 : 0.0;
    gl_FragColor = vec4(v, v, v, c.a);
}

#elif defined(MASK)
#define MAX_AREAS 16

uniform vec4 u_areas[MAX_AREAS];
uniform vec4 u_strengths[MAX_AREAS / 4];
uniform int u_areas_count;
const float outerAlpha = 0.4;

varying vec4 v_color;

void main(){
    vec4 c = texture2D(u_texture, v_texCoords) * v_color;
    c.a *= 255.0 / 254.0;
    float mask = 0.0, weight = 0.0;
    vec3 tint = vec3(1.0);
    vec3 sum = vec3(0.0);
    int contributors = 0;

    for(int i = 0; i < MAX_AREAS; i++){
        if(i >= u_areas_count) break;

        vec4 area = u_areas[i];
        float dst = distance(gl_FragCoord.xy, area.xy);
        float inner = 1.0 - smoothstep(area.z * 0.8, area.z * 1.1, dst);
        float outer = outerAlpha * (1.0 - smoothstep(area.z, area.w, dst));
        float areaMask = max(inner, outer) * u_strengths[i / 4][i - (i / 4) * 4];
        mask = max(mask, areaMask);
        if(areaMask > 0.0){
            vec3 color = vec3(1.0);
            if(dst >= area.z * 1.1){
                color = mix(vec3(1.0, 0.62, 0.3), vec3(0.92, 0.26, 0.18), smoothstep(area.z * 1.1, area.w, dst));
            }else if(dst > area.z * 0.4){
                color = mix(vec3(1.0), vec3(1.0, 0.62, 0.3), smoothstep(area.z * 0.4, area.z * 1.1, dst));
            }
            tint = color;
            sum += color * areaMask;
            weight += areaMask;
            contributors++;
        }
    }

    if(contributors > 1) tint = sum / weight;
    gl_FragColor = vec4(c.rgb * tint, c.a * mask);
}
#endif