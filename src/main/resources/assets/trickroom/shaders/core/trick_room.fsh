#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>

in vec2 uv;
in vec4 vertexColor;

out vec4 fragColor;

float ps_hash3(vec3 p) {
    p = fract(p * 0.1031);
    p += dot(p, p.yzx + 33.33);
    return fract((p.x + p.y) * p.z);
}

float ps_noise3(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(
            mix(mix(ps_hash3(i), ps_hash3(i + vec3(1.0, 0.0, 0.0)), f.x),
                    mix(ps_hash3(i + vec3(0.0, 1.0, 0.0)), ps_hash3(i + vec3(1.0, 1.0, 0.0)), f.x), f.y),
            mix(mix(ps_hash3(i + vec3(0.0, 0.0, 1.0)), ps_hash3(i + vec3(1.0, 0.0, 1.0)), f.x),
                    mix(ps_hash3(i + vec3(0.0, 1.0, 1.0)), ps_hash3(i + vec3(1.0, 1.0, 1.0)), f.x), f.y), f.z);
}

float ps_fbm3(vec3 p) {
    float value = 0.0;
    float amplitude = 0.5;
    for (int i = 0; i < 4; ++i) {
        value += amplitude * ps_noise3(p);
        p = p * 2.03 + vec3(11.0, 23.0, 5.0);
        amplitude *= 0.5;
    }
    return value;
}

// The face shows a part of a 4 x 3 atlas of the picture.
const vec2 ATLAS_SIZE = vec2(4.0, 3.0);
const float ATLAS_Y_OFFSET = 1.0;

vec3 palette(float x) {
    vec3 deep = vec3(0.005, 0.025, 0.070);
    vec3 blue = vec3(0.010, 0.220, 0.620);
    vec3 aqua = vec3(0.000, 0.850, 1.000);
    vec3 white = vec3(0.900, 1.000, 1.000);

    vec3 c = mix(deep, blue, smoothstep(0.02, 0.45, x));
    c = mix(c, aqua, smoothstep(0.30, 0.90, x));
    c = mix(c, white, smoothstep(0.82, 1.45, x));
    return c;
}

float waveField(vec2 p, float t) {
    float v = 0.0;

    p += 0.18 * vec2(
            sin(p.y * 2.2 + t * 0.35) + sin(p.y * 5.1 - t * 0.22),
            cos(p.x * 2.0 - t * 0.31) + cos(p.x * 4.7 + t * 0.27)
    );

    v += sin(p.x * 7.0 + t * 0.9);
    v += sin(p.y * 9.0 - t * 1.1);
    v += sin((p.x + p.y) * 6.0 + t * 0.75);
    v += sin((p.x - p.y) * 8.5 - t * 0.85);
    v += sin(length(p) * 15.0 - t * 1.7);

    vec2 q = p;
    q += 0.22 * vec2(
            sin(q.y * 6.0 + t * 0.8),
            cos(q.x * 6.0 - t * 0.9)
    );

    v += sin(q.x * 17.0 + sin(q.y * 3.0 + t) * 2.5);
    v += cos(q.y * 19.0 + cos(q.x * 3.5 - t) * 2.5);
    v += sin((q.x + q.y) * 25.0 + sin(t + q.x * 2.0) * 3.0) * 0.45;

    return v / 8.45;
}

