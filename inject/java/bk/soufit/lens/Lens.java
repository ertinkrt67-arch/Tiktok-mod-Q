package bk.soufit.lens;

import android.graphics.RuntimeShader;
import android.os.Build;

public final class Lens {
    private Lens() {}

    public static final float EDGE_INTENSITY = 0.012f;
    public static final float RIM_INTENSITY = 0.03f;
    public static final float EDGE_DISTANCE = 0.15f;
    public static final float RIM_DISTANCE = 0.8f;
    public static final float BLUR_RADIUS = 10f;
    public static final float TINT_OPACITY = 0.05f;
    public static final float VIBRANCE = 0.30f;
    public static final float BEVEL_PX = 4f;
    public static final float SPEC_GAIN = 0.75f;
    public static final float SPEC_SHARPNESS = 10f;
    public static final float FRESNEL_GAIN = 0.25f;
    public static final float FRESNEL_POWER = 3f;
    public static final float SHADE_GAIN = 0.15f;
    public static final float LIGHT_X = -0.6f, LIGHT_Y = -0.8f;
    public static final float AA_PX = 0.75f;

    static final String VIDEO_CHILD = "video";

    static final String SHADER = "uniform float2 size;\n" + "uniform float2 origin;\n" + "uniform float2 sourceOffset;\n"
            + "uniform float radius;\n" + "uniform float composite;\n" + "uniform float edgeIntensity;\n"
            + "uniform float rimIntensity;\n" + "uniform float edgeDistance;\n" + "uniform float rimDistance;\n"
            + "uniform float blurRadius;\n" + "uniform float tintOpacity;\n" + "uniform float vibrance;\n"
            + "uniform float bevelPx;\n" + "uniform float specGain;\n" + "uniform float specSharpness;\n"
            + "uniform float fresnelGain;\n" + "uniform float fresnelPower;\n" + "uniform float shadeGain;\n"
            + "uniform float2 lightDir;\n" + "uniform float aaPx;\n" + "uniform shader video;\n"
            + "float roundBox(float2 p, float2 halfSize, float r) {\n"
            + "    float2 q = abs(p) - (halfSize - float2(r));\n"
            + "    return length(max(q, float2(0.0))) + min(max(q.x, q.y), 0.0) - r;\n" + "}\n"
            + "float2 outward(float2 p, float2 halfSize, float r) {\n"
            + "    float2 q = abs(p) - (halfSize - float2(r));\n"
            + "    if (q.x > 0.0 && q.y > 0.0) return normalize(q) * sign(p);\n"
            + "    return q.x > q.y ? float2(sign(p.x), 0.0) : float2(0.0, sign(p.y));\n" + "}\n"
            + "half4 main(float2 at) {\n"
            + "    float2 halfSize = size * 0.5;\n"
            + "    float r = min(radius, min(halfSize.x, halfSize.y));\n"
            + "    float2 p = at - origin - halfSize;\n"
            + "    float d = roundBox(p, halfSize, r);\n"
            + "    if (d > aaPx) {\n" + "        if (composite < 0.5) return half4(0.0);\n" + "        return video.eval(at);\n" + "    }\n"
            + "    float2 n = outward(p, halfSize, r);\n"
            + "    float inside = max(-d, 0.0);\n"
            + "    float shortSide = min(size.x, size.y);\n"
            + "    float2 warp = n * shortSide * (edgeIntensity * exp(-inside * edgeDistance)\n"
            + "            + rimIntensity * exp(-inside * rimDistance));\n"
            + "    float2 uv = at + sourceOffset + warp;\n"
            + "    float sigma = max(blurRadius * 0.5, 0.5);\n"
            + "    float step = sigma * 0.5;\n"
            + "    float ws = sigma * 1.08;\n"
            + "    float3 acc = float3(0.0);\n"
            + "    float wsum = 0.0;\n"
            + "    for (int j = -4; j <= 4; j++) {\n"
            + "        for (int i = -4; i <= 4; i++) {\n"
            + "            float2 o = float2(float(i), float(j)) * step;\n"
            + "            float w = exp(-dot(o, o) / (2.0 * ws * ws));\n"
            + "            acc += float3(video.eval(uv + o).rgb) * w;\n"
            + "            wsum += w;\n"
            + "        }\n"
            + "    }\n"
            + "    float3 seen = acc / wsum;\n"
            + "    float hi = max(seen.r, max(seen.g, seen.b));\n"
            + "    float lo = min(seen.r, min(seen.g, seen.b));\n"
            + "    float luma = dot(seen, float3(0.2126, 0.7152, 0.0722));\n"
            + "    float sat = hi > 0.0001 ? (hi - lo) / hi : 0.0;\n"
            + "    float boost = vibrance * (1.0 - sat) * smoothstep(0.08, 0.3, hi);\n"
            + "    seen = float3(luma) + (seen - float3(luma)) * (1.0 + boost);\n"
            + "    seen *= 1.0 - tintOpacity;\n"
            + "    float lip = exp(-inside / bevelPx);\n"
            + "    float lit = dot(n, lightDir);\n"
            + "    float spec = pow(max(lit, 0.0), specSharpness) * lip * lip * specGain;\n"
            + "    float sheen = pow(lip, fresnelPower) * fresnelGain;\n"
            + "    float shade = pow(max(-lit, 0.0), 2.0) * lip * lip * shadeGain;\n"
            + "    float3 lensColor = clamp(seen * (1.0 - shade) + float3(spec + sheen), 0.0, 1.0);\n"
            + "    float coverage = 1.0 - smoothstep(-aaPx, aaPx, d);\n"
            + "    if (composite < 0.5) return half4(half3(lensColor), coverage);\n"
            + "    float3 base = float3(video.eval(at).rgb);\n"
            + "    return half4(half3(mix(base, lensColor, coverage)), 1.0);\n"
            + "}\n";

