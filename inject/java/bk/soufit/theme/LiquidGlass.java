package bk.soufit.theme;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.RuntimeShader;
import android.graphics.Shader;
import android.os.Build;
public final class LiquidGlass {
    private LiquidGlass() {}

    public static final float BLUR_DOWNSCALE = 8f;
    public static final float EDGE_INTENSITY = 1.6f;
    public static final float EDGE_DISTANCE = 1.1f;
    public static final float RIM_INTENSITY = 0.5f;
    public static final float RIM_DISTANCE = 4.0f;
    public static final float BEVEL_WIDTH = 2.5f;
    public static final float CHROMA = 0.12f;
    public static final float VIBRANCE = 0.28f;
    public static final float TINT_LIGHT = 0.05f;
    public static final float TINT_DARK = 0.09f;
    public static final float FRESNEL_POWER = 3.5f;
    public static final float FRESNEL_GAIN = 0.45f;
    public static final float SPEC_SHARPNESS = 10f;
    public static final float SPEC_GAIN = 0.85f;
    public static final float SHADE_GAIN = 0.30f;
    public static final float GRAIN = 1.0f;
    public static final float LIGHT_X = -0.6f, LIGHT_Y = -0.8f;
    private static final int BODY_LIGHT = 0x59FFFFFF;
    private static final int BODY_DARK_ALPHA = 148;

    private static final String HEADER = "uniform float2 size;\n" + "uniform float2 origin;\n" + "uniform float radius;\n"
            + "uniform float shapeKind;\n" + "uniform float edgeWidth;\n" + "uniform float unit;\n" + "uniform float dark;\n"
            + "uniform float2 lightDir;\n" + "uniform float specSharpness;\n" + "uniform float specGain;\n"
            + "uniform float fresnelPower;\n" + "uniform float fresnelGain;\n" + "uniform float shadeGain;\n"
            + "uniform float edgeIntensity;\n" + "uniform float edgeDistance;\n" + "uniform float rimIntensity;\n"
            + "uniform float rimDistance;\n"
            + "float roundBoxDistance(float2 p, float2 halfSize, float r) {\n" + "    float2 q = abs(p) - (halfSize - float2(r));\n"
            + "    return length(max(q, float2(0.0))) + min(max(q.x, q.y), 0.0) - r;\n" + "}\n"
            + "float pillDistance(float2 p, float2 halfSize) {\n" + "    return roundBoxDistance(p, halfSize, min(halfSize.x, halfSize.y));\n"
            + "}\n" + "float circleDistance(float2 p, float radiusPx) {\n" + "    return length(p) - radiusPx;\n" + "}\n"
            + "float sceneDistance(float2 p, float2 halfSize) {\n" + "    if (shapeKind > 1.5) return circleDistance(p, min(halfSize.x, halfSize.y));\n"
            + "    if (shapeKind > 0.5) return pillDistance(p, halfSize);\n" + "    return roundBoxDistance(p, halfSize, radius);\n" + "}\n"
            + "float2 surfaceNormal(float2 p, float2 halfSize) {\n" + "    float e = 0.5;\n"
            + "    float2 g = float2(sceneDistance(p + float2(e, 0.0), halfSize) - sceneDistance(p - float2(e, 0.0), halfSize),\n"
            + "                      sceneDistance(p + float2(0.0, e), halfSize) - sceneDistance(p - float2(0.0, e), halfSize));\n"
            + "    float l = length(g);\n" + "    return l > 1e-5 ? g / l : float2(0.0, 1.0);\n" + "}\n";

    private static final String LIP = "    float2 p = at - origin - size * 0.5;\n" + "    float2 halfSize = size * 0.5;\n"
            + "    float d = sceneDistance(p, halfSize);\n" + "    float2 n = surfaceNormal(p, halfSize);\n"
            + "    float depth = clamp(-d / edgeWidth, 0.0, 1.0);\n" + "    float lip = 1.0 - depth;\n"
            + "    float height = sqrt(max(1.0 - (1.0 - depth) * (1.0 - depth), 0.0));\n"
            + "    float inside = max(-d, 0.0) / unit;\n"
            + "    float lit = dot(n, lightDir);\n" + "    float rim = lip * lip;\n"
            + "    float spec = pow(max(lit, 0.0), specSharpness) * rim * specGain;\n"
            + "    float shade = pow(max(-lit, 0.0), 2.0) * rim * shadeGain;\n"
            + "    float sheen = pow(lip, fresnelPower) * fresnelGain;\n"
            + "    float glow = dark > 0.5 ? 0.6 : 1.0;\n"
            + "    float bend = edgeIntensity * exp(-inside * edgeDistance) + rimIntensity * exp(-inside * rimDistance);\n"
            + "    float2 shift = n * bend * unit;\n";

