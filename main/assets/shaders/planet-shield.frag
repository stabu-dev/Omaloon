#define HIGHP

#if defined(FIELD)

#define ALPHA 0.18
#define WALL_ALPHA 0.3
#define CREASE_ALPHA 0.7

uniform sampler2D u_texture;
uniform vec2 u_resolution;
uniform vec2 u_texsize;
uniform vec2 u_invsize;
uniform float u_time;
uniform float u_dp;
uniform float u_outline;

float isWall(vec4 color){
    return color.a > 0.9 && color.a < 0.97 ? 1.0 : 0.0;
}

void main(){
    vec2 T = gl_FragCoord.xy / u_resolution;
    vec2 coords = T * u_texsize;

    T += vec2(sin(coords.y / 3.0 + u_time / 20.0), sin(coords.x / 3.0 + u_time / 20.0)) / u_texsize;

    vec4 color = texture2D(u_texture, T);
    if(color.a < 0.9) discard;

    vec2 v = u_invsize;

    vec4 up = texture2D(u_texture, T + vec2(0.0, u_outline) * v);
    vec4 down = texture2D(u_texture, T + vec2(0.0, -u_outline) * v);
    vec4 right = texture2D(u_texture, T + vec2(u_outline, 0.0) * v);
    vec4 left = texture2D(u_texture, T + vec2(-u_outline, 0.0) * v);

    float mined = min(min(min(up.a, down.a), right.a), left.a);
    float walls = isWall(up) + isWall(down) + isWall(right) + isWall(left);

    if(mined < 0.9){
        gl_FragColor = vec4(color.rgb, 1.0);
    }else if(color.a >= 0.97 && walls > 0.0){
        gl_FragColor = vec4(color.rgb, CREASE_ALPHA);
    }else{
        float alpha = mix(ALPHA, WALL_ALPHA, isWall(color));

        if(mod(coords.x / u_dp + coords.y / u_dp + sin(coords.x / u_dp / 5.0) * 3.0 + sin(coords.y / u_dp / 5.0) * 3.0 + u_time / 4.0, 10.0) < 2.0){
            color *= 1.65;
        }

        gl_FragColor = vec4(color.rgb, alpha);
    }
}

#else

uniform vec4 u_color;
uniform vec3 u_lightdir;
uniform vec3 u_ambientColor;

varying vec3 v_normal;
varying float v_wall;

void main(){
    float light = clamp((dot(normalize(v_normal), u_lightdir) + 1.0) / 2.0, 0.0, 1.0);
    gl_FragColor = vec4(u_color.rgb * u_ambientColor * (0.3 + 0.7 * light), mix(1.0, 0.94, v_wall));
}

#endif