    public static RuntimeShader create() {
        if (Build.VERSION.SDK_INT < 33) return null;
        try {
            return new RuntimeShader(SHADER);
        } catch (Throwable unusable) {
            return null;
        }
    }

    public static float padding(float width, float height) {
        float sigma = Math.max(BLUR_RADIUS * 0.5f, 0.5f);
        return Math.min(width, height) * (EDGE_INTENSITY + RIM_INTENSITY) + 3f * sigma + 1f;
    }

    public static void bind(RuntimeShader lens, float left, float top, float width, float height, float radius,
                            float sourceX, float sourceY, boolean composite) {
        lens.setFloatUniform("size", width, height);
        lens.setFloatUniform("origin", left, top);
        lens.setFloatUniform("sourceOffset", sourceX, sourceY);
        lens.setFloatUniform("radius", radius);
        lens.setFloatUniform("composite", composite ? 1f : 0f);
        lens.setFloatUniform("edgeIntensity", EDGE_INTENSITY);
        lens.setFloatUniform("rimIntensity", RIM_INTENSITY);
        lens.setFloatUniform("edgeDistance", EDGE_DISTANCE);
        lens.setFloatUniform("rimDistance", RIM_DISTANCE);
        lens.setFloatUniform("blurRadius", BLUR_RADIUS);
        lens.setFloatUniform("tintOpacity", TINT_OPACITY);
        lens.setFloatUniform("vibrance", VIBRANCE);
        lens.setFloatUniform("bevelPx", BEVEL_PX);
        lens.setFloatUniform("specGain", SPEC_GAIN);
        lens.setFloatUniform("specSharpness", SPEC_SHARPNESS);
        lens.setFloatUniform("fresnelGain", FRESNEL_GAIN);
        lens.setFloatUniform("fresnelPower", FRESNEL_POWER);
        lens.setFloatUniform("shadeGain", SHADE_GAIN);
        lens.setFloatUniform("lightDir", LIGHT_X, LIGHT_Y);
        lens.setFloatUniform("aaPx", AA_PX);
    }
}