// The original also added a ripple around the mouse, with the mouse button never held:
// it added nothing, and is left out.
vec3 waterSample(vec2 uv, float t, float offset) {
    vec2 p = uv;

    p += 0.09 * vec2(
            sin(t * 0.25 + uv.y * 1.8),
            cos(t * 0.22 + uv.x * 1.7)
    );

    float e = 0.0035;

    float h = waveField(p + vec2(offset), t);
    float hx = waveField(p + vec2(e + offset, offset), t);
    float hy = waveField(p + vec2(offset, e + offset), t);

    vec2 grad = vec2(hx - h, hy - h) / e;

    vec2 q = p + grad * 0.045;

    float w1 = waveField(q, t);
    float w2 = waveField(q * 1.75 + vec2(0.2, -0.1), t * 1.25);
    float w3 = waveField(q * 3.1 - vec2(0.1, 0.3), t * 0.78);
    float w4 = waveField(q * 5.0 + vec2(0.4, 0.2), t * 1.65);

    float caustic =
    pow(abs(w1), 3.0) +
    pow(abs(w2), 5.5) * 0.75 +
    pow(abs(w3), 8.0) * 0.38 +
    pow(abs(w4), 11.0) * 0.20;

    float gradMag = length(grad);
    float foam = smoothstep(2.0, 8.0, gradMag) * 0.38;

    float rippleLines =
    smoothstep(0.72, 0.97, abs(sin(w1 * 9.0 + t * 0.45))) * 0.22 +
    smoothstep(0.80, 0.99, abs(sin(w2 * 12.0 - t * 0.35))) * 0.16 +
    smoothstep(0.88, 1.00, abs(sin(w3 * 16.0 + t * 0.25))) * 0.10;

    float light = caustic * 0.9 + rippleLines + foam;

    vec3 col = palette(light);

    vec3 n = normalize(vec3(-grad * 0.12, 1.0));
    vec3 l = normalize(vec3(0.35, 0.55, 1.0));

    float spec = pow(max(0.0, dot(n, l)), 24.0) * 0.35;

    col += vec3(0.55, 0.85, 1.0) * spec;

    return col;
}

vec3 ps_custom_3(vec2 a, float b, float c, float d) {
    // Water
    // a: face UV, b: time in seconds, c: zoom
    vec2 resolution = max(ScreenSize, vec2(1.0));
    vec2 face = (fract(a) - 0.5) / c + 0.5;
    vec2 atlas = vec2(face.x, face.y + ATLAS_Y_OFFSET) / ATLAS_SIZE;
    vec2 p = (atlas * 2.0 - 1.0) * vec2(resolution.x / resolution.y, 1.0);

    // Red, green and blue each see the water slightly shifted.
    vec3 col = vec3(
            waterSample(p, b, 0.006).r,
            waterSample(p, b, 0.000).g,
            waterSample(p, b, -0.006).b);

    float ang = atan(p.y, p.x);
    float rad = length(p);
    float rays = pow(max(0.0, sin(ang * 7.0 + b * 0.22 + rad * 3.0)), 6.0) * 0.11;
    col += vec3(0.10, 0.35, 0.55) * rays;

    col = 1.0 - exp(-col * 1.22);
    return pow(col, vec3(0.92));
}

float ps_custom_5(vec2 a, float b, float c, float d) {
    // Border
    // 1 on the thin line around each face, 0 inside. a: face UV, b: width in pixels
    vec2 face = fract(a);
    vec2 size = b / max(ScreenSize, vec2(1.0));
    bool border = face.x <= size.x || face.x >= 1.0 - size.x
    || face.y <= size.y || face.y >= 1.0 - size.y;
    return border ? 1.0 : 0.0;
}

void main() {
    float v0 = GameTime * 1200.0;
    vec3 v1 = ps_custom_3(uv, v0, 0.3, 0.0);
    vec4 v2 = vertexColor * ColorModulator;
    vec4 v3 = vec4(v1, 1.0) * v2;
    float v4 = ps_custom_5(uv, 0.6, 0.0, 0.0);
    float v5 = v4 * 0.35;
    vec4 v6 = mix(v3, vec4(0.587, 0.948, 1.0, 1.0), v5);
    vec4 v7 = v2;
    float v8 = v7.w * 0.9;
    float v9 = v7.w * 0.82;
    float v10 = max(v8, v9);
    float v11 = mix(v8, v10, v4);
    float v12 = v11;
    if (v12 < 0.0) discard;
    fragColor = vec4((v6).xyz, v12);
}