    private static final String EDGE_SOURCE = HEADER + "half4 main(float2 at) {\n" + LIP
            + "    float light = clamp((spec + sheen) * glow, 0.0, 1.0);\n" + "    float dim = clamp(shade, 0.0, 1.0);\n"
            + "    if (light >= dim) return half4(1.0, 1.0, 1.0, light);\n" + "    return half4(0.0, 0.0, 0.0, dim);\n" + "}\n";

    private static final String BODY_SOURCE = HEADER + "uniform float chroma;\n" + "uniform float vibrance;\n"
            + "uniform float tint;\n" + "uniform float grain;\n" + "uniform shader backdrop;\n"
            + "float hash(float2 p) {\n" + "    float3 p3 = fract(float3(p.xyx) * 0.1031);\n"
            + "    p3 += dot(p3, p3.yzx + 33.33);\n" + "    return fract((p3.x + p3.y) * p3.z);\n" + "}\n"
            + "half4 main(float2 at) {\n" + LIP
            + "    float3 seen = float3(backdrop.eval(at - shift * (1.0 + chroma)).r,\n"
            + "                         backdrop.eval(at - shift).g,\n"
            + "                         backdrop.eval(at - shift * (1.0 - chroma)).b);\n"
            + "    float luma = dot(seen, float3(0.2126, 0.7152, 0.0722));\n"
            + "    float3 c = float3(luma) + (seen - float3(luma)) * (1.0 + vibrance);\n"
            + "    float3 tone = dark > 0.5 ? float3(0.02, 0.02, 0.03) : float3(1.0);\n"
            + "    c = c * (1.0 - tint) + tone * tint;\n" + "    c *= 1.0 - shade;\n"
            + "    c += (spec + sheen) * glow;\n"
            + "    c = clamp(c, 0.0, 1.0);\n"
            + "    c += (hash(at) + hash(at + float2(17.31, 9.73)) - 1.0) * grain / 255.0;\n"
            + "    return half4(half3(clamp(c, 0.0, 1.0)), 1.0);\n" + "}\n";

    private static final Paint EDGE = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint BODY = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint RIM = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final RectF SCRATCH = new RectF();
    private static RuntimeShader edgeShader;
    private static RuntimeShader bodyShader;
    private static boolean shadersTried;

    private static void makeShaders() {
        if (shadersTried) return;
        shadersTried = true;
        if (Build.VERSION.SDK_INT < 33) return;
        try {
            edgeShader = new RuntimeShader(EDGE_SOURCE);
        } catch (Throwable unusable) {
            edgeShader = null;
        }
        try {
            bodyShader = new RuntimeShader(BODY_SOURCE);
        } catch (Throwable unusable) {
            bodyShader = null;
        }
    }

    private static float shapeKind(RectF box, float round) {
        float w = box.width(), h = box.height(), shorter = Math.min(w, h);
        if (round < shorter / 2f - 0.5f) return 0f;
        return Math.abs(w - h) < 0.5f ? 2f : 1f;
    }

    private static void setGeometry(RuntimeShader shader, RectF box, float round, float unit, boolean dark) {
        shader.setFloatUniform("size", box.width(), box.height());
        shader.setFloatUniform("origin", box.left, box.top);
        shader.setFloatUniform("radius", round);
        shader.setFloatUniform("shapeKind", shapeKind(box, round));
        shader.setFloatUniform("edgeWidth", BEVEL_WIDTH * unit);
        shader.setFloatUniform("unit", unit);
        shader.setFloatUniform("dark", dark ? 1f : 0f);
        shader.setFloatUniform("lightDir", LIGHT_X, LIGHT_Y);
        shader.setFloatUniform("specSharpness", SPEC_SHARPNESS);
        shader.setFloatUniform("specGain", SPEC_GAIN);
        shader.setFloatUniform("fresnelPower", FRESNEL_POWER);
        shader.setFloatUniform("fresnelGain", FRESNEL_GAIN);
        shader.setFloatUniform("shadeGain", SHADE_GAIN);
        shader.setFloatUniform("edgeIntensity", EDGE_INTENSITY);
        shader.setFloatUniform("edgeDistance", EDGE_DISTANCE);
        shader.setFloatUniform("rimIntensity", RIM_INTENSITY);
        shader.setFloatUniform("rimDistance", RIM_DISTANCE);
    }

    public static void light(Canvas canvas, RectF box, float round, float unit, boolean dark) {
        makeShaders();
        if (edgeShader != null) {
            setGeometry(edgeShader, box, round, unit, dark);
            EDGE.setShader(edgeShader);
            canvas.drawRoundRect(box, round, round, EDGE);
            return;
        }
        float inset = unit * 0.6f;
        SCRATCH.set(box);
        SCRATCH.inset(inset, inset);
        RIM.setStyle(Paint.Style.STROKE);
        RIM.setStrokeWidth(unit * 1.2f);
        int top = dark ? 0x4DFFFFFF : 0xE6FFFFFF, middle = dark ? 0x0FFFFFFF : 0x2EFFFFFF, bottom = dark ? 0x1FFFFFFF : 0x66FFFFFF;
        RIM.setShader(new LinearGradient(0, box.top, 0, box.bottom, new int[] {top, middle, bottom}, new float[] {0f, 0.5f, 1f},
                Shader.TileMode.CLAMP));
        float innerRound = Math.max(0f, round - inset);
        canvas.drawRoundRect(SCRATCH, innerRound, innerRound, RIM);
    }

    public static void body(Canvas canvas, RectF box, float round, float unit, boolean dark, Shader backdrop) {
        makeShaders();
        if (bodyShader == null || backdrop == null) {
            light(canvas, box, round, unit, dark);
            return;
        }
        bodyShader.setInputShader("backdrop", backdrop);
        setGeometry(bodyShader, box, round, unit, dark);
        bodyShader.setFloatUniform("chroma", CHROMA);
        bodyShader.setFloatUniform("vibrance", VIBRANCE);
        bodyShader.setFloatUniform("tint", dark ? TINT_DARK : TINT_LIGHT);
        bodyShader.setFloatUniform("grain", GRAIN);
        BODY.setShader(bodyShader);
        canvas.drawRoundRect(box, round, round, BODY);
    }

    public static Shader backdrop(Bitmap snapshot, float downscale) {
        Bitmap current = snapshot;
        for (float factor = downscale; factor >= 2f; factor /= 2f) {
            int w = Math.max(1, current.getWidth() / 2), h = Math.max(1, current.getHeight() / 2);
            Bitmap half = Bitmap.createScaledBitmap(current, w, h, true);
            if (current != snapshot) current.recycle();
            current = half;
        }
        BitmapShader shader = new BitmapShader(current, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        Matrix stretch = new Matrix();
        stretch.setScale(snapshot.getWidth() / (float) current.getWidth(), snapshot.getHeight() / (float) current.getHeight());
        shader.setLocalMatrix(stretch);
        return shader;
    }

    public static void shadow(Paint paint, float unit) {
        paint.setShadowLayer(unit * 6f, 0f, unit * 6f, 0x33000000);
    }

    public static void unshadow(Paint paint) {
        paint.clearShadowLayer();
    }

    private static final Paint SLAB = new Paint(Paint.ANTI_ALIAS_FLAG);
    public static void slab(Canvas canvas, RectF box, float round, float unit, int ground) {
        boolean dark = Tint.darkGlass(Themes.isDark(), ground);
        SLAB.setColor(dark ? (Tint.glassBody(Themes.isFullAmoled()) & 0x00FFFFFF) | (BODY_DARK_ALPHA << 24) : BODY_LIGHT);
        shadow(SLAB, unit);
        canvas.drawRoundRect(box, round, round, SLAB);
        unshadow(SLAB);
        light(canvas, box, round, unit, dark);
    }
}
